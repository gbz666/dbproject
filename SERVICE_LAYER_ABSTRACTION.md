# Service 层抽象化方案：忽略 Redis 和 MySQL 细节

## 问题：如何让 Service 层不关心底层存储？

### 目标
- ✅ Service 层代码简洁，不直接操作 Redis/MySQL
- ✅ 可以轻松切换存储方式
- ✅ 代码可维护性高

---

## 方案一：Spring Cache 抽象（推荐，最简单）

### 原理
使用 Spring Cache 注解，自动处理缓存逻辑，Service 层完全不需要关心缓存细节。

### 实现步骤

#### 1. 添加依赖（已添加）
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### 2. 启用缓存
```java
@SpringBootApplication
@EnableCaching  // 启用缓存
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
```

#### 3. 配置缓存管理器
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))  // 默认过期时间 1 小时
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}
```

#### 4. Service 层使用（完全忽略 Redis/MySQL）

```java
@Service
public class ProductService {
    
    @Autowired
    private ProductsMapper productsMapper;
    
    /**
     * 查询产品 - Service 层完全不需要关心缓存
     * @Cacheable: 先查缓存，没有则执行方法并缓存结果
     */
    @Cacheable(value = "products", key = "#productId")
    public ProductVO getProduct(Long productId) {
        // 只关心业务逻辑，不关心缓存
        Product product = productsMapper.selectByPrimaryKey(productId);
        return convertToVO(product);
    }
    
    /**
     * 更新产品 - 自动清除缓存
     * @CacheEvict: 更新时清除缓存
     */
    @CacheEvict(value = "products", key = "#productId")
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        // 只关心业务逻辑
        productsMapper.updateByPrimaryKeySelective(convertToEntity(request));
    }
    
    /**
     * 删除产品 - 自动清除缓存
     */
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(Long productId) {
        productsMapper.deleteByPrimaryKey(productId);
    }
    
    /**
     * 创建产品 - 可以选择是否缓存
     */
    public void createProduct(ProductCreateRequest request) {
        productsMapper.insert(convertToEntity(request));
        // 不缓存，等查询时再缓存
    }
}
```

### 优点
- ✅ **Service 层代码极简**：只需要添加注解
- ✅ **自动处理缓存**：Spring 自动处理缓存逻辑
- ✅ **可配置**：可以轻松切换缓存实现（Redis、Caffeine、内存等）
- ✅ **无需修改代码**：切换缓存实现只需修改配置

---

## 方案二：Repository 模式（更灵活）

### 原理
定义抽象的数据访问接口，Service 层只依赖接口，不关心底层实现。

### 实现步骤

#### 1. 定义 Repository 接口

```java
/**
 * 产品数据访问接口
 * Service 层只依赖这个接口，不关心底层是 MySQL 还是 Redis
 */
public interface ProductRepository {
    /**
     * 查询产品（自动处理缓存）
     */
    ProductVO findById(Long productId);
    
    /**
     * 保存产品（自动处理缓存）
     */
    void save(ProductVO product);
    
    /**
     * 更新产品（自动处理缓存）
     */
    void update(Long productId, ProductVO product);
    
    /**
     * 删除产品（自动清除缓存）
     */
    void delete(Long productId);
}
```

#### 2. 实现 Repository（处理 MySQL + Redis）

```java
@Repository
public class ProductRepositoryImpl implements ProductRepository {
    
    @Autowired
    private ProductsMapper productsMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_PREFIX = "cache:product:";
    private static final long CACHE_EXPIRE = 3600;
    
    @Override
    public ProductVO findById(Long productId) {
        String cacheKey = CACHE_PREFIX + productId;
        
        // 1. 先查缓存
        ProductVO cached = (ProductVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 2. 缓存未命中，查数据库
        Product product = productsMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return null;
        }
        
        ProductVO vo = convertToVO(product);
        
        // 3. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_EXPIRE, TimeUnit.SECONDS);
        
        return vo;
    }
    
    @Override
    public void save(ProductVO product) {
        // 保存到数据库
        Product entity = convertToEntity(product);
        productsMapper.insert(entity);
        
        // 可以选择是否立即缓存
        // String cacheKey = CACHE_PREFIX + entity.getId();
        // redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE, TimeUnit.SECONDS);
    }
    
