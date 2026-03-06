package com.database.controller;

import com.database.dto.LoginRequest;
import com.database.dto.LoginResponse;
import com.database.service.AuthService;
import com.database.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器：处理登录、登出、Token 刷新
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login: 用户登录
     * @param request 登录请求（用户名、密码）
     * @return 200 OK，返回 Token 及用户信息
     */
    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        Result<LoginResponse> result = Result.success("登录成功", response);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/auth/logout: 用户登出
     * @param authorization 请求头 Authorization（Bearer Token）
     * @return 200 OK
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token);
        Result<Void> result = Result.success();
        result.setMessage("登出成功");
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/auth/refresh: 刷新 Token
     * @param authorization 请求头 Authorization（Bearer Token）
     * @return 200 OK 或 401 Token 无效/已过期
     */
    @PostMapping("/refresh")
    public ResponseEntity<Result<LoginResponse>> refreshToken(
            @RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        LoginResponse response = authService.refreshToken(token);
        if (response == null) {
            return ResponseEntity.status(401).body(Result.fail(401, "Token无效或已过期", null));
        }
        Result<LoginResponse> result = Result.success("Token刷新成功", response);
        return ResponseEntity.ok(result);
    }
}
