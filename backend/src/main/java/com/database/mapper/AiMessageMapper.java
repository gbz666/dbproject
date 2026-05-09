package com.database.mapper;

import com.database.pojo.AiMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 针对表【ai_messages(AI 消息表)】的数据库操作 Mapper
 * @Entity com.database.pojo.AiMessage
 */
public interface AiMessageMapper {

    /**
     * 插入一条消息
     */
    int insert(AiMessage record);

    /**
     * 根据对话 ID 查询消息列表（按创建时间正序）
     */
    List<AiMessage> selectByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 根据对话 ID 删除所有消息（物理删除，级联由数据库处理）
     */
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
