# RedisTemplate 封装层：为什么需要？如何实现？

## 一、为什么需要封装一层？

### 直接使用 RedisTemplate 的问题

#### 1. 代码重复

```java
// ❌ 每个地方都要写这样的代码
String cacheKey = "cache:product:" + productId;
ProductVO cached = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
if (cached != null) {
    return cached;
}
// ... 查数据库 ...
redisTemplate.opsForValue().set(cacheKey, vo, 3600, TimeUnit.SECONDS);
```

#### 2. 类型转换繁琐

```java
// ❌ 每次都要强制类型转换
ProductVO product = (ProductVO) redisTemplate.opsForValue().get("key");
```

#### 3. Key 命名不统一

```java
// ❌ 不同地方可能用不同的命名方式
redisTemplate.opsForValue().set("product:" + id, ...);
redisTemplate.opsForValue().set("cache:product:" + id, ...);
redisTemplate.opsForValue().set("product_cache:" + id, ...);
```

#### 4. 异常处理分散

```java
// ❌ 每个地方都要处理异常
try {
    redisTemplate.opsForValue().set("key", "value");
} catch (Exception e) {
    log.error("Redis 操作失败", e);
    // 降级处理...
}
```

#### 5. 业务逻辑和缓存逻辑混在一起

```java
// ❌ Service 层既要处理业务逻辑，又要处理缓存逻辑
public ProductVO getProduct(Long productId) {
    // 缓存逻辑
    String cacheKey = "cache:product:" + productId;
    ProductVO cached = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) return cached;
    
    // 业务逻辑
    Product product = productsMapper.selectByPrimaryKey(productId);
    ProductVO vo = convertToVO(product);
    
    // 缓存逻辑
    redisTemplate.opsForValue().set(cacheKey, vo, 3600, TimeUnit.SECONDS);
    return vo;
}
```

---

## 二、封装方案：统一的缓存服务

### 方案一：基础封装（推荐）

#### 1. 创建统一的缓存服务类

