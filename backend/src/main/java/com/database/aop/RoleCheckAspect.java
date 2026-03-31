package com.database.aop;

import com.database.exception.BusinessException;
import com.database.mapper.StaffRolesMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色权限校验切面：配合 @RequireRole 注解使用。
 * 从 request attribute 中取 AuthInterceptor 注入的 userId，
 * 查询该用户的角色列表，判断是否包含所需角色。
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RoleCheckAspect {

    private final StaffRolesMapper staffRolesMapper;

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException("无法获取请求上下文", 403);
        }

        HttpServletRequest request = attrs.getRequest();
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new BusinessException("未登录，无法校验权限", 401);
        }

        Long userId = (Long) userIdObj;
        List<String> userRoles = staffRolesMapper.selectRoleNamesByStaffId(userId);

        Set<String> required = Arrays.stream(requireRole.value())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean hasRole = userRoles.stream()
                .map(String::toLowerCase)
                .anyMatch(required::contains);

        if (!hasRole) {
            log.warn("用户 {} 缺少所需角色 {}，当前角色: {}", userId, required, userRoles);
            throw new BusinessException(
                    "权限不足，需要角色: " + String.join(" / ", requireRole.value()), 403);
        }
    }
}
