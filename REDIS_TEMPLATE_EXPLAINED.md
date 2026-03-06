# RedisTemplate 详解

## 一、RedisTemplate 是什么？

### 基本概念

`RedisTemplate` 是 Spring Data Redis 提供的**核心工具类**，用于操作 Redis 数据库。

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;
```

### 作用

- ✅ **封装 Redis 操作**：简化 Redis 命令的使用
- ✅ **序列化支持**：自动处理 Java 对象与 Redis 数据的转换
- ✅ **连接管理**：自动管理 Redis 连接（使用连接池）
- ✅ **异常处理**：统一的异常处理机制

---

## 二、RedisTemplate 的结构

### 泛型参数

```java
RedisTemplate<K, V>
```

- **K**：Key 的类型（通常是 `String`）
- **V**：Value 的类型（可以是 `Object`、`String`、自定义类型等）

### 常用类型

```java
// 最常用：Key 是 String，Value 是 Object（可以存储任何类型）
RedisTemplate<String, Object> redisTemplate;

// Key 和 Value 都是 String
RedisTemplate<String, String> stringRedisTemplate;

// Key 是 String，Value 是自定义类型
RedisTemplate<String, User> userRedisTemplate;
```

---

## 三、RedisTemplate 的操作接口

### 1. opsForValue() - 操作 String 类型（键值对）

**最常用**，用于存储简单的键值对。

```java
// 设置值
redisTemplate.opsForValue().set("key", "value");

// 设置值（带过期时间）
redisTemplate.opsForValue().set("key", "value", 60, TimeUnit.SECONDS);

// 获取值
Object value = redisTemplate.opsForValue().get("key");

// 删除值
redisTemplate.delete("key");

// 检查是否存在
Boolean exists = redisTemplate.hasKey("key");

// 设置过期时间
redisTemplate.expire("key", 60, TimeUnit.SECONDS);

// 获取剩余过期时间
Long expire = redisTemplate.getExpire("key", TimeUnit.SECONDS);

// 原子性递增
Long count = redisTemplate.opsForValue().increment("counter", 1);

// 原子性递减
Long count = redisTemplate.opsForValue().decrement("counter", 1);
```

**实际应用示例**（Token 存储）：
```java
// TokenService.java 中的使用
String tokenKey = "token:" + token;
redisTemplate.opsForValue().set(tokenKey, userId, expiration, TimeUnit.SECONDS);
```

---

### 2. opsForList() - 操作 List 类型（列表）

用于存储有序的列表数据。

```java
// 从左侧推入
redisTemplate.opsForList().leftPush("list", "value1");
redisTemplate.opsForList().leftPush("list", "value2");

// 从右侧推入
redisTemplate.opsForList().rightPush("list", "value3");

// 获取列表长度
Long size = redisTemplate.opsForList().size("list");

// 获取指定范围的元素
List<Object> list = redisTemplate.opsForList().range("list", 0, -1);

// 从左侧弹出
Object value = redisTemplate.opsForList().leftPop("list");

// 从右侧弹出
Object value = redisTemplate.opsForList().rightPop("list");
```

**实际应用示例**（消息队列）：
```java
// 消息队列：生产者
redisTemplate.opsForList().rightPush("queue:orders", orderId);

// 消息队列：消费者
Object orderId = redisTemplate.opsForList().leftPop("queue:orders", 10, TimeUnit.SECONDS);
```

---

### 3. opsForSet() - 操作 Set 类型（集合）

用于存储**不重复**的无序集合。

```java
// 添加元素
redisTemplate.opsForSet().add("set", "value1", "value2", "value3");

// 获取所有元素
Set<Object> set = redisTemplate.opsForSet().members("set");

// 判断元素是否存在
Boolean exists = redisTemplate.opsForSet().isMember("set", "value1");

// 获取集合大小
Long size = redisTemplate.opsForSet().size("set");

// 移除元素
Long removed = redisTemplate.opsForSet().remove("set", "value1");

// 求交集
Set<Object> intersection = redisTemplate.opsForSet().intersect("set1", "set2");

// 求并集
Set<Object> union = redisTemplate.opsForSet().union("set1", "set2");

// 求差集
Set<Object> difference = redisTemplate.opsForSet().difference("set1", "set2");
```

**实际应用示例**（标签系统）：
```java
// 给产品添加标签
redisTemplate.opsForSet().add("product:1:tags", "电子产品", "热销", "推荐");

