package com.database.service;

import com.database.dto.CustomerCreateRequest; // 【新】引入用于创建的 DTO
import com.database.vo.CustomerDetailVO;
import com.database.dto.CustomerUpdateRequest; // 【新】引入用于更新的 DTO (假设已创建)
import com.database.exception.BusinessException;
import com.database.mapper.CustomersMapper;
import com.database.mapper.StaffsMapper;
import com.database.pojo.Customers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects; // 引入 Objects

@Service
@Slf4j
public class CustomerService {

    // 使用 final 关键字，确保依赖不可变
    private final CustomersMapper customersMapper;
    private final StaffsMapper staffsMapper;

    @Autowired
    public CustomerService(CustomersMapper customersMapper, StaffsMapper staffsMapper) {
        this.customersMapper = customersMapper;
        this.staffsMapper = staffsMapper;
    }

    public PageInfo<CustomerDetailVO> getCustomersByPage(int pageNum, int pageSize,String customerName,String customerCode) {
        PageHelper.startPage(pageNum, pageSize);
        // 调用 MyBatis 关联查询方法
        List<CustomerDetailVO> customerDetailList = customersMapper.selectCustomerDetailsByPage(customerName,customerCode);
        return new PageInfo<>(customerDetailList);
    }

    /**
     * 辅助方法：通过员工姓名查找ID。
     * 如果姓名为空，则返回 null (表示不关联)。
     * 如果姓名不为空，但查不到 ID，则抛出业务异常。
     */
    private Long getStaffIdByName(String staffName, String role) {
        if (staffName == null || staffName.trim().isEmpty()) {
            return null; // 允许姓名为空，对应的 ID 设为 null
        }
        // 调用 StaffsMapper.selectByStaffName 方法
        Long staffId = staffsMapper.selectByStaffName(staffName);
        if (staffId == null) {
            throw new BusinessException("员工查找失败：" + role + "[" + staffName + "] 不存在或已被删除。", 404);
        }
        return staffId;
    }

    /**
     * 创建客户的业务逻辑。
     * @param request 客户创建请求 DTO (含姓名，无 ID/Code)
     * @param currentStaffId 当前操作员ID
     * @return 包含业务编码的完整 Customers 实体
     */
    @Transactional
    public Customers createCustomer(CustomerCreateRequest request, Long currentStaffId) { // 【注意】参数类型改为 CustomerCreateRequest

        // --- 1. 业务校验 ---
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new BusinessException("客户名称不能为空！", 400);
        }

        // --- 2. 姓名转 ID 并创建 Entity ---
        Long ownerId = getStaffIdByName(request.getOwnerName(), "客户归属人");
        Long followUpPersonId = getStaffIdByName(request.getFollowUpPersonName(), "客户跟进人");
        Long salesPersonId = getStaffIdByName(request.getSalesPersonName(), "客户销售人员");

        Customers newCustomer = new Customers();
        BeanUtils.copyProperties(request, newCustomer); // DTO -> Entity 属性拷贝

        // 2.3 设置关联人 ID 和审计字段
        newCustomer.setOwnerId(ownerId);
        newCustomer.setFollowUpPersonId(followUpPersonId);
        newCustomer.setSalesPersonId(salesPersonId);
        newCustomer.setCreatedById(currentStaffId);
        newCustomer.setUpdatedById(currentStaffId);
        // is_deleted 默认 0 (未删除)

        // --- 3. 第一次数据库操作：插入记录，MyBatis自动回填 ID ---
        customersMapper.insertCustomer(newCustomer);

        // --- 4. 核心业务逻辑：生成业务编码 ---
        Long newId = newCustomer.getId(); // 获取自动回填的 ID
        if (newId == null) {
            throw new BusinessException("系统错误：获取新客户的自增ID失败！", 500);
        }

        // 业务编码格式：K + ID
        String newCustomerCode = "K" + newId;
        newCustomer.setCustomerCode(newCustomerCode);

