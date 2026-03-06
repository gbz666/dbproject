package com.database.interceptor;

import com.database.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 拦截需要认证的请求，验证Token有效性
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    // 不需要认证的路径
    private static final String[] EXCLUDE_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/error"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS 预检请求（OPTIONS）必须放行，否则浏览器收不到 Access-Control-* 头会报跨域错误
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // 检查是否是不需要认证的路径
        for (String excludePath : EXCLUDE_PATHS) {
            if (path.startsWith(excludePath)) {
                return true;
            }
        }

        // 从请求头获取Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未提供Token或Token格式错误\"}");
            return false;
        }

        String token = authHeader.substring(7); // 去掉 "Bearer " 前缀

        // 验证Token
        Long userId = tokenService.validateToken(token);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }

        // 检查Token是否即将过期（剩余时间少于30分钟），如果是则自动刷新
        if (tokenService.isTokenExpiringSoon(token)) {
            String newToken = tokenService.refreshToken(token);
            if (newToken != null) {
                // 将新Token添加到响应头中，前端可以从响应头获取新Token
                response.setHeader("X-New-Token", newToken);
                log.debug("Token已自动刷新，用户ID: {}", userId);
            }
        }

        // 将用户ID、员工姓名存储到 request 属性中，供后续使用（currentStaffId 供进项发票等 @RequestAttribute 使用）
        request.setAttribute("userId", userId);
        request.setAttribute("currentStaffId", userId);
        request.setAttribute("staffName", tokenService.getStaffNameFromToken(token));

        return true;
    }
}