// 获取产品的所有标签
Set<Object> tags = redisTemplate.opsForSet().members("product:1:tags");

// 判断产品是否有某个标签
Boolean hasTag = redisTemplate.opsForSet().isMember("product:1:tags", "热销");
```

---

### 4. opsForHash() - 操作 Hash 类型（哈希表）

用于存储**字段-值**的映射关系（类似 Java 的 Map）。

```java
// 设置字段值
redisTemplate.opsForHash().put("hash", "field1", "value1");
redisTemplate.opsForHash().put("hash", "field2", "value2");

// 获取字段值
Object value = redisTemplate.opsForHash().get("hash", "field1");

// 获取所有字段和值
Map<Object, Object> map = redisTemplate.opsForHash().entries("hash");

// 获取所有字段
Set<Object> fields = redisTemplate.opsForHash().keys("hash");

// 获取所有值
List<Object> values = redisTemplate.opsForHash().values("hash");

// 判断字段是否存在
Boolean exists = redisTemplate.opsForHash().hasKey("hash", "field1");

// 删除字段
Long deleted = redisTemplate.opsForHash().delete("hash", "field1");

// 获取哈希表大小
Long size = redisTemplate.opsForHash().size("hash");
```

**实际应用示例**（用户信息存储）：
```java
// 存储用户信息
Map<String, Object> userInfo = new HashMap<>();
userInfo.put("username", "admin");
userInfo.put("email", "admin@example.com");
userInfo.put("phone", "13800138000");
redisTemplate.opsForHash().putAll("user:1", userInfo);

// 获取用户信息
Map<Object, Object> userInfo = redisTemplate.opsForHash().entries("user:1");

// 更新单个字段
redisTemplate.opsForHash().put("user:1", "email", "newemail@example.com");
```

---

### 5. opsForZSet() - 操作 ZSet 类型（有序集合）

用于存储**有序且不重复**的集合（带分数，可以排序）。

```java
// 添加元素（带分数）
redisTemplate.opsForZSet().add("zset", "value1", 10.0);
redisTemplate.opsForZSet().add("zset", "value2", 20.0);
redisTemplate.opsForZSet().add("zset", "value3", 15.0);

// 获取元素分数
Double score = redisTemplate.opsForZSet().score("zset", "value1");

// 获取排名（从低到高）
Long rank = redisTemplate.opsForZSet().rank("zset", "value1");

// 获取排名（从高到低）
Long rank = redisTemplate.opsForZSet().reverseRank("zset", "value1");

// 获取指定范围的元素（按分数排序）
Set<Object> set = redisTemplate.opsForZSet().range("zset", 0, -1);

// 获取指定分数范围的元素
Set<Object> set = redisTemplate.opsForZSet().rangeByScore("zset", 10.0, 20.0);

// 增加分数
Double newScore = redisTemplate.opsForZSet().incrementScore("zset", "value1", 5.0);

// 移除元素
Long removed = redisTemplate.opsForZSet().remove("zset", "value1");
```

**实际应用示例**（排行榜）：
```java
// 用户积分排行榜
redisTemplate.opsForZSet().add("leaderboard", "user:1", 1000.0);
redisTemplate.opsForZSet().add("leaderboard", "user:2", 2000.0);
redisTemplate.opsForZSet().add("leaderboard", "user:3", 1500.0);

// 获取排行榜 Top 10
Set<Object> top10 = redisTemplate.opsForZSet().reverseRange("leaderboard", 0, 9);

// 用户获得积分
redisTemplate.opsForZSet().incrementScore("leaderboard", "user:1", 50.0);
```

---

## 四、RedisTemplate 的配置

### 当前项目的配置

```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 使用 GenericJackson2JsonRedisSerializer 序列化 value
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        
        // 使用 StringRedisSerializer 序列化 key
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // 配置序列化器
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

### 配置说明

| 配置项 | 作用 | 说明 |
|--------|------|------|
| `setConnectionFactory` | 设置连接工厂 | 指定 Redis 连接信息 |
| `setKeySerializer` | Key 序列化器 | 通常使用 `StringRedisSerializer` |
| `setValueSerializer` | Value 序列化器 | 可以使用 JSON、JDK 序列化等 |
| `setHashKeySerializer` | Hash Key 序列化器 | Hash 类型的 Key 序列化 |
| `setHashValueSerializer` | Hash Value 序列化器 | Hash 类型的 Value 序列化 |

---

## 五、序列化器对比