        // --- 5. 第二次数据库操作：更新业务编码 ---
        customersMapper.updateCustomerCode(newCustomer);

        // 6. 返回包含 id 和 customerCode 的完整对象
        return newCustomer;
    }

    @Transactional
    public void deleteCustomer(String customerCode, Long currentStaffId) {
        // 使用 Objects.requireNonNull 简化空值判断
        Objects.requireNonNull(customerCode, "客户编号不能为空。");
        Objects.requireNonNull(currentStaffId, "操作员ID不能为空。");

        Long affectedRows = customersMapper.updateToDeletedByCustomerCode(customerCode, currentStaffId); // 修正方法名拼写

        if (affectedRows == 0) {
            throw new BusinessException("删除失败：客户编号为 [" + customerCode + "] 的记录不存在或已被删除。", 404);
        }
    }

    /**
     * 更新客户信息
     * @param customerCode 客户业务编号
     * @param request 待更新的数据 DTO
     * @param currentStaffId 当前操作员ID
     * @return 更新后的完整客户对象 DTO
     */
    @Transactional
    public CustomerDetailVO updateCustomer(String customerCode, CustomerUpdateRequest request, Long currentStaffId) { // 【注意】参数类型改为 CustomerUpdateRequest

        // --- 1. 业务校验与 ID 获取 ---
        Objects.requireNonNull(customerCode, "客户编号不能为空。");

        // 1.1 校验客户是否存在且未被删除，并获取其内部 ID
        Customers existingCustomer = customersMapper.selectByCustomerCode(customerCode);

        if (existingCustomer == null || existingCustomer.getIsDeleted() == 1) {
            throw new BusinessException("更新失败：客户编号 [" + customerCode + "] 不存在或已被删除。", 404);
        }

        // --- 2. 创建 Entity 用于更新，并拷贝请求 DTO 的属性 ---
        Customers updateEntity = new Customers();
        // 将请求 DTO 的可更新字段拷贝到 Entity 中
        BeanUtils.copyProperties(request, updateEntity);

        // 2.1 设置必要 ID 和审计字段
        updateEntity.setId(existingCustomer.getId()); // 设置主键 ID 用于更新条件
        updateEntity.setCustomerCode(customerCode); // 设置 Code
        updateEntity.setUpdatedById(currentStaffId); // 记录是谁执行了更新

        // --- 3. 校验关联人并转换 ID (仅当名称字段在请求DTO中存在时才进行转换) ---
        // 注意：这里使用 request 来判断名称是否被提供，并设置到 updateEntity 中。

        // 3.1 归属人
        if (request.getOwnerName() != null) {
            Long ownerId = request.getOwnerName().trim().isEmpty() ? null : getStaffIdByName(request.getOwnerName(), "客户归属人");
            updateEntity.setOwnerId(ownerId);
        }

        // 3.2 跟进人
        if (request.getFollowUpPersonName() != null) {
            Long followUpPersonId = request.getFollowUpPersonName().trim().isEmpty() ? null : getStaffIdByName(request.getFollowUpPersonName(), "客户跟进人");
            updateEntity.setFollowUpPersonId(followUpPersonId);
        }

        // 3.3 销售人员
        if (request.getSalesPersonName() != null) {
            Long salesPersonId = request.getSalesPersonName().trim().isEmpty() ? null : getStaffIdByName(request.getSalesPersonName(), "客户销售人员");
            updateEntity.setSalesPersonId(salesPersonId);
        }

        // --- 4. 执行更新操作 ---
        // Mapper 需要支持根据 ID 动态更新非空字段
        int affectedRows = customersMapper.updateCustomer(updateEntity);

        if (affectedRows == 0) {
            log.warn("更新客户 [{}] 操作未影响任何记录，可能提交的数据与原有数据完全相同。", customerCode);
        }

        // 5. 返回更新后的完整 DTO (从数据库重新查询，确保返回最新数据)
        return customersMapper.selectCustomerDtoByCustomerCode(customerCode);
    }
}