# 高并发与锁方案说明

本文档基于 [OVERVIEW.md](./OVERVIEW.md) 中的架构，说明在高并发下可能存在的问题，以及“上锁”等可选方案。

---

## 1. 当前与并发相关的实现

### 1.1 已经具备的保障

- **库存数量更新**（`InventoryMapper.updateInventory`）  
  SQL 使用 `INSERT ... ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)`，是**单行原子更新**，不会出现“先读再写”导致的丢失更新。同一 (product_id, warehouse_id) 的并发加减在数据库层面是安全的。

- **事务**  
  出库、入库、销售订单、采购订单等写操作已使用 `@Transactional(rollbackFor = Exception.class)`，保证单次请求内的多表一致性。

### 1.2 高并发下容易出的问题

| 场景 | 问题 | 后果 |
|------|------|------|
| **出库扣减库存** | 当前是直接 `quantity + (-qty)`，没有“扣减后不能为负”的约束 | 并发多笔出库可能导致库存被扣成负数（超卖） |
| **销售订单创建** | 先 `selectProductNum` 再插入订单，扣库存在**出库单**里做，和这里的校验分离 | 多请求同时通过“库存足够”的校验，后续出库时仍可能超卖 |
| **订单号生成** | 销售/采购单号用 `COUNT(*)+1` 生成 | 并发下可能得到相同单号（需依赖唯一索引 + 重试） |

---

## 2. 推荐方案概览

- **库存扣减**：用**数据库原子条件更新**（见下节），避免显式锁、又能防超卖。  
- **需要“先占库存”时**：可在出库/下单时对库存行加**行级悲观锁**（`SELECT ... FOR UPDATE`）。  
- **多实例部署**：需要跨 JVM 互斥时，可用 **Redis 分布式锁**。  
- **订单号**：保证 `order_code` 唯一索引，冲突时重试或改用序列表/数据库序列。

下面分项说明。

---

## 3. 库存扣减：原子条件更新（防超卖，推荐）

不在应用层“先查再减”，而是用一条 SQL 在**扣减时带条件**，只有库存足够才更新，否则不更新。

思路：

- **出库**：`UPDATE inventory SET quantity = quantity - #{qty} WHERE product_id = ? AND warehouse_id = ? AND quantity >= #{qty}`  
- 执行后看 **affected rows**：  
  - 若为 0，说明当前库存不足，直接返回“库存不足”错误，可重试或提示用户；  
  - 若为 1，扣减成功，继续后续逻辑。

这样：

- 不需要在应用层加锁，也不需要对整张表加锁。  
- 利用数据库行级锁，同一行的并发扣减会串行执行，且不会出现负库存。  
- 适合高并发：锁粒度小、逻辑简单。

实现上可在 `InventoryMapper` 中新增方法（如 `deductInventoryIfSufficient`），在 `OutBoundOrdersService.processItemsAndInventory` 中调用该方法扣减，并根据返回值判断成功与否；若不足则抛业务异常并回滚事务。  
（项目中已预留/实现该原子扣减接口时，出库逻辑应统一走该接口。）

---

## 4. 行级悲观锁：SELECT ... FOR UPDATE

适用场景：需要“先锁定再根据当前值做复杂判断或多次更新”时（例如先锁库存行，再根据业务规则决定扣多少、占多少）。

做法：

- 在**同一事务**内：  
  1）`SELECT ... FROM inventory WHERE product_id = ? AND warehouse_id = ? FOR UPDATE`  
  2）在应用层判断 `quantity` 是否足够  
  3）若足够则 `UPDATE inventory SET quantity = quantity - ? ...`  
  4）提交事务

- 效果：同一 (product_id, warehouse_id) 的并发事务会在这行上排队，避免超卖；但会增大锁等待时间，高并发下容易成为瓶颈。

建议：能用电子的“原子条件更新”就不用 `FOR UPDATE`；只有逻辑复杂、必须“读-判断-写”时再用悲观锁。

---

## 5. 乐观锁（版本号）

思路：表增加 `version` 字段，更新时 `UPDATE ... SET quantity = ..., version = version + 1 WHERE ... AND version = #{oldVersion}`；若 affected rows 为 0 则说明被别的请求改过，在应用层重试或提示。

- 优点：无长事务、无行锁占用，适合读多写少。  
- 缺点：高并发写时冲突多，需要重试策略。

本项目库存写并发若不高，优先用**原子条件更新**即可；若后续要加“预留/占用”等复杂状态，再考虑加 version 做乐观锁。

---

## 6. Redis 分布式锁

适用场景：**多实例部署**时，需要跨 JVM 互斥（例如同一时刻只允许一个定时任务跑、或对“单号生成”做全局限流）。

用法（概念）：

- Key：如 `lock:inventory:${productId}:${warehouseId}`  
- Value：唯一标识（如 UUID），用于释放时校验  
- 过期时间：避免死锁（如 10 秒）  
- 加锁：`SET key value NX PX 10000`  
- 解锁：先 GET 再 DEL，或使用 Lua 脚本保证“只删自己的锁”

注意：库存扣减更推荐在数据库用原子更新；Redis 锁适合“单号生成”“定时任务”等与 DB 解耦的互斥场景，或作为补充手段（例如在调用原子扣减前加短时锁，减少重试次数）。

---

## 7. 订单号并发

当前：`countOrdersByMonth` 得到 N，然后生成 `xs/cg + 年月 + (N+1)`。并发时两个请求可能都得到 N，生成相同单号。

建议：

1. **数据库**：对 `sales_orders.order_code`、`purchase_orders.purchase_code` 建 **UNIQUE** 索引。  
2. **应用**：生成单号并插入，若捕获到唯一键冲突（如 MySQL `DuplicateKeyException`），则重试一次（重新 count 或使用序列表生成新号）。  
3. 可选：用**序列表**或数据库序列替代“COUNT+1”，由数据库保证自增，避免重复。

---

## 8. 小结

| 目标 | 推荐做法 |
|------|----------|
| 出库不超卖、库存不为负 | **原子条件更新**：`UPDATE quantity = quantity - ? WHERE ... AND quantity >= ?`，根据 affected rows 判断 |
| 必须先读再写的复杂逻辑 | 同一事务内 **SELECT ... FOR UPDATE** 行级锁 |
| 多实例互斥（非库存） | **Redis 分布式锁**（带过期、安全释放） |
| 订单号不重复 | **唯一索引 + 冲突重试** 或 序列表/序列 |
| 减少锁竞争 | 尽量在 DB 用原子更新；锁的粒度尽量小（按行、按业务键） |

当前项目已具备事务和原子 `quantity` 加减；在此基础上，**优先在库存扣减处改为“原子条件更新”**，即可在高并发下避免超卖，无需一上来就加分布式锁或全局锁。若你希望，我可以在仓库里标出需要改动的 Mapper/Service 位置并给出具体补丁示例。
