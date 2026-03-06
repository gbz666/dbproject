# Redis opsValue 详解与 MySQL+Redis 配合方案

## 一、opsValue 是什么？

### 基本概念

`opsValue` 是 Spring Data Redis 提供的操作 Redis **String 类型**数据的接口。

```java
redisTemplate.opsForValue()  // 返回 ValueOperations<String, Object> 对象
```

### 为什么叫 "opsValue"？

- **ops** = Operations（操作）
- **Value** = Redis 的 String 类型（Redis 中最基本的数据类型）

### 常用操作

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// 1. 设置值（带过期时间）
redisTemplate.opsForValue().set("key", "value", 60, TimeUnit.SECONDS);

// 2. 获取值
Object value = redisTemplate.opsForValue().get("key");

// 3. 删除值
redisTemplate.delete("key");

// 4. 检查是否存在
Boolean exists = redisTemplate.hasKey("key");

// 5. 设置过期时间
redisTemplate.expire("key", 60, TimeUnit.SECONDS);

// 6. 获取剩余过期时间
Long expire = redisTemplate.getExpire("key", TimeUnit.SECONDS);
```

### Redis 的其他操作接口

```java
// String 类型（键值对）
redisTemplate.opsForValue()

// List 类型（列表）
redisTemplate.opsForList()

// Set 类型（集合）
redisTemplate.opsForSet()

// Hash 类型（哈希表）
redisTemplate.opsForHash()

// ZSet 类型（有序集合）
redisTemplate.opsForZSet()
```

---

## 二、opsValue 安全吗？

### ✅ 安全性分析

#### 1. **线程安全**
- ✅ `RedisTemplate` 是**线程安全**的
- ✅ 可以在多线程环境中使用
- ✅ Spring 容器管理的 Bean 默认单例，多个线程共享同一个实例是安全的

#### 2. **连接安全**
- ✅ 使用连接池（Lettuce），避免频繁创建连接
- ✅ 连接自动管理，异常时自动回收

#### 3. **数据安全**
- ⚠️ **Redis 默认不持久化**（需要配置）
- ⚠️ **内存数据**，服务器重启可能丢失
- ✅ 适合存储**临时数据**（如 Token、缓存）

### ⚠️ 潜在风险与解决方案

#### 风险1：数据丢失
**场景**：Redis 服务器重启，内存中的数据丢失

**解决方案**：
```yaml
# application.yaml
spring:
  data:
    redis:
      # 启用 RDB 持久化（Redis 配置文件中设置）
      # 或者使用 AOF 持久化
```

**建议**：
- Token 等临时数据：可以丢失（用户重新登录即可）
- 重要数据：应该存储在 MySQL 中，Redis 只做缓存

#### 风险2：内存溢出
**场景**：Redis 内存满了，可能导致服务不可用

**解决方案**：
```yaml
# Redis 配置（redis.conf）
maxmemory 2gb
maxmemory-policy allkeys-lru  # 内存满时删除最近最少使用的键
```

#### 风险3：并发问题
**场景**：多个请求同时操作同一个 Key

**解决方案**：使用 Redis 的原子操作或分布式锁

```java
// 示例：原子性递增
redisTemplate.opsForValue().increment("counter", 1);

// 示例：分布式锁
Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock:key", "value", 10, TimeUnit.SECONDS);
```

### ✅ 当前实现的安全性

```java
// TokenService.java 中的使用
redisTemplate.opsForValue().set(tokenKey, userId, expiration, TimeUnit.SECONDS);
```

**安全性评估**：
- ✅ **线程安全**：RedisTemplate 是线程安全的
- ✅ **过期自动清理**：设置了过期时间，自动删除
- ✅ **单点登录**：一个用户只能有一个有效 Token
- ⚠️ **数据丢失风险**：Redis 重启会丢失 Token（但这是可接受的，用户重新登录即可）

---

## 三、MySQL + Redis 配合使用方案

### 为什么需要配合？

| 场景 | MySQL | Redis |
|------|-------|-------|
| **持久化** | ✅ 持久化存储 | ⚠️ 内存存储（可配置持久化） |
| **查询速度** | ⚠️ 相对较慢 | ✅ 非常快 |
| **数据量** | ✅ 支持大数据量 | ⚠️ 受内存限制 |
| **成本** | ✅ 相对较低 | ⚠️ 内存成本较高 |
| **适用场景** | 永久数据、复杂查询 | 临时数据、热点数据、缓存 |

### 配合策略：Cache-Aside Pattern（旁路缓存）

```
┌─────────┐         ┌─────────┐         ┌─────────┐
│  前端   │ ──────> │  后端   │ ──────> │  Redis  │
└─────────┘         └─────────┘         └─────────┘
                            │
                            ▼
                       ┌─────────┐
                       │  MySQL  │
                       └─────────┘
