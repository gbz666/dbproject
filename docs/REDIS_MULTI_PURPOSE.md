# Redis 多用途使用方案

## 问题：如果 Redis 不止用于登录，还有其他用途怎么办？

### 当前实现分析

当前代码已经使用了**最佳实践**：**Key 前缀命名空间**

```java
// TokenService.java 中的实现
private static final String TOKEN_PREFIX = "token:";
private static final String USER_TOKEN_PREFIX = "user_token:";
```

这种方式可以很好地隔离不同业务的数据。

---

## 解决方案：三种方式

### 方案一：Key 前缀命名空间（推荐，当前已使用）

**优点**：
- ✅ 简单易用，无需额外配置
- ✅ 所有业务共享同一个 Redis 实例
- ✅ 便于统一管理和监控
- ✅ 代码清晰，易于维护

**实现方式**：
为不同业务使用不同的 Key 前缀

```java
// 认证相关
private static final String TOKEN_PREFIX = "auth:token:";
private static final String USER_TOKEN_PREFIX = "auth:user_token:";

// 缓存相关（示例）
private static final String CACHE_PRODUCT_PREFIX = "cache:product:";
private static final String CACHE_CUSTOMER_PREFIX = "cache:customer:";

// 限流相关（示例）
private static final String RATE_LIMIT_PREFIX = "ratelimit:";

// 会话相关（示例）
private static final String SESSION_PREFIX = "session:";
```

**示例代码**：

```java
@Service
public class ProductCacheService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_PREFIX = "cache:product:";
    private static final long CACHE_EXPIRE = 3600; // 1小时
    
    public void cacheProduct(Long productId, ProductVO product) {
        String key = CACHE_PREFIX + productId;
        redisTemplate.opsForValue().set(key, product, CACHE_EXPIRE, TimeUnit.SECONDS);
    }
    
    public ProductVO getCachedProduct(Long productId) {
        String key = CACHE_PREFIX + productId;
        return (ProductVO) redisTemplate.opsForValue().get(key);
    }
}
```

---

### 方案二：使用不同的 Redis Database（可选）

Redis 支持 0-15 共 16 个数据库，可以为不同业务分配不同的 database。

**配置示例**：

```yaml
spring:
  data:
    redis:
      # 认证相关使用 database 0
      database: 0
      
# 或者创建多个 RedisTemplate
```

**多 Database 配置类**：

```java
@Configuration
public class MultiRedisConfig {
    
    // 认证 RedisTemplate（database 0）
    @Bean
    @Primary
    public RedisTemplate<String, Object> authRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 可以设置不同的 database
        return template;
    }
    
    // 缓存 RedisTemplate（database 1）
    @Bean("cacheRedisTemplate")
    public RedisTemplate<String, Object> cacheRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**使用方式**：

```java
@Service
public class ProductCacheService {
    @Autowired
    @Qualifier("cacheRedisTemplate")
    private RedisTemplate<String, Object> cacheRedisTemplate;
    
    // 使用 cacheRedisTemplate 进行操作
}
```

**注意**：
- ⚠️ Redis 官方不推荐使用多个 database，建议使用不同的 Redis 实例
- ⚠️ 多个 database 之间无法原子操作
- ✅ 但对于小型项目，这种方式仍然可用

---

### 方案三：使用不同的 Redis 实例（生产环境推荐）

**优点**：
- ✅ 完全隔离，互不影响
- ✅ 可以独立扩展和监控
- ✅ 符合 Redis 官方最佳实践

**配置示例**：

```yaml
spring:
  data:
    redis:
      # 认证 Redis
      host: localhost
      port: 6379
      database: 0
      
# 缓存 Redis（需要单独配置）
cache:
  redis:
    host: localhost
    port: 6380  # 不同的端口
    database: 0
```

---

## 推荐方案：Key 前缀 + 统一管理

### 创建 Redis Key 管理工具类

```java
package com.database.util;

/**
 * Redis Key 前缀管理
 * 统一管理所有 Redis Key 的前缀，避免冲突
 */
public class RedisKeyPrefix {
    
    // ========== 认证相关 ==========
    public static final String AUTH_TOKEN = "auth:token:";
    public static final String AUTH_USER_TOKEN = "auth:user_token:";
    public static final String AUTH_REFRESH_TOKEN = "auth:refresh_token:";
    
    // ========== 缓存相关 ==========
    public static final String CACHE_PRODUCT = "cache:product:";
    public static final String CACHE_CUSTOMER = "cache:customer:";
    public static final String CACHE_SUPPLIER = "cache:supplier:";
    public static final String CACHE_INVENTORY = "cache:inventory:";
    
    // ========== 限流相关 ==========
    public static final String RATE_LIMIT_IP = "ratelimit:ip:";
    public static final String RATE_LIMIT_USER = "ratelimit:user:";
    
    // ========== 会话相关 ==========
    public static final String SESSION = "session:";
    
    // ========== 分布式锁 ==========
    public static final String LOCK = "lock:";
    
    /**
     * 构建完整的 Key
     */
    public static String buildKey(String prefix, String suffix) {
        return prefix + suffix;
    }
}
```

### 使用示例

```java
@Service
public class ProductCacheService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void cacheProduct(Long productId, ProductVO product) {
        String key = RedisKeyPrefix.buildKey(RedisKeyPrefix.CACHE_PRODUCT, String.valueOf(productId));
        redisTemplate.opsForValue().set(key, product, 1, TimeUnit.HOURS);
    }
    
    public ProductVO getCachedProduct(Long productId) {
        String key = RedisKeyPrefix.buildKey(RedisKeyPrefix.CACHE_PRODUCT, String.valueOf(productId));
        return (ProductVO) redisTemplate.opsForValue().get(key);
    }
}
```

---

## 总结

| 方案 | 适用场景 | 优点 | 缺点 |
|------|---------|------|------|
| **Key 前缀** | 小型/中型项目 | 简单、易维护 | 需要统一管理前缀 |
| **多 Database** | 中型项目 | 逻辑隔离 | 官方不推荐，功能受限 |
| **多实例** | 大型/生产环境 | 完全隔离、可扩展 | 需要更多资源 |

**推荐**：对于您的项目，使用 **Key 前缀命名空间** 即可，这是最简单且有效的方案。