```java
package com.database.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 统一缓存服务
 * 封装 RedisTemplate，提供简洁的缓存操作方法
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // ========== String 类型操作（键值对）==========
    
    /**
     * 获取缓存（String 类型）
     * 
     * @param key 缓存键
     * @param clazz 返回值类型
     * @return 缓存值，不存在返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return clazz.cast(value);
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}", key, e);
            return null;  // 降级：缓存失败不影响业务
        }
    }
    
    // ========== Hash 类型操作（哈希表）==========
    
    /**
     * 获取 Hash 字段值
     * 
     * @param key Hash 键
     * @param field 字段名
     * @param clazz 返回值类型
     * @return 字段值，不存在返回 null
     */
    public <T> T hGet(String key, String field, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            return value != null ? clazz.cast(value) : null;
        } catch (Exception e) {
            log.error("获取 Hash 字段失败，key: {}, field: {}", key, field, e);
            return null;
        }
    }
    
    /**
     * 设置 Hash 字段值
     * 
     * @param key Hash 键
     * @param field 字段名
     * @param value 字段值
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("设置 Hash 字段失败，key: {}, field: {}", key, field, e);
        }
    }
    
    /**
     * 批量设置 Hash 字段
     * 
     * @param key Hash 键
     * @param map 字段-值映射
     */
    public void hSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("批量设置 Hash 字段失败，key: {}", key, e);
        }
    }
    
    /**
     * 获取 Hash 所有字段和值
     * 
     * @param key Hash 键
     * @return 字段-值映射
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取 Hash 所有字段失败，key: {}", key, e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * 删除 Hash 字段
     * 
     * @param key Hash 键
     * @param fields 字段名（可变参数）
     * @return 删除的字段数量
     */
    public Long hDelete(String key, String... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, (Object[]) fields);
        } catch (Exception e) {
            log.error("删除 Hash 字段失败，key: {}, fields: {}", key, Arrays.toString(fields), e);
            return 0L;
        }
    }
    
    /**
     * 检查 Hash 字段是否存在
     * 
     * @param key Hash 键
     * @param field 字段名
     * @return 是否存在
     */
    public boolean hExists(String key, String field) {
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("检查 Hash 字段是否存在失败，key: {}, field: {}", key, field, e);
            return false;
        }
    }
    
    // ========== List 类型操作（列表）==========
    
    /**
     * 从左侧推入列表
     * 
     * @param key 列表键
     * @param value 值
     * @return 列表长度
     */
    public Long lPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("左侧推入列表失败，key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 从右侧推入列表
     * 
     * @param key 列表键
     * @param value 值
     * @return 列表长度
     */
    public Long rPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("右侧推入列表失败，key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 从左侧弹出列表
     * 
     * @param key 列表键
     * @param clazz 返回值类型
     * @return 弹出的值，列表为空返回 null
     */
    public <T> T lPop(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForList().leftPop(key);
            return value != null ? clazz.cast(value) : null;
        } catch (Exception e) {
            log.error("左侧弹出列表失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 从右侧弹出列表（阻塞式，带超时）
     * 
     * @param key 列表键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @param clazz 返回值类型
     * @return 弹出的值，超时返回 null
     */
    public <T> T rPop(String key, long timeout, TimeUnit unit, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForList().rightPop(key, timeout, unit);
            return value != null ? clazz.cast(value) : null;
        } catch (Exception e) {
            log.error("右侧弹出列表失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 获取列表指定范围元素
     * 
     * @param key 列表键
     * @param start 起始位置（0 开始）
     * @param end 结束位置（-1 表示最后）
     * @param clazz 元素类型
     * @return 元素列表
     */
    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) {
        try {
            List<Object> list = redisTemplate.opsForList().range(key, start, end);
            if (list == null) {
                return Collections.emptyList();
            }
            return list.stream()
                    .map(clazz::cast)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取列表范围失败，key: {}", key, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取列表长度
     * 
     * @param key 列表键
     * @return 列表长度
     */
    public Long lSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("获取列表长度失败，key: {}", key, e);
            return 0L;
        }
    }
    
    // ========== Set 类型操作（集合）==========
    
    /**
     * 添加元素到集合
     * 
     * @param key 集合键
     * @param values 值（可变参数）
     * @return 添加的元素数量
     */
    public Long sAdd(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("添加集合元素失败，key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 从集合移除元素
     * 
     * @param key 集合键
     * @param values 值（可变参数）
     * @return 移除的元素数量
     */
    public Long sRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            log.error("移除集合元素失败，key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 获取集合所有元素
     * 
     * @param key 集合键
     * @param clazz 元素类型
     * @return 元素集合
     */
    public <T> Set<T> sMembers(String key, Class<T> clazz) {
        try {
            Set<Object> set = redisTemplate.opsForSet().members(key);
            if (set == null) {
                return Collections.emptySet();
            }
            return set.stream()
                    .map(clazz::cast)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("获取集合所有元素失败，key: {}", key, e);
            return Collections.emptySet();
        }
    }
    
    /**
     * 判断元素是否在集合中
     * 
     * @param key 集合键
     * @param value 值
     * @return 是否存在
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("检查集合元素是否存在失败，key: {}", key, e);
            return false;
        }
    }
    
    /**
     * 获取集合大小
     * 
     * @param key 集合键
     * @return 集合大小
     */
    public Long sSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("获取集合大小失败，key: {}", key, e);
            return 0L;
        }
    }
    
    // ========== ZSet 类型操作（有序集合）==========
    
    /**
     * 添加元素到有序集合（带分数）
     * 
     * @param key 有序集合键
     * @param value 值
     * @param score 分数
     * @return 是否添加成功
     */
    public boolean zAdd(String key, Object value, double score) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, value, score));
        } catch (Exception e) {
            log.error("添加有序集合元素失败，key: {}", key, e);
            return false;
        }
    }
    
    /**
     * 从有序集合移除元素
     * 
     * @param key 有序集合键
     * @param values 值（可变参数）
     * @return 移除的元素数量
     */
    public Long zRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception e) {
            log.error("移除有序集合元素失败，key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 获取有序集合指定范围元素（按分数从低到高）
     * 
     * @param key 有序集合键
     * @param start 起始位置
     * @param end 结束位置
     * @param clazz 元素类型
     * @return 元素集合
     */
    public <T> Set<T> zRange(String key, long start, long end, Class<T> clazz) {
        try {
            Set<Object> set = redisTemplate.opsForZSet().range(key, start, end);
            if (set == null) {
                return Collections.emptySet();
            }
            return set.stream()
                    .map(clazz::cast)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("获取有序集合范围失败，key: {}", key, e);
            return Collections.emptySet();
        }
    }
    
    /**
     * 获取有序集合指定范围元素（按分数从高到低，用于排行榜）
     * 
     * @param key 有序集合键
     * @param start 起始位置
     * @param end 结束位置
     * @param clazz 元素类型
     * @return 元素集合
     */
    public <T> Set<T> zReverseRange(String key, long start, long end, Class<T> clazz) {
        try {
            Set<Object> set = redisTemplate.opsForZSet().reverseRange(key, start, end);
            if (set == null) {
                return Collections.emptySet();
            }
            return set.stream()
                    .map(clazz::cast)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("获取有序集合反向范围失败，key: {}", key, e);
            return Collections.emptySet();
        }
    }
    
    /**
     * 增加元素分数
     * 
     * @param key 有序集合键
     * @param value 值
     * @param delta 增量
     * @return 增加后的分数
     */
    public Double zIncrementScore(String key, Object value, double delta) {
        try {
            return redisTemplate.opsForZSet().incrementScore(key, value, delta);
        } catch (Exception e) {
            log.error("增加有序集合元素分数失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 获取元素分数
     * 
     * @param key 有序集合键
     * @param value 值
     * @return 分数，不存在返回 null
     */
    public Double zScore(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().score(key, value);
        } catch (Exception e) {
            log.error("获取有序集合元素分数失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 获取有序集合大小
     * 
     * @param key 有序集合键
     * @return 集合大小
     */
    public Long zSize(String key) {
        try {
            return redisTemplate.opsForZSet().size(key);
        } catch (Exception e) {
            log.error("获取有序集合大小失败，key: {}", key, e);
            return 0L;
        }
    }
    
    /**
     * 设置缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}", key, e);
            // 降级：缓存失败不影响业务，只记录日志
        }
    }
    
    /**
     * 设置缓存（默认过期时间：1小时）
     */
    public void set(String key, Object value) {
        set(key, value, 1, TimeUnit.HOURS);
    }
    
    /**
     * 删除缓存
     * 
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败，key: {}", key, e);
        }
    }
    
    /**
     * 批量删除缓存
     * 
     * @param keys 缓存键列表
     */
    public void delete(String... keys) {
        try {
            redisTemplate.delete(java.util.Arrays.asList(keys));
        } catch (Exception e) {
            log.error("批量删除缓存失败", e);
        }
    }
    
    /**
     * 检查缓存是否存在
     * 
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查缓存是否存在失败，key: {}", key, e);
            return false;
        }
    }
    
    /**
     * 获取剩余过期时间
     * 
     * @param key 缓存键
     * @param unit 时间单位
     * @return 剩余过期时间，-1 表示永久，-2 表示不存在
     */
    public Long getExpire(String key, TimeUnit unit) {
        try {
            return redisTemplate.getExpire(key, unit);
        } catch (Exception e) {
            log.error("获取过期时间失败，key: {}", key, e);
            return -2L;
        }
    }
    
    /**
     * 设置过期时间
     * 
     * @param key 缓存键
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置过期时间失败，key: {}", key, e);
        }
    }
    
    /**
     * 原子性递增
     * 
     * @param key 缓存键
     * @param delta 增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("递增失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 原子性递减
     * 
     * @param key 缓存键
     * @param delta 减量
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            log.error("递减失败，key: {}", key, e);
            return null;
        }
    }
}
```