### 1. StringRedisSerializer（字符串序列化）

**特点**：
- ✅ 只能存储字符串
- ✅ 性能最好
- ✅ 可读性强（Redis 中可以直接看到）

**使用场景**：
- Key 的序列化（推荐）
- 简单的字符串值

```java
StringRedisSerializer serializer = new StringRedisSerializer();
template.setKeySerializer(serializer);
template.setValueSerializer(serializer);
```

---

### 2. GenericJackson2JsonRedisSerializer（JSON 序列化）

**特点**：
- ✅ 可以存储任何 Java 对象
- ✅ 自动处理类型信息
- ✅ 可读性较好（JSON 格式）

**使用场景**：
- Value 的序列化（推荐）
- 存储复杂对象

```java
GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
template.setValueSerializer(serializer);
```

---

### 3. JdkSerializationRedisSerializer（JDK 序列化）

**特点**：
- ✅ 可以存储任何 Java 对象
- ❌ 性能较差
- ❌ 可读性差（二进制格式）
- ❌ 占用空间大

**使用场景**：
- 不推荐使用（性能差）

---

## 六、实际应用示例

### 示例1：Token 存储（当前项目）

```java
@Service
public class TokenService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 存储 Token
    public void saveToken(String token, Long userId, long expiration) {
        String key = "token:" + token;
        redisTemplate.opsForValue().set(key, userId, expiration, TimeUnit.SECONDS);
    }
    
    // 获取 Token 对应的用户ID
    public Long getUserIdByToken(String token) {
        String key = "token:" + token;
        return (Long) redisTemplate.opsForValue().get(key);
    }
    
    // 删除 Token
    public void deleteToken(String token) {
        String key = "token:" + token;
        redisTemplate.delete(key);
    }
}
```

---

### 示例2：产品缓存

```java
@Service
public class ProductCacheService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存产品
    public void cacheProduct(Long productId, ProductVO product) {
        String key = "cache:product:" + productId;
        redisTemplate.opsForValue().set(key, product, 1, TimeUnit.HOURS);
    }
    
    // 获取缓存的产品
    public ProductVO getCachedProduct(Long productId) {
        String key = "cache:product:" + productId;
        return (ProductVO) redisTemplate.opsForValue().get(key);
    }
    
    // 清除缓存
    public void clearCache(Long productId) {
        String key = "cache:product:" + productId;
        redisTemplate.delete(key);
    }
}
```

---

### 示例3：购物车（使用 Hash）

```java
@Service
public class CartService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 添加商品到购物车
    public void addToCart(Long userId, Long productId, Integer quantity) {
        String key = "cart:" + userId;
        redisTemplate.opsForHash().put(key, productId.toString(), quantity);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);  // 7天过期
    }
    
    // 获取购物车
    public Map<Object, Object> getCart(Long userId) {
        String key = "cart:" + userId;
        return redisTemplate.opsForHash().entries(key);
    }
    
    // 更新商品数量
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        String key = "cart:" + userId;
        redisTemplate.opsForHash().put(key, productId.toString(), quantity);
    }
    
    // 删除商品
    public void removeFromCart(Long userId, Long productId) {
        String key = "cart:" + userId;
        redisTemplate.opsForHash().delete(key, productId.toString());
    }
}
```

---

### 示例4：消息队列（使用 List）

```java
@Service
public class MessageQueueService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 发送消息（生产者）
    public void sendMessage(String queueName, Object message) {
        redisTemplate.opsForList().rightPush("queue:" + queueName, message);
    }
    
    // 接收消息（消费者）
    public Object receiveMessage(String queueName, long timeout) {
        return redisTemplate.opsForList().leftPop("queue:" + queueName, timeout, TimeUnit.SECONDS);
    }
    
    // 获取队列长度
    public Long getQueueLength(String queueName) {
        return redisTemplate.opsForList().size("queue:" + queueName);
    }
}
```

---

### 示例5：分布式锁

```java
@Service
public class DistributedLockService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 获取锁
    public boolean tryLock(String lockKey, String lockValue, long expireTime) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(
            "lock:" + lockKey, 
            lockValue, 
            expireTime, 
            TimeUnit.SECONDS
        );
        return Boolean.TRUE.equals(result);
    }
    
    // 释放锁
    public void releaseLock(String lockKey, String lockValue) {
        String key = "lock:" + lockKey;
        String value = (String) redisTemplate.opsForValue().get(key);
        if (lockValue.equals(value)) {
            redisTemplate.delete(key);
        }
    }
}
```

