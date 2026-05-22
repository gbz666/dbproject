package com.database.mapper;

import com.database.pojo.AiSqlMemory;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 针对表【ai_sql_memory(AI SQL 模板记忆表)】的数据库操作 Mapper
 * @Entity com.database.pojo.AiSqlMemory
 *
 * 注：本期 Phase 3 极简版只提供保存/复用/反馈相关的方法，
 * 检索方法（按归一化问题、表重叠度）留到下一版做 RAG 时再加。
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
}