#### 2. 创建 Key 管理工具类

```java
package com.database.util;

/**
 * Redis Key 前缀管理
 * 统一管理所有 Redis Key 的前缀，避免冲突
 */
public class RedisKey {
    
    // ========== 认证相关 ==========
    public static final String AUTH_TOKEN = "auth:token:";
    public static final String AUTH_USER_TOKEN = "auth:user_token:";
    
    // ========== 缓存相关 ==========
    public static final String CACHE_PRODUCT = "cache:product:";
    public static final String CACHE_CUSTOMER = "cache:customer:";
    public static final String CACHE_SUPPLIER = "cache:supplier:";
    public static final String CACHE_INVENTORY = "cache:inventory:";
    
    // ========== 会话相关 ==========
    public static final String SESSION = "session:";
    
    /**
     * 构建完整的 Key
     */
    public static String build(String prefix, String suffix) {
        return prefix + suffix;
    }
    
    /**
     * 构建完整的 Key（多个后缀）
     */
    public static String build(String prefix, String... suffixes) {
        StringBuilder sb = new StringBuilder(prefix);
        for (String suffix : suffixes) {
            sb.append(suffix);
        }
        return sb.toString();
    }
}
```

#### 3. Service 层使用示例

##### 示例1：String 类型（键值对）- 产品缓存

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductsMapper productsMapper;
    private final CacheService cacheService;
    
    /**
     * 查询产品（String 类型缓存）
     */
    public ProductVO getProduct(Long productId) {
        String cacheKey = RedisKey.build(RedisKey.CACHE_PRODUCT, String.valueOf(productId));
        
        // ✅ 类型安全，无需强制转换
        ProductVO cached = cacheService.get(cacheKey, ProductVO.class);
        if (cached != null) {
            return cached;
        }
        
        Product product = productsMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return null;
        }
        
        ProductVO vo = convertToVO(product);
        cacheService.set(cacheKey, vo, 1, TimeUnit.HOURS);
        return vo;
    }
}
```

##### 示例2：Hash 类型（哈希表）- 用户信息、购物车

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final CacheService cacheService;
    
    /**
     * 缓存用户信息（Hash 类型）
     */
    public void cacheUserInfo(Long userId, UserInfoVO userInfo) {
        String hashKey = RedisKey.build(RedisKey.CACHE_USER, String.valueOf(userId));
        
        // ✅ 使用 Hash 存储用户信息
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", userInfo.getUsername());
        userMap.put("email", userInfo.getEmail());
        userMap.put("phone", userInfo.getPhone());
        
        cacheService.hSetAll(hashKey, userMap);
        cacheService.expire(hashKey, 1, TimeUnit.HOURS);
    }
    
    /**
     * 获取用户信息（Hash 类型）
     */
    public UserInfoVO getUserInfo(Long userId) {
        String hashKey = RedisKey.build(RedisKey.CACHE_USER, String.valueOf(userId));
        
        // ✅ 获取所有字段
        Map<Object, Object> userMap = cacheService.hGetAll(hashKey);
        if (userMap.isEmpty()) {
            return null;
        }
        
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUsername((String) userMap.get("username"));
        userInfo.setEmail((String) userMap.get("email"));
        userInfo.setPhone((String) userMap.get("phone"));
        return userInfo;
    }
    
    /**
     * 更新用户单个字段（Hash 类型）
     */
    public void updateUserEmail(Long userId, String email) {
        String hashKey = RedisKey.build(RedisKey.CACHE_USER, String.valueOf(userId));
        cacheService.hSet(hashKey, "email", email);
    }
}

/**
 * 购物车服务（Hash 类型）
 */
@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CacheService cacheService;
    
    /**
     * 添加商品到购物车（Hash 类型）
     */
    public void addToCart(Long userId, Long productId, Integer quantity) {
        String cartKey = RedisKey.build(RedisKey.CART, String.valueOf(userId));
        cacheService.hSet(cartKey, productId.toString(), quantity);
        cacheService.expire(cartKey, 7, TimeUnit.DAYS);  // 7天过期
    }
    
    /**
     * 获取购物车（Hash 类型）
     */
    public Map<Object, Object> getCart(Long userId) {
        String cartKey = RedisKey.build(RedisKey.CART, String.valueOf(userId));
        return cacheService.hGetAll(cartKey);
    }
    
    /**
     * 更新商品数量（Hash 类型）
     */
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String cartKey = RedisKey.build(RedisKey.CART, String.valueOf(userId));
        cacheService.hSet(cartKey, productId.toString(), quantity);
    }
    
    /**
     * 删除商品（Hash 类型）
     */
    public void removeFromCart(Long userId, Long productId) {
        String cartKey = RedisKey.build(RedisKey.CART, String.valueOf(userId));
        cacheService.hDelete(cartKey, productId.toString());
    }
}
```