---

## 七、常用操作总结

### 通用操作

```java
// 删除键
redisTemplate.delete("key");

// 批量删除
redisTemplate.delete(Arrays.asList("key1", "key2", "key3"));

// 检查键是否存在
Boolean exists = redisTemplate.hasKey("key");

// 设置过期时间
redisTemplate.expire("key", 60, TimeUnit.SECONDS);

// 获取剩余过期时间
Long expire = redisTemplate.getExpire("key", TimeUnit.SECONDS);

// 移除过期时间（永久保存）
redisTemplate.persist("key");

// 获取所有匹配的键
Set<String> keys = redisTemplate.keys("pattern:*");
```

### 批量操作

```java
// 批量设置
Map<String, Object> map = new HashMap<>();
map.put("key1", "value1");
map.put("key2", "value2");
redisTemplate.opsForValue().multiSet(map);

// 批量获取
List<Object> values = redisTemplate.opsForValue().multiGet(Arrays.asList("key1", "key2"));
```

---

## 八、注意事项

### 1. 序列化问题

**问题**：如果 Key 和 Value 的序列化器不一致，可能导致数据无法读取。

**解决**：统一配置序列化器。

```java
// ✅ 正确：Key 和 Value 使用合适的序列化器
template.setKeySerializer(new StringRedisSerializer());
template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
```

---

### 2. 类型转换问题

**问题**：从 Redis 获取的数据需要强制类型转换。

**解决**：使用泛型或类型检查。

```java
// ⚠️ 需要类型转换
ProductVO product = (ProductVO) redisTemplate.opsForValue().get("key");

// ✅ 更好的方式：使用泛型方法
public <T> T get(String key, Class<T> clazz) {
    Object value = redisTemplate.opsForValue().get(key);
    return clazz.cast(value);
}
```

---

### 3. 连接管理

**问题**：频繁创建 RedisTemplate 会导致连接泄漏。

**解决**：使用 Spring 依赖注入，让 Spring 管理生命周期。

```java
// ✅ 正确：使用 @Autowired
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// ❌ 错误：手动创建
RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
```

---

### 4. 异常处理

**问题**：Redis 连接失败时可能抛出异常。

**解决**：添加异常处理。

```java
try {
    redisTemplate.opsForValue().set("key", "value");
} catch (Exception e) {
    log.error("Redis 操作失败", e);
    // 降级处理：直接查数据库
}
```

---

## 九、最佳实践

### 1. Key 命名规范

```java
// ✅ 使用前缀和冒号分隔
"auth:token:xxx"
"cache:product:123"
"session:user:456"

// ❌ 避免无意义的 Key
"key1"
"data"
```

### 2. 过期时间设置

```java
// ✅ 总是设置过期时间（避免内存泄漏）
redisTemplate.opsForValue().set("key", "value", 3600, TimeUnit.SECONDS);

// ❌ 不设置过期时间（可能导致内存问题）
redisTemplate.opsForValue().set("key", "value");
```

### 3. 批量操作

```java
// ✅ 使用批量操作（性能更好）
redisTemplate.opsForValue().multiSet(map);

// ❌ 循环单个操作（性能差）
for (Map.Entry<String, Object> entry : map.entrySet()) {
    redisTemplate.opsForValue().set(entry.getKey(), entry.getValue());
}
```

---

## 十、总结

### RedisTemplate 的核心特点

| 特点 | 说明 |
|------|------|
| **封装** | 简化 Redis 命令的使用 |
| **序列化** | 自动处理 Java 对象与 Redis 数据的转换 |
| **连接管理** | 自动管理连接（使用连接池） |
| **类型支持** | 支持 String、List、Set、Hash、ZSet 等类型 |

### 常用操作接口

| 接口 | 用途 | 使用场景 |
|------|------|---------|
| `opsForValue()` | 键值对 | Token、缓存、计数器 |
| `opsForList()` | 列表 | 消息队列、最新列表 |
| `opsForSet()` | 集合 | 标签、去重 |
| `opsForHash()` | 哈希表 | 用户信息、购物车 |
| `opsForZSet()` | 有序集合 | 排行榜、优先级队列 |

### 推荐配置

```java
// Key：String 序列化
template.setKeySerializer(new StringRedisSerializer());

// Value：JSON 序列化
template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
```

RedisTemplate 是操作 Redis 的核心工具，掌握它就能轻松使用 Redis 的各种功能！
