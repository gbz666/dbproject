package com.database.controller;

import com.database.dto.*;
import com.database.service.AiConversationService;
import com.database.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 对话管理控制器
 */
@RestController
@RequestMapping("/api/ai/conversations")
public class AiConversationController {

    private final AiConversationService conversationService;

    @Autowired
    public AiConversationController(AiConversationService conversationService) {
        this.conversationService = conversationService;
    }

    /**
     * 创建新对话
     */
    @PostMapping
    public ResponseEntity<Result<AiConversationResponse>> createConversation(
            @RequestBody(required = false) AiConversationCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (request == null) {
            request = new AiConversationCreateRequest();
        }
        AiConversationResponse response = conversationService.createConversation(userId, request);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 获取当前用户的对话列表
     */
    @GetMapping
    public ResponseEntity<Result<List<AiConversationResponse>>> getConversations(
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<AiConversationResponse> responses = conversationService.getConversations(userId);
        return ResponseEntity.ok(Result.success(responses));
    }

    /**
     * 获取对话详情（包含消息列表）
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<AiConversationResponse>> getConversationDetail(
            @PathVariable("id") Long conversationId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AiConversationResponse response = conversationService.getConversationDetail(conversationId, userId);
        return ResponseEntity.ok(Result.success(response));
    }

    /**
     * 更新对话标题
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> updateConversationTitle(
            @PathVariable("id") Long conversationId,
            @Valid @RequestBody AiConversationUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        conversationService.updateConversationTitle(conversationId, userId, request.getTitle());
        return ResponseEntity.ok(Result.success());
    }

    /**
     * 删除对话（软删除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteConversation(
            @PathVariable("id") Long conversationId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.ok(Result.success());
    }

    /**
     * 保存消息到对话
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<Result<AiMessageResponse>> saveMessage(
            @PathVariable("id") Long conversationId,
            @Valid @RequestBody AiMessageSaveRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AiMessageResponse response = conversationService.saveMessage(conversationId, userId, request);
        return ResponseEntity.ok(Result.success(response));
    }
}
