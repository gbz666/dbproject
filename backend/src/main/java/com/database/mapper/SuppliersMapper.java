package com.database.mapper;

import com.database.vo.SupplierDetailVO;
import com.database.pojo.Suppliers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Mapper
public interface SuppliersMapper {

    // ----------------- Select -----------------
    /**
     * 根据主键 ID 查询供应商（仅返回未删除的记录）
     */
    Suppliers selectByPrimaryKey(@Param("id") Long id);

    /**
     * 分页查询供应商详情列表 (包含关联员工姓名)
     */
    List<SupplierDetailVO> selectSupplierDetailsByPage(@RequestParam("supplierCode")String supplierCode,@RequestParam("supplierName") String supplierName);

    /**
     * 根据业务编号查询供应商实体（用于校验/获取ID）
     */
    Suppliers selectBySupplierCode(@Param("supplierCode") String supplierCode);

    /**
     * 根据业务编号查询供应商详情 DTO
     */
    SupplierDetailVO selectSupplierDtoBySupplierCode(@Param("supplierCode") String supplierCode);


    // ----------------- Insert/Update/Delete -----------------
    /**
     * 插入新的供应商记录，并自动回填 ID
     */
    void insertSupplier(Suppliers supplier);

    /**
     * 更新供应商的业务编号（创建后调用）
     */
    int updateSupplierCode(Suppliers supplier);

    /**
     * 根据 ID 动态更新供应商信息 (忽略 POJO 中为 null 的字段)
     */
    int updateSupplier(Suppliers supplier);

    /**
     * 软删除供应商
     * @return 影响的行数
     */
    Long updateToDeletedBySupplierCode(
            @Param("supplierCode") String supplierCode,
            @Param("currentStaffId") Long currentStaffId
    );
}