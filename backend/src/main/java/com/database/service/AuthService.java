package com.database.service;

import com.database.dto.LoginRequest;
import com.database.dto.LoginResponse;
import com.database.exception.BusinessException;
import com.database.pojo.Staffs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务类
 * 负责用户登录、密码验证等认证相关业务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final StaffService staffService;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.expiration:7200}")
    private Long expiration;

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（包含Token）
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 根据员工姓名查询员工（登录使用 staffName）
        Staffs staff = staffService.getStaffByStaffName(request.getStaffName());
        if (staff == null) {
            throw new BusinessException("员工姓名或密码错误", 401);
        }

        // 2. 检查账户状态
        if (staff.getStatus() == null || staff.getStatus() == 0) {
            throw new BusinessException("账户已被禁用，请联系管理员", 403);
        }

        // 3. 验证密码
        log.info("TEMP: correct hash for [{}] = {}", request.getPassword(), passwordEncoder.encode(request.getPassword()));
        if (staff.getPassword() == null || !passwordEncoder.matches(request.getPassword(), staff.getPassword())) {
            throw new BusinessException("员工姓名或密码错误", 401);
        }

        // 4. 生成 Token（存入 staffName）
        String token = tokenService.generateToken(staff.getId(), staff.getStaffName());

        // 5. 更新最后登录时间
        staffService.updateLastLoginTime(staff.getId());

        log.info("用户登录成功，员工姓名: {}, 用户ID: {}", staff.getStaffName(), staff.getId());

        // 6. 返回登录响应
        return new LoginResponse(
                token,
                staff.getId(),
                staff.getStaffName(),
                expiration
        );
    }

    /**
     * 用户登出
     *
     * @param token Token字符串
     */
    public void logout(String token) {
        tokenService.deleteToken(token);
        log.info("用户登出成功");
    }

    /**
     * 刷新 Token，返回新 Token 及员工姓名等
     *
     * @param token 原 Token
     * @return 登录响应（含新 token、userId、staffName、expiration），无效则 null
     */
    public LoginResponse refreshToken(String token) {
        Long userId = tokenService.validateToken(token);
        if (userId == null) {
            return null;
        }
        String staffName = tokenService.getStaffNameFromToken(token);
        if (staffName == null) {
            return null;
        }
        String newToken = tokenService.refreshToken(token);
        if (newToken == null) {
            return null;
        }
        return new LoginResponse(newToken, userId, staffName, expiration);
    }
}
