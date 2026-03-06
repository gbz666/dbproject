package com.database.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务类
 * 负责生成、验证、刷新 JWT Token，并使用 Redis 管理 Token 过期
 */
@Service
@Slf4j
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // JWT 密钥（应该从配置文件读取，这里使用默认值）
    @Value("${jwt.secret:your-secret-key-should-be-at-least-256-bits-long-for-security}")
    private String secret;

    // Token 过期时间（秒），默认 2 小时
    @Value("${jwt.expiration:7200}")
    private Long expiration;

    // Redis Key 前缀
    private static final String TOKEN_PREFIX = "token:";
    private static final String USER_TOKEN_PREFIX = "user_token:";

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param staffName 员工姓名（作为登录标识）
     * @return Token 字符串
     */
    public String generateToken(Long userId, String staffName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("staffName", staffName)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        // 将 Token 存储到 Redis，设置过期时间
        String tokenKey = TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, userId, expiration, TimeUnit.SECONDS);

        // 存储用户对应的 Token（用于单点登录，一个用户只能有一个有效Token）
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        String oldToken = (String) redisTemplate.opsForValue().get(userTokenKey);
        if (oldToken != null) {
            // 删除旧的 Token
            redisTemplate.delete(TOKEN_PREFIX + oldToken);
        }
        redisTemplate.opsForValue().set(userTokenKey, token, expiration, TimeUnit.SECONDS);

        log.info("生成Token成功，用户ID: {}, Token过期时间: {}秒", userId, expiration);
        return token;
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token Token 字符串
     * @return 如果有效返回用户ID，否则返回 null
     */
    public Long validateToken(String token) {
        try {
            // 先从 Redis 中检查 Token 是否存在
            String tokenKey = TOKEN_PREFIX + token;
            Object redisValue = redisTemplate.opsForValue().get(tokenKey);
            if (redisValue == null) {
                log.warn("Token不存在于Redis中，可能已过期: {}", token);
                return null;
            }

            // 验证 JWT Token 本身是否有效
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userIdFromToken = Long.parseLong(claims.getSubject());

            // Redis 反序列化时 Long 可能变成 Integer，统一转为 Long 再比较
            Long userIdFromRedis = redisValue instanceof Number
                    ? ((Number) redisValue).longValue()
                    : Long.parseLong(redisValue.toString());

            if (!userIdFromToken.equals(userIdFromRedis)) {
                log.warn("Token中的用户ID与Redis中的不一致");
                return null;
            }

            return userIdFromToken;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取员工姓名
     *
     * @param token Token 字符串
     * @return 员工姓名
     */
    public String getStaffNameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("staffName", String.class);
        } catch (Exception e) {
            log.error("从Token获取员工姓名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 刷新 Token（延长过期时间）
     *
     * @param token 原 Token
     * @return 新的 Token，如果原 Token 无效则返回 null
     */
    public String refreshToken(String token) {
        Long userId = validateToken(token);
        if (userId == null) {
            return null;
        }

        String staffName = getStaffNameFromToken(token);
        if (staffName == null) {
            return null;
        }

        // 删除旧 Token
        deleteToken(token);

        // 生成新 Token
        return generateToken(userId, staffName);
    }

    /**
     * 删除 Token（登出时使用）
     *
     * @param token Token 字符串
     */
    public void deleteToken(String token) {
        try {
            Long userId = validateToken(token);
            if (userId != null) {
                // 删除 Token
                redisTemplate.delete(TOKEN_PREFIX + token);
                // 删除用户对应的 Token
                redisTemplate.delete(USER_TOKEN_PREFIX + userId);
                log.info("Token已删除，用户ID: {}", userId);
            }
        } catch (Exception e) {
            log.error("删除Token失败: {}", e.getMessage());
        }
    }

    /**
     * 检查 Token 是否即将过期（剩余时间少于30分钟）
     *
     * @param token Token 字符串
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            String tokenKey = TOKEN_PREFIX + token;
            Long expire = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
            return expire != null && expire > 0 && expire < 1800; // 30分钟 = 1800秒
        } catch (Exception e) {
            log.error("检查Token过期时间失败: {}", e.getMessage());
            return false;
        }
    }
}