```

### 实现方案

#### 方案一：查询缓存（Read-Through）

**场景**：频繁查询的产品、客户等数据

```java
@Service
public class ProductService {
    @Autowired
    private ProductsMapper productsMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_PREFIX = "cache:product:";
    private static final long CACHE_EXPIRE = 3600; // 1小时
    
    /**
     * 查询产品（带缓存）
     */
    public ProductVO getProduct(Long productId) {
        String cacheKey = CACHE_PREFIX + productId;
        
        // 1. 先查 Redis
        ProductVO product = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
        if (product != null) {
            log.debug("从缓存获取产品: {}", productId);
            return product;
        }
        
        // 2. Redis 没有，查 MySQL
        Product productEntity = productsMapper.selectByPrimaryKey(productId);
        if (productEntity == null) {
            return null;
        }
        
        // 3. 转换为 VO
        product = convertToVO(productEntity);
        
        // 4. 写入 Redis（下次查询直接从缓存获取）
        redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE, TimeUnit.SECONDS);
        log.debug("从数据库获取产品并缓存: {}", productId);
        
        return product;
    }
    
    /**
     * 更新产品（同时更新缓存）
     */
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        // 1. 更新 MySQL
        productsMapper.updateByPrimaryKeySelective(convertToEntity(request));
        
        // 2. 删除缓存（下次查询时重新加载）
        String cacheKey = CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
        
        log.info("更新产品并清除缓存: {}", productId);
    }
}
```

#### 方案二：写时更新（Write-Through）

**场景**：更新数据时同时更新缓存

```java
/**
 * 更新产品（同时更新 MySQL 和 Redis）
 */
public void updateProduct(Long productId, ProductUpdateRequest request) {
    // 1. 更新 MySQL
    Product product = productsMapper.selectByPrimaryKey(productId);
    // ... 更新逻辑 ...
    productsMapper.updateByPrimaryKeySelective(product);
    
    // 2. 更新 Redis（保持数据一致性）
    String cacheKey = CACHE_PREFIX + productId;
    ProductVO productVO = convertToVO(product);
    redisTemplate.opsForValue().set(cacheKey, productVO, CACHE_EXPIRE, TimeUnit.SECONDS);
    
    log.info("更新产品并同步缓存: {}", productId);
}
```

#### 方案三：删除时清除缓存（Write-Behind）

**场景**：删除数据时清除缓存

```java
/**
 * 删除产品（同时删除 MySQL 和 Redis）
 */
public void deleteProduct(Long productId) {
    // 1. 删除 MySQL
    productsMapper.deleteByPrimaryKey(productId);
    
    // 2. 删除 Redis
    String cacheKey = CACHE_PREFIX + productId;
    redisTemplate.delete(cacheKey);
    
    log.info("删除产品并清除缓存: {}", productId);
}
```

### 完整示例：产品服务 + 缓存

```java
@Service
@Slf4j
public class ProductServiceWithCache {
    
    @Autowired
    private ProductsMapper productsMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_PREFIX = "cache:product:";
    private static final long CACHE_EXPIRE = 3600; // 1小时
    
    /**
     * 查询产品（带缓存）
     */
    public ProductVO getProduct(Long productId) {
        String cacheKey = CACHE_PREFIX + productId;
        
        // 1. 先查 Redis
        ProductVO cached = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("缓存命中: productId={}", productId);
            return cached;
        }
        
