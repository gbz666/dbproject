package com.database.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许对所有路径进行跨域访问
                .allowedOrigins(
                        "http://localhost:5173", // 允许您的 Vue 前端开发服务器访问
                        "http://127.0.0.1:5173"  // 以防万一，也加上 127.0.0.1
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许发送 Cookie (如需 session 或 token 认证)
                .maxAge(3600); // 预检请求的有效期
    }
}