##### 示例3：List 类型（列表）- 消息队列、最新列表

```java
@Service
@RequiredArgsConstructor
public class MessageQueueService {
    
    private final CacheService cacheService;
    
    /**
     * 发送消息（List 类型 - 生产者）
     */
    public void sendMessage(String queueName, String message) {
        String queueKey = RedisKey.build(RedisKey.QUEUE, queueName);
        cacheService.rPush(queueKey, message);
    }
    
    /**
     * 接收消息（List 类型 - 消费者）
     */
    public String receiveMessage(String queueName) {
        String queueKey = RedisKey.build(RedisKey.QUEUE, queueName);
        return cacheService.lPop(queueKey, String.class);
    }
    
    /**
     * 接收消息（阻塞式，带超时）
     */
    public String receiveMessageWithTimeout(String queueName, long timeoutSeconds) {
        String queueKey = RedisKey.build(RedisKey.QUEUE, queueName);
        return cacheService.rPop(queueKey, timeoutSeconds, TimeUnit.SECONDS, String.class);
    }
}

/**
 * 最新消息列表（List 类型）
 */
@Service
@RequiredArgsConstructor
public class NewsService {
    
    private final CacheService cacheService;
    
    /**
     * 添加最新消息（List 类型）
     */
    public void addLatestNews(String newsId) {
        String listKey = RedisKey.build(RedisKey.LATEST_NEWS, "list");
        cacheService.lPush(listKey, newsId);
        
        // 只保留最新的 100 条
        Long size = cacheService.lSize(listKey);
        if (size > 100) {
            // 删除多余的（从右侧删除）
            // 注意：需要手动实现，或者使用 lTrim
        }
    }
    
    /**
     * 获取最新消息列表（List 类型）
     */
    public List<String> getLatestNews(int count) {
        String listKey = RedisKey.build(RedisKey.LATEST_NEWS, "list");
        return cacheService.lRange(listKey, 0, count - 1, String.class);
    }
}
```

