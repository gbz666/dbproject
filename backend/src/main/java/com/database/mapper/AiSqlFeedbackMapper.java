package com.database.mapper;

import com.database.pojo.AiSqlFeedback;
import org.apache.ibatis.annotations.Param;

/**
 * 针对表【ai_sql_feedback(AI SQL 反馈表)】的数据库操作 Mapper
 * @Entity com.database.pojo.AiSqlFeedback
 */
public interface AiSqlFeedbackMapper {

    /**
     * 插入或更新反馈记录。
     * uk_memory_user(memory_id, user_id) 唯一约束保证同一用户对同一 memory 只有一条记录，
     * 用 INSERT ... ON DUPLICATE KEY UPDATE 实现"改投"。
     */
    void insertOrUpdate(AiSqlFeedback feedback);

    /**
     * 某 memory 的点赞数（vote = +1 的总数）
     */
    int countUpvotes(@Param("memoryId") Long memoryId);

    /**
     * 某 memory 的点踩数（vote = -1 的总数）
     */
    int countDownvotes(@Param("memoryId") Long memoryId);
}
