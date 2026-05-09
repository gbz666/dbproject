package com.database.service;

import com.database.dto.*;
import com.database.exception.BusinessException;
import com.database.mapper.AiConversationMapper;
import com.database.mapper.AiMessageMapper;
import com.database.pojo.AiConversation;
import com.database.pojo.AiMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 对话服务：管理对话和消息的 CRUD
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiConversationService {

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建新对话
     */
    public AiConversationResponse createConversation(Long userId, AiConversationCreateRequest request) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(request.getTitle() != null ? request.getTitle() : "新对话");

        conversationMapper.insert(conversation);

        AiConversationResponse response = new AiConversationResponse();
        response.setId(conversation.getId());
        response.setTitle(conversation.getTitle());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());
        response.setMessageCount(0);
        response.setMessages(new ArrayList<>());
        return response;
    }

    /**
     * 获取用户的对话列表
     */
    public List<AiConversationResponse> getConversations(Long userId) {
        List<AiConversation> conversations = conversationMapper.selectByUserId(userId);
        List<AiConversationResponse> responses = new ArrayList<>();

        for (AiConversation conv : conversations) {
            AiConversationResponse response = new AiConversationResponse();
            response.setId(conv.getId());
            response.setTitle(conv.getTitle());
            response.setCreatedAt(conv.getCreatedAt());
            response.setUpdatedAt(conv.getUpdatedAt());
            // 查询消息数量
            List<AiMessage> messages = messageMapper.selectByConversationId(conv.getId());
            response.setMessageCount(messages.size());
            responses.add(response);
        }

        return responses;
    }

    /**
     * 获取单个对话详情（包含消息列表）
     */
    public AiConversationResponse getConversationDetail(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectByPrimaryKey(conversationId);
        if (conversation == null) {
            throw new BusinessException("对话不存在", 404);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此对话", 403);
        }

        AiConversationResponse response = new AiConversationResponse();
        response.setId(conversation.getId());
        response.setTitle(conversation.getTitle());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setUpdatedAt(conversation.getUpdatedAt());

        // 查询消息列表
        List<AiMessage> messages = messageMapper.selectByConversationId(conversationId);
        List<AiMessageResponse> messageResponses = new ArrayList<>();

        for (AiMessage msg : messages) {
            AiMessageResponse msgResponse = new AiMessageResponse();
            msgResponse.setId(msg.getId());
            msgResponse.setRole(msg.getRole());
            msgResponse.setContent(msg.getContent());
            msgResponse.setErrorMsg(msg.getErrorMsg());
            msgResponse.setCreatedAt(msg.getCreatedAt());

            // 解析 sqlResult JSON
            if (msg.getSqlResult() != null) {
                try {
                    msgResponse.setSqlResult(objectMapper.readValue(msg.getSqlResult(), Object.class));
                } catch (JsonProcessingException e) {
                    log.warn("解析 sqlResult JSON 失败: {}", e.getMessage());
                    msgResponse.setSqlResult(msg.getSqlResult());
                }
            }

            messageResponses.add(msgResponse);
        }

        response.setMessageCount(messageResponses.size());
        response.setMessages(messageResponses);

        return response;
    }

    /**
     * 更新对话标题
     */
    public void updateConversationTitle(Long conversationId, Long userId, String title) {
        AiConversation conversation = conversationMapper.selectByPrimaryKey(conversationId);
        if (conversation == null) {
            throw new BusinessException("对话不存在", 404);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此对话", 403);
        }

        conversationMapper.updateTitle(conversationId, title);
    }

    /**
     * 删除对话（软删除）
     */
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectByPrimaryKey(conversationId);
        if (conversation == null) {
            throw new BusinessException("对话不存在", 404);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此对话", 403);
        }

        conversationMapper.softDelete(conversationId);
    }

    /**
     * 保存消息到对话
     */
    public AiMessageResponse saveMessage(Long conversationId, Long userId, AiMessageSaveRequest request) {
        // 验证对话存在且属于当前用户
        AiConversation conversation = conversationMapper.selectByPrimaryKey(conversationId);
        if (conversation == null) {
            throw new BusinessException("对话不存在", 404);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此对话", 403);
        }

        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole(request.getRole());
        message.setContent(request.getContent());
        message.setSqlResult(request.getSqlResult());
        message.setErrorMsg(request.getErrorMsg());

        messageMapper.insert(message);

        // 更新对话的 updated_at
        conversationMapper.updateTimestamp(conversationId);

        AiMessageResponse response = new AiMessageResponse();
        response.setId(message.getId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setErrorMsg(message.getErrorMsg());
        response.setCreatedAt(message.getCreatedAt());

        // 解析 sqlResult
        if (message.getSqlResult() != null) {
            try {
                response.setSqlResult(objectMapper.readValue(message.getSqlResult(), Object.class));
            } catch (JsonProcessingException e) {
                response.setSqlResult(message.getSqlResult());
            }
        }

        return response;
    }
}