    @Override
    public void update(Long productId, ProductVO product) {
        // 更新数据库
        Product entity = convertToEntity(product);
        productsMapper.updateByPrimaryKeySelective(entity);
        
        // 清除缓存
        String cacheKey = CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
    }
    
    @Override
    public void delete(Long productId) {
        // 删除数据库
        productsMapper.deleteByPrimaryKey(productId);
        
        // 清除缓存
        String cacheKey = CACHE_PREFIX + productId;
        redisTemplate.delete(cacheKey);
    }
    
    private ProductVO convertToVO(Product product) {
        // 转换逻辑...
        return new ProductVO();
    }
    
    private Product convertToEntity(ProductVO vo) {
        // 转换逻辑...
        return new Product();
    }
}
```

#### 3. Service 层使用（完全忽略底层）

```java
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;  // 只依赖接口
    
    /**
     * Service 层完全不需要知道底层是 Redis 还是 MySQL
     */
    public ProductVO getProduct(Long productId) {
        // 只关心业务逻辑
        return productRepository.findById(productId);
    }
    
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        ProductVO product = productRepository.findById(productId);
        // ... 业务逻辑处理 ...
        productRepository.update(productId, product);
    }
    
    public void deleteProduct(Long productId) {
        productRepository.delete(productId);
    }
}
```

### 优点
- ✅ **完全解耦**：Service 层完全不关心底层实现
- ✅ **易于测试**：可以轻松 Mock Repository
- ✅ **灵活切换**：可以轻松切换不同的 Repository 实现

---

## 方案三：组合模式（当前项目推荐）

### 原理
创建一个统一的缓存服务，封装所有 Redis 操作，Service 层通过这个服务访问缓存。

### 实现步骤

#### 1. 创建统一的缓存服务

```java
/**
 * 统一缓存服务
 * 封装所有 Redis 操作，Service 层通过这个服务访问缓存
 */
@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }
    
    /**
     * 设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    
    /**
     * 检查是否存在
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 获取剩余过期时间
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }
}
```

#### 2. Service 层使用

```java
@Service
public class ProductService {
    
    @Autowired
    private ProductsMapper productsMapper;
    
    @Autowired
    private CacheService cacheService;  // 统一的缓存服务
    
    private static final String CACHE_PREFIX = "cache:product:";
    
    public ProductVO getProduct(Long productId) {
        String cacheKey = CACHE_PREFIX + productId;
        
        // 使用统一的缓存服务，不需要直接操作 RedisTemplate
        ProductVO cached = cacheService.get(cacheKey, ProductVO.class);
        if (cached != null) {
            return cached;
        }
        
        // 查数据库
        Product product = productsMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return null;
        }
        
        ProductVO vo = convertToVO(product);
        
        // 写入缓存
        cacheService.set(cacheKey, vo, 3600, TimeUnit.SECONDS);
        
        return vo;
    }
    
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        // 更新数据库
        productsMapper.updateByPrimaryKeySelective(convertToEntity(request));
        
        // 清除缓存
        cacheService.delete(CACHE_PREFIX + productId);
    }
}
```

### 优点
- ✅ **简化代码**：不需要直接操作 RedisTemplate
- ✅ **统一管理**：所有缓存操作在一个地方
- ✅ **易于扩展**：可以轻松添加缓存统计、监控等功能

---

## 方案四：AOP 切面（最优雅，但复杂）

### 原理
使用 AOP 切面自动处理缓存逻辑，Service 层完全不需要关心缓存。

### 实现步骤

#### 1. 定义缓存注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
    String value();  // 缓存 key 前缀
    String key() default "";  // 缓存 key
    long expire() default 3600;  // 过期时间（秒）
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {
    String value();  // 缓存 key 前缀
    String key() default "";  // 缓存 key
}
```

#### 2. 实现 AOP 切面

```java
@Aspect
@Component
public class CacheAspect {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 缓存查询切面
     */
    @Around("@annotation(cacheable)")
    public Object aroundCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        // 构建缓存 key
        String cacheKey = buildCacheKey(cacheable.value(), cacheable.key(), joinPoint.getArgs());
        
        // 先查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 缓存未命中，执行方法
        Object result = joinPoint.proceed();
        
        // 写入缓存
        if (result != null) {
            redisTemplate.opsForValue().set(cacheKey, result, cacheable.expire(), TimeUnit.SECONDS);
        }
        
        return result;
    }
    
    /**
     * 缓存清除切面
     */
    @Around("@annotation(cacheEvict)")
    public Object aroundCacheEvict(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) throws Throwable {
        // 执行方法
        Object result = joinPoint.proceed();
        
        // 清除缓存
        String cacheKey = buildCacheKey(cacheEvict.value(), cacheEvict.key(), joinPoint.getArgs());
        redisTemplate.delete(cacheKey);
        
        return result;
    }
    
    private String buildCacheKey(String prefix, String key, Object[] args) {
        // 构建缓存 key 的逻辑...
        return prefix + ":" + key;
    }
}
```

#### 3. Service 层使用（完全忽略缓存）

```java
@Service
public class ProductService {
    
