package com.database.mapper;

import com.database.vo.CustomerDetailVO;
import com.database.pojo.Customers;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 开发者
 * @description 针对表【customers(客户基础信息表)】的数据库操作Mapper
 * @Entity com.database.pojo.Customers
 */
public interface CustomersMapper {

    // ... (保留原有的 insert, updateByPrimaryKeySelective, updateByPrimaryKey, deleteByPrimaryKey 等基础方法)

    /**
     * 分页查询客户详情列表，关联员工姓名
     */
    List<CustomerDetailVO> selectCustomerDetailsByPage(@Param("customerName") String customerName,@Param("customerCode") String customerCode);

    /**
     * 第一次插入客户记录（不含业务编码），用于获取自增主键ID
     */
    void insertCustomer(Customers customer);

    /**
     * 根据 ID 更新客户业务编码 (CustomerCode)
     */
    void updateCustomerCode(Customers customer);

    /**
     * 软删除客户：将 is_deleted 设为 1，并记录删除时间、更新人。
     * @param customerCode 客户业务编码
     * @param currentStaffId 当前操作员ID
     * @return 影响的行数
     */
    // 修正方法名拼写
    Long updateToDeletedByCustomerCode(@Param("customerCode") String customerCode, @Param("currentStaffId") Long currentStaffId);

    /**
     * 根据客户业务编码查询未删除的客户记录（Customers Entity）
     */
    Customers selectByCustomerCode(String customerCode);

    /**
     * 动态更新客户信息（Service层调用），基于 ID 进行更新，使用 Customers 实体来携带更新数据和关联人 ID
     * MyBatis XML 需使用 <update><set> 实现动态更新。
     * @param customer 包含 ID 和待更新字段的 Customers 实体
     * @return 影响的行数
     */
    // 参数类型改为 Customers 实体
    int updateCustomer(Customers customer);

    /**
     * 根据客户业务编码查询客户详情 DTO，关联员工姓名
     */
    CustomerDetailVO selectCustomerDtoByCustomerCode(String customerCode);
}