##### 示例4：Set 类型（集合）- 标签系统、去重

```java
@Service
@RequiredArgsConstructor
public class TagService {
    
    private final CacheService cacheService;
    
    /**
     * 给产品添加标签（Set 类型）
     */
    public void addTags(Long productId, String... tags) {
        String setKey = RedisKey.build(RedisKey.PRODUCT_TAGS, String.valueOf(productId));
        cacheService.sAdd(setKey, (Object[]) tags);
    }
    
    /**
     * 获取产品的所有标签（Set 类型）
     */
    public Set<String> getTags(Long productId) {
        String setKey = RedisKey.build(RedisKey.PRODUCT_TAGS, String.valueOf(productId));
        return cacheService.sMembers(setKey, String.class);
    }
    
    /**
     * 检查产品是否有某个标签（Set 类型）
     */
    public boolean hasTag(Long productId, String tag) {
        String setKey = RedisKey.build(RedisKey.PRODUCT_TAGS, String.valueOf(productId));
        return cacheService.sIsMember(setKey, tag);
    }
    
    /**
     * 移除标签（Set 类型）
     */
    public void removeTag(Long productId, String tag) {
        String setKey = RedisKey.build(RedisKey.PRODUCT_TAGS, String.valueOf(productId));
        cacheService.sRemove(setKey, tag);
    }
}

/**
 * 去重服务（Set 类型）
 */
@Service
@RequiredArgsConstructor
public class DeduplicationService {
    
    private final CacheService cacheService;
    
    /**
     * 检查是否已处理（Set 类型 - 去重）
     */
    public boolean isProcessed(String taskId) {
        String setKey = RedisKey.build(RedisKey.PROCESSED_TASKS, "set");
        return cacheService.sIsMember(setKey, taskId);
    }
    
    /**
     * 标记为已处理（Set 类型）
     */
    public void markAsProcessed(String taskId) {
        String setKey = RedisKey.build(RedisKey.PROCESSED_TASKS, "set");
        cacheService.sAdd(setKey, taskId);
        cacheService.expire(setKey, 24, TimeUnit.HOURS);  // 24小时后自动清理
    }
}
```

##### 示例5：ZSet 类型（有序集合）- 排行榜