        // 2. 缓存未命中，查 MySQL
        Product product = productsMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return null;
        }
        
        ProductVO vo = convertToVO(product);
        
        // 3. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
        log.debug("缓存未命中，从数据库加载并缓存: productId={}", productId);
        
        return vo;
    }
    
    /**
     * 创建产品
     */
    public void createProduct(ProductCreateRequest request) {
        // 1. 插入 MySQL
        Product product = convertToEntity(request);
        productsMapper.insert(product);
        
        // 2. 不需要立即缓存（等查询时再缓存）
        // 或者可以选择立即缓存
        // String cacheKey = CACHE_PREFIX + product.getId();
        // redisTemplate.opsForValue().set(cacheKey, convertToVO(product), CACHE_EXPIRE, TimeUnit.SECONDS);
        
        log.info("创建产品: productId={}", product.getId());
    }
    
    /**
     * 更新产品
     */
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        // 1. 更新 MySQL
        Product product = productsMapper.selectByPrimaryKey(productId);
        // ... 更新字段 ...
        productsMapper.updateByPrimaryKeySelective(product);
        
        // 2. 删除缓存（下次查询时重新加载）
        String cacheKey = CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
        
        log.info("更新产品并清除缓存: productId={}", productId);
    }
    
    /**
     * 删除产品
     */
    public void deleteProduct(Long productId) {
        // 1. 删除 MySQL
        productsMapper.deleteByPrimaryKey(productId);
        
        // 2. 删除缓存
        String cacheKey = CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
        
        log.info("删除产品并清除缓存: productId={}", productId);
    }
    
    /**
     * 批量查询产品（可以缓存整个列表）
     */
    public List<ProductVO> getProducts(List<Long> productIds) {
        List<ProductVO> result = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();
        
        // 1. 批量从 Redis 获取
        for (Long id : productIds) {
            String cacheKey = CACHE_PREFIX + id;
            ProductVO cached = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                result.add(cached);
            } else {
                missingIds.add(id);
            }
        }
        
        // 2. 从 MySQL 查询缺失的数据
        if (!missingIds.isEmpty()) {
            List<Product> products = productsMapper.selectByIds(missingIds);
            for (Product product : products) {
                ProductVO vo = convertToVO(product);
                result.add(vo);
                
                // 3. 写入缓存
                String cacheKey = CACHE_PREFIX + product.getId();
                redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
            }
        }
        
        return result;
    }
    
    private ProductVO convertToVO(Product product) {
        // 转换逻辑...
        return new ProductVO();
    }
    
    private Product convertToEntity(ProductCreateRequest request) {
        // 转换逻辑...
        return new Product();
    }
}
```

### 缓存更新策略对比

| 策略 | 优点 | 缺点 | 适用场景 |
|------|------|------|---------|
| **Cache-Aside** | 简单、灵活 | 可能出现缓存不一致 | 读多写少 |
| **Write-Through** | 数据一致性好 | 写操作较慢 | 写多读少 |
| **Write-Behind** | 写操作快 | 可能丢失数据 | 对一致性要求不高 |

---

## 四、最佳实践建议

### 1. 数据分类

```java
// 永久数据 → MySQL
- 用户信息
- 订单记录
- 财务数据

// 临时数据 → Redis
- Token
- 会话信息
- 验证码

// 热点数据 → MySQL + Redis（缓存）
- 产品信息（频繁查询）
- 客户信息（频繁查询）
- 库存信息（实时性要求高）
```

### 2. 缓存 Key 命名规范

```java
public class RedisKeyPrefix {
    // 认证相关
    public static final String AUTH_TOKEN = "auth:token:";
    
    // 缓存相关
    public static final String CACHE_PRODUCT = "cache:product:";
    public static final String CACHE_CUSTOMER = "cache:customer:";
    
    // 会话相关
    public static final String SESSION = "session:";
}
```

### 3. 缓存过期时间设置

```java
// 短期缓存（1小时）
private static final long SHORT_CACHE = 3600;

// 中期缓存（1天）
private static final long MEDIUM_CACHE = 86400;

// 长期缓存（7天）
private static final long LONG_CACHE = 604800;
```

### 4. 异常处理

```java
public ProductVO getProduct(Long productId) {
    try {
        // Redis 操作
        ProductVO cached = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
    } catch (Exception e) {
        log.error("Redis 操作失败，降级到数据库查询", e);
        // 降级：直接查数据库
    }
    
    // 数据库查询
    return productsMapper.selectByPrimaryKey(productId);
}
```

---

## 五、总结

### opsValue 安全性

| 方面 | 安全性 |
|------|--------|
| **线程安全** | ✅ 安全 |
| **连接管理** | ✅ 安全（连接池） |
| **数据持久化** | ⚠️ 需配置（默认不持久化） |
| **内存管理** | ⚠️ 需配置过期策略 |

### MySQL + Redis 配合

| 数据 | 存储位置 | 原因 |
|------|---------|------|
| **Token** | Redis | 临时数据，需要过期 |
| **用户信息** | MySQL | 永久数据 |
| **产品信息** | MySQL + Redis | 热点数据，缓存加速 |
| **订单记录** | MySQL | 永久数据，需要事务 |

**核心原则**：
- ✅ MySQL 作为**数据源**（Source of Truth）
- ✅ Redis 作为**缓存层**（加速查询）
- ✅ 重要数据必须存储在 MySQL
- ✅ Redis 只存储临时数据或缓存
