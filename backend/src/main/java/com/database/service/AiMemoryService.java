package com.database.service;

import com.database.dto.AiGenerateSqlResponse;
import com.database.mapper.AiSqlFeedbackMapper;
import com.database.mapper.AiSqlMemoryMapper;
import com.database.pojo.AiSqlFeedback;
import com.database.pojo.AiSqlMemory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AI SQL 记忆与反馈服务。Phase 3 极简版。
 *
 * 暴露三个职责：
 *   1) saveOrReuseMemory  —— 生成 SQL 后保存模板，若已存在则复用 + use_count++
 *   2) recordExecSuccess  —— 执行成功后 success_count++ 并重算 confidence
 *   3) recordFeedback     —— 用户点赞/踩，写 ai_sql_feedback 并重算 confidence
 *
 * 置信度公式（recomputeConfidence）：
 *   base       = 0.30
 *   + 0.05 × min(successCount, 6)   // 最多 +0.30
 *   + 0.10 × upvotes
 *   - 0.15 × downvotes
 *   clamp 到 [0.0, 1.0]
 *
 * 语义说明：
 *   - 没人点过 + 没成功过 = 0.30（用户要求的"低一点的置信度"）
 *   - 一个点赞抵 2 次成功；一个点踩抵 3 个点赞
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiMemoryService {

    private final AiSqlMemoryMapper memoryMapper;
    private final AiSqlFeedbackMapper feedbackMapper;
    private final ObjectMapper objectMapper;

    private static final BigDecimal DEFAULT_CONFIDENCE = new BigDecimal("0.300");
    private static final BigDecimal BASE = new BigDecimal("0.30");
    private static final BigDecimal SUCCESS_STEP = new BigDecimal("0.05");
    private static final int SUCCESS_CAP = 6;
    private static final BigDecimal UPVOTE_BONUS = new BigDecimal("0.10");
    private static final BigDecimal DOWNVOTE_PENALTY = new BigDecimal("0.15");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    /**
     * 保存或复用 SQL 模板。完全相同的 sql_template 视为同一条记忆，
     * 复用时只把 use_count + 1、刷新 last_used_at，不覆盖原 question/params。
     *
     * @return memory.id
     */
    public Long saveOrReuseMemory(String question, AiGenerateSqlResponse result, Long userId) {
        if (result == null || result.getSqlTemplate() == null || result.getSqlTemplate().isBlank()) {
            log.warn("saveOrReuseMemory: sqlTemplate 为空，跳过记忆保存");
            return null;
        }

        // 计算三个 RAG 字段
        String normalized = AiSqlNormalizer.normalize(question);
        String tablesUsed = SqlTableExtractor.extract(result.getSqlTemplate());
        String intentTag = AiIntentTagger.tag(normalized);

        // 复用优先级 1：归一化问题完全相同（同义问题命中）
        if (!normalized.isEmpty()) {
            AiSqlMemory byNorm = memoryMapper.selectByNormalizedQuestion(normalized);
            if (byNorm != null) {
                memoryMapper.incrementUseCount(byNorm.getId());
                return byNorm.getId();
            }
        }

        // 复用优先级 2：SQL 模板完全相同（旧逻辑兼容）
        AiSqlMemory bySql = memoryMapper.selectBySqlTemplate(result.getSqlTemplate());
        if (bySql != null) {
            memoryMapper.incrementUseCount(bySql.getId());
            return bySql.getId();
        }

        AiSqlMemory memory = new AiSqlMemory();
        memory.setQuestionText(question);
        memory.setNormalizedQuestion(normalized);
        memory.setSqlTemplate(result.getSqlTemplate());
        memory.setParamsSpec(safeToJson(result.getParamsSpec()));
        memory.setChartHint(safeToJson(result.getChartHint()));
        memory.setTablesUsed(tablesUsed);
        memory.setIntentTag(intentTag);
        memory.setConfidence(DEFAULT_CONFIDENCE);
        memory.setUseCount(1);
        memory.setSuccessCount(0);
        memory.setVersion(1);
        memory.setStatus("draft");
        memory.setCreatedBy(userId);

        memoryMapper.insert(memory);
        return memory.getId();
    }

    /**
     * 执行成功后调用：success_count++ 并重算 confidence。
     * memoryId 为 null 时静默忽略（兼容首次执行未关联 memory 的场景）。
     */
    public void recordExecSuccess(Long memoryId) {
        if (memoryId == null) return;
        memoryMapper.incrementSuccessCount(memoryId);
        recomputeConfidence(memoryId);
    }

    /**
     * 记录用户反馈（点赞 +1 / 点踩 -1），同一用户对同一 memory 改投视为更新。
     */
    public void recordFeedback(Long memoryId, Long userId, int vote) {
        if (memoryId == null || userId == null) {
            throw new IllegalArgumentException("memoryId / userId 不能为空");
        }
        if (vote != 1 && vote != -1) {
            throw new IllegalArgumentException("vote 必须为 +1 或 -1，收到: " + vote);
        }

        AiSqlFeedback feedback = new AiSqlFeedback();
        feedback.setMemoryId(memoryId);
        feedback.setUserId(userId);
        feedback.setVote(vote);
        feedbackMapper.insertOrUpdate(feedback);

        recomputeConfidence(memoryId);
    }

    /**
     * 重新计算并写回 confidence。
     */
    private void recomputeConfidence(Long memoryId) {
        AiSqlMemory memory = memoryMapper.selectById(memoryId);
        if (memory == null) {
            log.warn("recomputeConfidence: memory id={} 不存在，跳过", memoryId);
            return;
        }

        int successCount = memory.getSuccessCount() == null ? 0 : memory.getSuccessCount();
        int upvotes = feedbackMapper.countUpvotes(memoryId);
        int downvotes = feedbackMapper.countDownvotes(memoryId);

        int cappedSuccess = Math.min(successCount, SUCCESS_CAP);
        BigDecimal score = BASE
                .add(SUCCESS_STEP.multiply(BigDecimal.valueOf(cappedSuccess)))
                .add(UPVOTE_BONUS.multiply(BigDecimal.valueOf(upvotes)))
                .subtract(DOWNVOTE_PENALTY.multiply(BigDecimal.valueOf(downvotes)));

        if (score.compareTo(ZERO) < 0) score = ZERO;
        if (score.compareTo(ONE) > 0) score = ONE;
        score = score.setScale(3, RoundingMode.HALF_UP);

        memoryMapper.updateConfidence(memoryId, score);
    }

    private String safeToJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("序列化为 JSON 失败: {}", e.getMessage());
            return null;
        }
    }
}
