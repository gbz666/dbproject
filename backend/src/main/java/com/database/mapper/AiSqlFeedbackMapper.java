package com.database.mapper;

import com.database.pojo.AiSqlFeedback;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

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

    /**
     * 批量查询某用户对一组 memory 的反馈，用于消息加载时回填 feedbackVote。
     * 返回每条记录含 memory_id 与 vote 两个字段（已通过 ResultMap 映射到 POJO）。
     */
    List<AiSqlFeedback> selectByUserAndMemoryIds(
            @Param("userId") Long userId,
            @Param("memoryIds") Collection<Long> memoryIds);
}