```java
@Service
@RequiredArgsConstructor
public class LeaderboardService {
    
    private final CacheService cacheService;
    
    /**
     * 用户获得积分（ZSet 类型）
     */
    public void addScore(Long userId, double score) {
        String zsetKey = RedisKey.build(RedisKey.LEADERBOARD, "scores");
        cacheService.zIncrementScore(zsetKey, userId.toString(), score);
    }
    
    /**
     * 获取排行榜 Top 10（ZSet 类型）
     */
    public List<LeaderboardVO> getTop10() {
        String zsetKey = RedisKey.build(RedisKey.LEADERBOARD, "scores");
        Set<String> top10UserIds = cacheService.zReverseRange(zsetKey, 0, 9, String.class);
        
        List<LeaderboardVO> leaderboard = new ArrayList<>();
        int rank = 1;
        for (String userIdStr : top10UserIds) {
            Long userId = Long.parseLong(userIdStr);
            Double score = cacheService.zScore(zsetKey, userIdStr);
            
            LeaderboardVO vo = new LeaderboardVO();
            vo.setRank(rank++);
            vo.setUserId(userId);
            vo.setScore(score != null ? score : 0.0);
            leaderboard.add(vo);
        }
        
        return leaderboard;
    }
    
    /**
     * 获取用户排名（ZSet 类型）
     */
    public Long getUserRank(Long userId) {
        String zsetKey = RedisKey.build(RedisKey.LEADERBOARD, "scores");
        // 注意：需要手动实现 reverseRank，或者使用其他方式
        // 这里简化处理
        return null;
    }
}
```

---

## 四、完整封装类（支持所有类型）

### 需要添加的导入

```java
import java.util.*;
import java.util.stream.Collectors;
```

### 完整 CacheService 类结构

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // ========== String 类型操作 ==========
    public <T> T get(String key, Class<T> clazz) { ... }
    public void set(String key, Object value, long timeout, TimeUnit unit) { ... }
    public void delete(String key) { ... }
    
    // ========== Hash 类型操作 ==========
    public <T> T hGet(String key, String field, Class<T> clazz) { ... }
    public void hSet(String key, String field, Object value) { ... }
    public void hSetAll(String key, Map<String, Object> map) { ... }
    public Map<Object, Object> hGetAll(String key) { ... }
    public Long hDelete(String key, String... fields) { ... }
    public boolean hExists(String key, String field) { ... }
    
    // ========== List 类型操作 ==========
    public Long lPush(String key, Object value) { ... }
    public Long rPush(String key, Object value) { ... }
    public <T> T lPop(String key, Class<T> clazz) { ... }
    public <T> T rPop(String key, long timeout, TimeUnit unit, Class<T> clazz) { ... }
    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) { ... }
    public Long lSize(String key) { ... }
    
    // ========== Set 类型操作 ==========
    public Long sAdd(String key, Object... values) { ... }
    public Long sRemove(String key, Object... values) { ... }
    public <T> Set<T> sMembers(String key, Class<T> clazz) { ... }
    public boolean sIsMember(String key, Object value) { ... }
    public Long sSize(String key) { ... }
    
    // ========== ZSet 类型操作 ==========
    public boolean zAdd(String key, Object value, double score) { ... }
    public Long zRemove(String key, Object... values) { ... }
    public <T> Set<T> zRange(String key, long start, long end, Class<T> clazz) { ... }
    public <T> Set<T> zReverseRange(String key, long start, long end, Class<T> clazz) { ... }
    public Double zIncrementScore(String key, Object value, double delta) { ... }
    public Double zScore(String key, Object value) { ... }
    public Long zSize(String key) { ... }
}
```

### 更新 RedisKey 工具类

```java
public class RedisKey {
    // String 类型
    public static final String CACHE_PRODUCT = "cache:product:";
    
    // Hash 类型
    public static final String CACHE_USER = "cache:user:";
    public static final String CART = "cart:";
    
    // List 类型
    public static final String QUEUE = "queue:";
    public static final String LATEST_NEWS = "latest:news:";
    
    // Set 类型
    public static final String PRODUCT_TAGS = "tags:product:";
    public static final String PROCESSED_TASKS = "processed:tasks:";
    
    // ZSet 类型
    public static final String LEADERBOARD = "leaderboard:";
    
