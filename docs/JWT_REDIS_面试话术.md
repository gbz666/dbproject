# JWT + Redis 认证 面试话术

本文档根据项目实际实现整理，帮你把「JWT + Redis 认证」讲清楚，适合对 Redis 了解不深但想如实回答的情况。

---

## 一、整体流程（1 分钟版）

**面试官：「你这个 JWT + Redis 认证是怎么实现的？」**

> 登录成功后，我生成 JWT 并同时存到 Redis 里。之后每次请求，拦截器会从 `Authorization: Bearer xxx` 里取 token，先查 Redis 看 token 是否存在，再校验 JWT 签名。如果 token 快过期（比如还剩不到 30 分钟），拦截器会自动刷新，把新 token 放到响应头 `X-New-Token` 里，前端再更新本地 token。登出时，我会把 Redis 里对应的 token 删掉，这样即使 token 还在别人手里，也校验不过，实现真正的登出。

---

## 二、分点讲解（可展开）

### 1. 为什么用 Redis 存 Token？

**面试官：「为什么 JWT 还要存 Redis？JWT 不是无状态吗？」**

> JWT 本身是无状态的，但我在项目里用 Redis 存了 token，主要有几个目的：
>
> 1. **实现登出**：JWT 一旦发出，在过期前本身无法失效。把 token 存在 Redis 里，登出时删掉，下次验证时查不到，就相当于失效了。
> 2. **单点登录**：一个用户只保留一个有效 token，新登录时会把旧 token 从 Redis 删掉，旧 token 就无法再用了。
> 3. **统一校验**：服务端可以先查 Redis 再解析 JWT，保证 token 是当前有效的，而不仅是格式正确。

### 2. Redis 里存了什么？

**面试官：「Redis 具体存了什么？」**

> 我用了两种 key：
>
> - `token:{token}`：存的是 `userId`，过期时间等于 JWT 的过期时间（比如 2 小时），用于验证 token 是否有效。
> - `user_token:{userId}`：存的是当前用户的 token，用来做单点登录，登录时如果已有旧 token，就先删掉旧的，再写入新的。

### 3. 自动刷新是怎么做的？

**面试官：「Token 自动刷新怎么实现？」**

> 在拦截器里，每次请求时用 `RedisTemplate.getExpire()` 看 token 的剩余 TTL，如果小于 30 分钟，就调用 `TokenService.refreshToken()`：删除旧 token，生成新 JWT，再写入 Redis。新 token 放到响应头 `X-New-Token`，前端在响应拦截器里读取并更新本地 token，这样用户长时间操作不会突然掉线。

### 4. 校验流程

**面试官：「验证 token 的完整流程是什么？」**

> 1. 从请求头拿到 `Authorization: Bearer xxx`，截取 token。
> 2. 查 Redis 的 `token:{token}`，查不到直接返回 401。
> 3. 用 Jjwt 解析 JWT，验证签名和过期时间。
> 4. 比对 JWT 里的 `userId` 和 Redis 里存的 `userId` 是否一致。
> 5. 通过后，把 `userId`、`staffName` 等放到 `request` 属性里，供业务层使用（比如进项发票的 `currentStaffId`）。

---

## 三、常见追问

### Q1：JWT 和 Session 的区别？

> 我简单理解是：Session 把状态存在服务端（如 Redis），JWT 把用户信息放在 token 里、本身无状态。我项目里是 JWT + Redis  hybrid 方式，既用 JWT 携带信息，又用 Redis 做 token 管理和登出。

### Q2：如果 Redis 挂了怎么办？

> 目前项目里，Redis 挂掉的话，`validateToken` 查不到 Redis 会直接返回 null，相当于所有需要认证的接口都会返回 401。生产环境一般会做 Redis 高可用（主从、哨兵或集群），或者考虑在 Redis 不可用时做一个降级逻辑（比如只校验 JWT 不查 Redis），但这会失去「登出即失效」和单点登录的能力，需要根据业务权衡。

### Q3：Token 存在哪里？前端怎么传？

> 前端把 token 存在 Cookie 里（或者 localStorage，看具体实现），请求时在 Header 里带上 `Authorization: Bearer <token>`。我的拦截器只从 Header 读，不关心前端具体存在哪。

### Q4：Redis 的 TTL 为什么和 JWT 过期时间一样？

> 保持一致是为了逻辑简单：JWT 过期了，Redis 里的记录也过期了，两边状态一致。如果 JWT 过期而 Redis 没过期，或者反过来，都会导致校验逻辑复杂，所以设置成相同的过期时间。

---

## 四、不太熟的可以怎么说

**面试官：「Redis 底层数据结构了解吗？」**

> 我主要是用 Spring Data Redis 的 `RedisTemplate`，对底层数据结构没有深入看过。我知道 Redis 有 String、Hash、List、Set 等，我这个项目里用的是 String 类型的 key-value，像 `set key value ex 7200` 这种。

**面试官：「Redis 持久化、主从、集群了解吗？」**

> 这些我还没深入做过，只是知道有 RDB、AOF 持久化，以及主从、哨兵、集群等部署方式。我项目里 Redis 主要用于 token 存储，是开发环境单实例，生产环境会交给运维做高可用。如果后续有需要，我准备按官方文档和项目需求再补这块。

---

## 五、一句话总结（收尾用）

> 我这个项目的认证是「JWT 携带用户信息 + Redis 存储 token 做登出和单点登录」，在拦截器里做校验和自动刷新。Redis 我主要是按业务需求来用，对底层实现还在学习中。

---

**提示**：面试时尽量结合自己项目讲，不用背得太死。如果问到没做过的点，可以说「这块我还没在实际项目里用到，但知道基本原理，后续会补」之类，诚实比硬掰更好。
