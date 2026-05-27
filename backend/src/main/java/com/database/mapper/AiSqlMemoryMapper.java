package com.database.mapper;

import com.database.pojo.AiSqlMemory;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 针对表【ai_sql_memory(AI SQL 模板记忆表)】的数据库操作 Mapper
 * @Entity com.database.pojo.AiSqlMemory
 */
public interface AiSqlMemoryMapper {

    /**
     * 插入一条 memory 记录，返回自增 id（通过 useGeneratedKeys 回填到 memory.id）
     */
    void insert(AiSqlMemory memory);

    /**
     * 按主键查询
     */
    AiSqlMemory selectById(@Param("id") Long id);

    /**
     * 按 SQL 模板完全相同查询（用于 saveOrReuseMemory 去重）
     */
    AiSqlMemory selectBySqlTemplate(@Param("sqlTemplate") String sqlTemplate);

    /**
     * 按归一化问题精确匹配（用于 RAG 检索的首选复用通道）。
     * 空串归一化问题不参与匹配，避免历史脏数据被误命中。
     */
    AiSqlMemory selectByNormalizedQuestion(@Param("normalizedQuestion") String normalizedQuestion);

    /**
     * 复用次数 +1 并更新 last_used_at
     */
    void incrementUseCount(@Param("id") Long id);

    /**
     * 成功次数 +1
     */
    void incrementSuccessCount(@Param("id") Long id);

    /**
     * 更新置信度
     */
    void updateConfidence(@Param("id") Long id, @Param("confidence") BigDecimal confidence);

    /**
     * 更新 status（用于把 memory 降级为 'failed' 等）
     */
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * RAG 检索：召回 normalized_question 命中任一 gram 或 intent 相同的高质量 memory。
     * 质量门槛：success_count >= 1 且 status 不在 ('failed','archived')。
     *
     * @param grams         归一化问题切出的 2-gram 列表（至少 1 个）
     * @param intent        当前问题的 intent_tag（可为空）
     * @param minConfidence confidence 下限
     * @param limit         返回上限
     */
    List<AiSqlMemory> searchCandidates(
            @Param("grams") List<String> grams,
            @Param("intent") String intent,
            @Param("minConfidence") BigDecimal minConfidence,
            @Param("limit") int limit
    );
}
