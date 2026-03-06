package com.database.service;

import com.database.vo.SupplierDetailVO;
import com.database.dto.SupplierRequest;
import com.database.exception.BusinessException;
import com.database.mapper.StaffsMapper;
import com.database.mapper.SuppliersMapper;
import com.database.pojo.Suppliers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SupplierService {

    private final SuppliersMapper suppliersMapper;
    private final StaffsMapper staffsMapper; // 假设 StaffsMapper 已经存在并提供 selectByStaffName 方法

    @Autowired
    public SupplierService(SuppliersMapper suppliersMapper, StaffsMapper staffsMapper) {
        this.suppliersMapper = suppliersMapper;
        this.staffsMapper = staffsMapper;
    }

    /**
     * 分页查询供应商列表
     */
    public PageInfo<SupplierDetailVO> getSuppliersByPage(int pageNum, int pageSize,String supplierCode,String supplierName) {
        PageHelper.startPage(pageNum, pageSize);
        List<SupplierDetailVO> supplierDetailList = suppliersMapper.selectSupplierDetailsByPage(supplierCode,supplierName);
        return new PageInfo<>(supplierDetailList);
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
     * 创建供应商的业务逻辑。
     * @param request 供应商创建请求 DTO
     * @param currentStaffId 当前操作员ID
     * @return 包含业务编码的完整 Suppliers 实体
     */
    @Transactional
    public Suppliers createSupplier(SupplierRequest request, Long currentStaffId) {

        // --- 1. 业务校验 ---
        if (request.getSupplierName() == null || request.getSupplierName().trim().isEmpty()) {
            throw new BusinessException("供应商名称不能为空！", 400);
        }

        // --- 2. 姓名转 ID 并创建 Entity ---
        Long ownerId = getStaffIdByName(request.getOwnerName(), "供应商归属人");
        Long followUpPersonId = getStaffIdByName(request.getFollowUpPersonName(), "供应商跟进人");
        Long salesPersonId = getStaffIdByName(request.getSalesPersonName(), "供应商关系维护人");

        Suppliers newSupplier = new Suppliers();
        BeanUtils.copyProperties(request, newSupplier); // DTO -> Entity 属性拷贝

        // 2.3 设置关联人 ID 和审计字段
        newSupplier.setOwnerId(ownerId);
        newSupplier.setFollowUpPersonId(followUpPersonId);
        newSupplier.setSalesPersonId(salesPersonId);
        newSupplier.setCreatedById(currentStaffId);
        newSupplier.setUpdatedById(currentStaffId);

        // --- 3. 第一次数据库操作：插入记录，MyBatis自动回填 ID ---
        suppliersMapper.insertSupplier(newSupplier);

        // --- 4. 核心业务逻辑：生成业务编码 ---
        Long newId = newSupplier.getId(); // 获取自动回填的 ID
        if (newId == null) {
            throw new BusinessException("系统错误：获取新供应商的自增ID失败！", 500);
        }

        // 业务编码格式：S + ID
        String newSupplierCode = "g" + newId;
        newSupplier.setSupplierCode(newSupplierCode);

        // --- 5. 第二次数据库操作：更新业务编码 ---
        suppliersMapper.updateSupplierCode(newSupplier);

        // 6. 返回包含 id 和 supplierCode 的完整对象
        return newSupplier;
    }

    /**
     * 更新供应商信息
     */
    @Transactional
    public SupplierDetailVO updateSupplier(String supplierCode, SupplierRequest request, Long currentStaffId) {

        // --- 1. 业务校验与 ID 获取 ---
        Objects.requireNonNull(supplierCode, "供应商编号不能为空。");

        // 1.1 校验供应商是否存在且未被删除，并获取其内部 ID
        Suppliers existingSupplier = suppliersMapper.selectBySupplierCode(supplierCode);

        if (existingSupplier == null || existingSupplier.getIsDeleted() == 1) {
            throw new BusinessException("更新失败：供应商编号 [" + supplierCode + "] 不存在或已被删除。", 404);
        }

        // --- 2. 创建 Entity 用于更新，并拷贝请求 DTO 的属性 ---
        Suppliers updateEntity = new Suppliers();
        BeanUtils.copyProperties(request, updateEntity);

        // 2.1 设置必要 ID 和审计字段
        updateEntity.setId(existingSupplier.getId()); // 设置主键 ID 用于更新条件
        updateEntity.setSupplierCode(supplierCode);
        updateEntity.setUpdatedById(currentStaffId); // 记录是谁执行了更新

        // --- 3. 校验关联人并转换 ID (仅当名称字段在请求DTO中存在且不为null时才进行处理) ---

        // 3.1 归属人
        if (request.getOwnerName() != null) {
            Long ownerId = request.getOwnerName().trim().isEmpty() ? null : getStaffIdByName(request.getOwnerName(), "供应商归属人");
            updateEntity.setOwnerId(ownerId);
        }

        // 3.2 跟进人
        if (request.getFollowUpPersonName() != null) {
            Long followUpPersonId = request.getFollowUpPersonName().trim().isEmpty() ? null : getStaffIdByName(request.getFollowUpPersonName(), "供应商跟进人");
            updateEntity.setFollowUpPersonId(followUpPersonId);
        }

        // 3.3 关系维护人
        if (request.getSalesPersonName() != null) {
            Long salesPersonId = request.getSalesPersonName().trim().isEmpty() ? null : getStaffIdByName(request.getSalesPersonName(), "供应商关系维护人");
            updateEntity.setSalesPersonId(salesPersonId);
        }

        // --- 4. 执行更新操作 ---
        suppliersMapper.updateSupplier(updateEntity);

        // 5. 返回更新后的完整 DTO
        return suppliersMapper.selectSupplierDtoBySupplierCode(supplierCode);
    }

    /**
     * 软删除供应商
     */
    @Transactional
    public void deleteSupplier(String supplierCode, Long currentStaffId) {
        Objects.requireNonNull(supplierCode, "供应商编号不能为空。");
        Objects.requireNonNull(currentStaffId, "操作员ID不能为空。");

        Long affectedRows = suppliersMapper.updateToDeletedBySupplierCode(supplierCode, currentStaffId);

        if (affectedRows == 0) {
            throw new BusinessException("删除失败：供应商编号为 [" + supplierCode + "] 的记录不存在或已被删除。", 404);
        }
    }
}