    @Autowired
    private ProductsMapper productsMapper;
    
    /**
     * Service 层只需要添加注解，完全不需要关心缓存实现
     */
    @Cacheable(value = "product", key = "#productId", expire = 3600)
    public ProductVO getProduct(Long productId) {
        // 只关心业务逻辑
        Product product = productsMapper.selectByPrimaryKey(productId);
        return convertToVO(product);
    }
    
    @CacheEvict(value = "product", key = "#productId")
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        // 只关心业务逻辑
        productsMapper.updateByPrimaryKeySelective(convertToEntity(request));
    }
}
```

---

## 方案对比

| 方案 | 复杂度 | Service 层代码 | 灵活性 | 推荐度 |
|------|--------|---------------|--------|--------|
| **Spring Cache** | ⭐ 简单 | 极简（只需注解） | ⭐⭐⭐ 高 | ⭐⭐⭐⭐⭐ 最推荐 |
| **Repository 模式** | ⭐⭐ 中等 | 简洁（依赖接口） | ⭐⭐⭐⭐ 很高 | ⭐⭐⭐⭐ 推荐 |
| **统一缓存服务** | ⭐⭐ 中等 | 简洁（依赖服务） | ⭐⭐⭐ 中等 | ⭐⭐⭐ 可用 |
| **AOP 切面** | ⭐⭐⭐ 复杂 | 极简（只需注解） | ⭐⭐⭐⭐ 很高 | ⭐⭐⭐ 复杂 |

---

## 推荐方案：Spring Cache（最简单）

### 完整实现示例

#### 1. 配置类

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("products", config.entryTtl(Duration.ofHours(2)))  // 产品缓存 2 小时
                .withCacheConfiguration("customers", config.entryTtl(Duration.ofHours(1)))  // 客户缓存 1 小时
                .build();
    }
}
```

#### 2. Service 层（完全忽略 Redis/MySQL）

```java
@Service
public class ProductService {
    
    @Autowired
    private ProductsMapper productsMapper;
    
    /**
     * 查询产品 - 自动处理缓存，Service 层完全不需要关心
     */
    @Cacheable(value = "products", key = "#productId")
    public ProductVO getProduct(Long productId) {
        Product product = productsMapper.selectByPrimaryKey(productId);
        return convertToVO(product);
    }
    
    /**
     * 更新产品 - 自动清除缓存
     */
    @CacheEvict(value = "products", key = "#productId")
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        productsMapper.updateByPrimaryKeySelective(convertToEntity(request));
    }
    
    /**
     * 删除产品 - 自动清除缓存
     */
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(Long productId) {
        productsMapper.deleteByPrimaryKey(productId);
    }
    
    /**
     * 批量清除缓存
     */
    @CacheEvict(value = "products", allEntries = true)
    public void clearAllCache() {
        // 清除所有产品缓存
    }
}
```

### 优点总结

- ✅ **Service 层极简**：只需要添加 `@Cacheable` 和 `@CacheEvict` 注解
- ✅ **自动处理**：Spring 自动处理缓存逻辑，无需手动编写缓存代码
- ✅ **可配置**：可以轻松切换缓存实现（Redis、Caffeine、内存等）
- ✅ **无需修改代码**：切换缓存实现只需修改配置，Service 层代码不变

---

## 总结

**推荐使用 Spring Cache**，因为：
1. ✅ 最简单：只需要添加注解
2. ✅ 最标准：Spring 官方推荐的方式
3. ✅ 最灵活：可以轻松切换缓存实现
4. ✅ Service 层完全不需要关心底层是 Redis 还是 MySQL

**如果 Spring Cache 不满足需求**，可以使用 Repository 模式，提供更多的灵活性。
