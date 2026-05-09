package com.database.mapper;

import com.database.pojo.AiConversation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【ai_conversations(AI 对话表)】的数据库操作 Mapper
 * @Entity com.database.pojo.AiConversation
 */
public interface AiConversationMapper {

    /**
     * 插入一条对话
     */
    int insert(AiConversation record);

    /**
     * 根据主键查询
     */
    AiConversation selectByPrimaryKey(Long id);

    /**
     * 根据用户 ID 查询对话列表（按更新时间倒序，排除已删除）
     */
    List<AiConversation> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新对话标题
     */
    int updateTitle(@Param("id") Long id, @Param("title") String title);

    /**
     * 软删除对话
     */
    int softDelete(@Param("id") Long id);

    /**
     * 更新对话的 updated_at 时间戳
     */
    int updateTimestamp(@Param("id") Long id);
}