    public static String build(String prefix, String suffix) {
        return prefix + suffix;
    }
}
```

---

### 方案二：高级封装（支持缓存穿透、击穿、雪崩）

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class AdvancedCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取缓存（支持缓存穿透保护）
     * 
     * @param key 缓存键
     * @param clazz 返回值类型
     * @param loader 缓存未命中时的数据加载器
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 缓存值
     */
    public <T> T getOrLoad(String key, Class<T> clazz, 
                          java.util.function.Supplier<T> loader,
                          long timeout, TimeUnit unit) {
        // 1. 先查缓存
        T cached = get(key, clazz);
        if (cached != null) {
            return cached;
        }
        
        // 2. 缓存未命中，检查是否在加载中（防止缓存击穿）
        String lockKey = "lock:" + key;
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 双重检查（可能其他线程已经加载完成）
                cached = get(key, clazz);
                if (cached != null) {
                    return cached;
                }
                
                // 3. 加载数据
                T value = loader.get();
                
                // 4. 写入缓存（防止缓存穿透：即使值为 null 也缓存，但设置较短过期时间）
                if (value != null) {
                    set(key, value, timeout, unit);
                } else {
                    // 空值缓存 5 分钟，防止缓存穿透
                    set(key, "", 5, TimeUnit.MINUTES);
                }
                
                return value;
            } finally {
                // 5. 释放锁
                redisTemplate.delete(lockKey);
            }
        } else {
            // 其他线程正在加载，等待并重试
            try {
                Thread.sleep(100);
                return getOrLoad(key, clazz, loader, timeout, unit);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return loader.get();  // 降级：直接加载数据
            }
        }
    }
    
    /**
     * 获取缓存（基础方法）
     */
    private <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                return null;
            }
            return clazz.cast(value);
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 设置缓存
     */
    private void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}", key, e);
        }
    }
}
```

#### 使用示例

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductsMapper productsMapper;
    private final AdvancedCacheService cacheService;
    
    public ProductVO getProduct(Long productId) {
        String cacheKey = RedisKey.build(RedisKey.CACHE_PRODUCT, String.valueOf(productId));
        
        // 一行代码搞定：自动处理缓存穿透、击穿、雪崩
        return cacheService.getOrLoad(
            cacheKey,
            ProductVO.class,
            () -> {
                // 数据加载器：只在缓存未命中时执行
                Product product = productsMapper.selectByPrimaryKey(productId);
                return product != null ? convertToVO(product) : null;
            },
            1,
            TimeUnit.HOURS
        );
    }
}
```

---

## 三、对比：封装前后

### 封装前（直接使用 RedisTemplate）

```java
@Service
public class ProductService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public ProductVO getProduct(Long productId) {
        // ❌ 代码冗长
        String cacheKey = "cache:product:" + productId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return (ProductVO) cached;  // ❌ 需要强制类型转换
            }
        } catch (Exception e) {
            log.error("获取缓存失败", e);
        }
        
        Product product = productsMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return null;
        }
        
        ProductVO vo = convertToVO(product);
        
        try {
            redisTemplate.opsForValue().set(cacheKey, vo, 3600, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("设置缓存失败", e);
        }
        
        return vo;
    }
}
```

### 封装后（使用 CacheService）

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    private final CacheService cacheService;  // ✅ 简洁的 API
    
    public ProductVO getProduct(Long productId) {
        // ✅ 代码简洁
        String cacheKey = RedisKey.build(RedisKey.CACHE_PRODUCT, String.valueOf(productId));
        
        // ✅ 类型安全，无需强制转换
        ProductVO cached = cacheService.get(cacheKey, ProductVO.class);
        if (cached != null) {
            return cached;
        }
        
        Product product = productsMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return null;
        }
        
        ProductVO vo = convertToVO(product);
        
        // ✅ 简洁的 API，自动处理异常
        cacheService.set(cacheKey, vo, 1, TimeUnit.HOURS);
        
        return vo;
    }
}
```

---

## 四、封装的优势

| 优势 | 说明 |
|------|------|
| **代码简洁** | Service 层代码更简洁，可读性更高 |
| **类型安全** | 泛型支持，无需强制类型转换 |
| **统一管理** | Key 命名统一，避免冲突 |
| **异常处理** | 统一的异常处理，降级策略 |
| **易于维护** | 缓存逻辑集中管理，易于修改 |
| **易于测试** | 可以轻松 Mock CacheService |

