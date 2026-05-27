package com.database.service;

import com.database.dto.AiGenerateSqlResponse;
import com.database.mapper.AiSqlExecLogMapper;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final AiSqlExecLogMapper execLogMapper;
    private final ObjectMapper objectMapper;

    private static final BigDecimal DEFAULT_CONFIDENCE = new BigDecimal("0.300");
    private static final BigDecimal BASE = new BigDecimal("0.30");
    private static final BigDecimal SUCCESS_STEP = new BigDecimal("0.05");
    private static final int SUCCESS_CAP = 6;
    private static final BigDecimal UPVOTE_BONUS = new BigDecimal("0.10");
    private static final BigDecimal DOWNVOTE_PENALTY = new BigDecimal("0.15");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    /** RAG 检索质量门槛 */
    private static final BigDecimal RETRIEVE_MIN_CONFIDENCE = new BigDecimal("0.30");
    private static final int RETRIEVE_MAX_GRAMS = 8;
    /** 连续 N 次执行失败 → 把 memory 降级 status='failed'，不再被 RAG 召回 */
    private static final int FAILURE_DEMOTION_THRESHOLD = 2;

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
     * RAG 检索：根据用户原始问题召回历史高质量 memory（最多 topK 条）。
     *
     * 质量门槛（在 mapper SQL 中硬约束）：
     *   - confidence >= 0.30
     *   - success_count >= 1 （必须有至少一次真实用户执行成功）
     *   - status NOT IN ('failed','archived')
     *
     * 排序优先级：intent 命中 > 2-gram 命中数 > confidence > use_count > id
     *
     * @return 空列表表示无可用历史命中（首次问该类问题 / 库还没积累）
     */
    public List<AiSqlMemory> retrieveCandidates(String question, int topK) {
        if (topK <= 0) return List.of();

        String normalized = AiSqlNormalizer.normalize(question);
        List<String> grams = toBigrams(normalized);
        if (grams.isEmpty()) return List.of();

        String intent = AiIntentTagger.tag(normalized);

        // 多召一些再截 topK，给后续 Java 端如果想做二次重排留余量
        int initialLimit = Math.max(topK * 3, topK + 5);
        List<AiSqlMemory> raw = memoryMapper.searchCandidates(grams, intent, RETRIEVE_MIN_CONFIDENCE, initialLimit);

        if (raw.size() <= topK) return raw;
        return new ArrayList<>(raw.subList(0, topK));
    }

    /**
     * 切 2-gram。跳过 __NUM__ / __TIME__ 占位符（避免捞回所有含数字的 memory）。
     */
    private static List<String> toBigrams(String normalized) {
        if (normalized == null || normalized.isEmpty()) return List.of();
        String stripped = normalized.replace("__num__", "").replace("__time__", "");
        if (stripped.length() < 2) return List.of();

        List<String> grams = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i + 1 < stripped.length(); i++) {
            String g = stripped.substring(i, i + 2);
            if (seen.add(g)) {
                grams.add(g);
                if (grams.size() >= RETRIEVE_MAX_GRAMS) break;
            }
        }
        return grams;
    }

    /**
     * 执行失败时调用：累计失败 >= FAILURE_DEMOTION_THRESHOLD 次，把 memory 降级为 'failed'，
     * 不再被 RAG 召回。调用方应在执行链路捕获异常的 catch 块里调用本方法（exec_log 已记录失败）。
     */
    public void recordExecFailure(Long memoryId) {
        if (memoryId == null) return;
        try {
            int failures = execLogMapper.countFailuresByMemoryId(memoryId);
            if (failures >= FAILURE_DEMOTION_THRESHOLD) {
                AiSqlMemory memory = memoryMapper.selectById(memoryId);
                if (memory != null && !"failed".equals(memory.getStatus())) {
                    memoryMapper.updateStatus(memoryId, "failed");
                    log.info("memory id={} 累计失败 {} 次 >= {}，已降级为 failed",
                            memoryId, failures, FAILURE_DEMOTION_THRESHOLD);
                }
            }
        } catch (Exception e) {
            log.warn("recordExecFailure 失败（非致命）memoryId={}: {}", memoryId, e.getMessage());
        }
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