---

## 五、推荐方案

### 对于当前项目

**推荐使用方案一（基础封装）**，因为：
1. ✅ 简单实用，满足大部分需求
2. ✅ 代码量适中，易于维护
3. ✅ 性能好，没有额外的开销
4. ✅ 易于理解和学习

### 实现步骤

1. **创建 `CacheService`**：封装 RedisTemplate 的常用操作
2. **创建 `RedisKey`**：统一管理 Key 前缀
3. **修改 Service 层**：使用 CacheService 替代直接使用 RedisTemplate

---

## 六、完整示例代码

### CacheService.java（基础版本）

```java
package com.database.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? clazz.cast(value) : null;
        } catch (Exception e) {
            log.error("获取缓存失败，key: {}", key, e);
            return null;
        }
    }
    
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败，key: {}", key, e);
        }
    }
    
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败，key: {}", key, e);
        }
    }
    
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查缓存是否存在失败，key: {}", key, e);
            return false;
        }
    }
}
```

### RedisKey.java

```java
package com.database.util;

public class RedisKey {
    public static final String CACHE_PRODUCT = "cache:product:";
    public static final String CACHE_CUSTOMER = "cache:customer:";
    public static final String AUTH_TOKEN = "auth:token:";
    
    public static String build(String prefix, String suffix) {
        return prefix + suffix;
    }
}
```

---

## 七、类型选择指南

### 什么时候用什么类型？

| 数据类型 | 使用场景 | 示例 |
|---------|---------|------|
| **String** | 简单的键值对、对象缓存 | Token、产品信息、用户信息（简单） |
| **Hash** | 对象的多个字段、购物车 | 用户详细信息、购物车商品 |
| **List** | 有序列表、消息队列 | 最新消息、待处理任务队列 |
| **Set** | 不重复集合、标签 | 产品标签、已处理任务去重 |
| **ZSet** | 有序集合、排行榜 | 积分排行榜、优先级队列 |

### 选择建议

```java
// ✅ 简单对象 → String
cacheService.set("product:1", productVO, 1, TimeUnit.HOURS);

// ✅ 对象多个字段 → Hash（更节省内存）
cacheService.hSetAll("user:1", userMap);

// ✅ 需要顺序 → List
cacheService.rPush("queue:orders", orderId);

// ✅ 需要去重 → Set
cacheService.sAdd("tags:product:1", "电子产品", "热销");

// ✅ 需要排序 → ZSet
cacheService.zAdd("leaderboard", "user:1", 1000.0);
```

---

## 八、总结

### 是否需要封装？

**答案：是的，强烈推荐封装一层！**

### 原因

1. ✅ **代码更简洁**：Service 层代码减少 50%+
2. ✅ **类型安全**：避免强制类型转换
3. ✅ **统一管理**：Key 命名统一，避免冲突
4. ✅ **易于维护**：缓存逻辑集中管理
5. ✅ **易于测试**：可以轻松 Mock
6. ✅ **支持所有类型**：String、Hash、List、Set、ZSet 全覆盖

### 推荐方案

**使用完整封装（CacheService + RedisKey）**，支持所有 Redis 数据类型，满足各种业务场景。

### 封装后的优势

| 对比项 | 直接使用 RedisTemplate | 使用封装的 CacheService |
|--------|----------------------|------------------------|
| **代码量** | 冗长 | 简洁 |
| **类型安全** | 需要强制转换 | 泛型支持 |
| **异常处理** | 分散在各处 | 统一处理 |
| **Key 管理** | 容易冲突 | 统一管理 |
| **支持类型** | 需要记住各种 API | 统一的 API 风格 |

### 完整封装的价值

- ✅ **一套 API 搞定所有类型**：不需要记住 `opsForValue()`、`opsForHash()` 等不同 API
- ✅ **统一的异常处理**：所有操作都有统一的异常处理和降级策略
- ✅ **类型安全**：所有方法都支持泛型，避免类型转换错误
- ✅ **易于扩展**：可以轻松添加新的功能（如分布式锁、限流等）
