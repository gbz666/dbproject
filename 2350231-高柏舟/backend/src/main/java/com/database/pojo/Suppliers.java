package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * 供应商基础信息表
 * @TableName suppliers
 */
@Data
public class Suppliers {
    /**
     * 供应商主键ID
     */
    private Long id;

    /**
     * 供应商业务编号(对外展示，不作为外键)
     */
    private String supplierCode;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商简称
     */
    private String shortName;

    /**
     * 主营业务描述
     */
    private String mainBusiness;

    /**
     * 税号/纳税识别号
     */
    private String taxNo;

    /**
     * 注册地址/地址
     */
    private String address;

    /**
     * 供应商电话
     */
    private String phone;

    /**
     * 供应商邮箱
     */
    private String email;

    /**
     * 负责销售/关系维护的员工ID(staffs.id)
     */
    private Long salesPersonId;

    /**
     * 跟进人员工ID(staffs.id)
     */
    private Long followUpPersonId;

    /**
     * 供应商归属人(staffs.id)
     */
    private Long ownerId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    /**
     * 创建人(staffs.id)
     */
    private Long createdById;

    /**
     * 最后修改人(staffs.id)
     */
    private Long updatedById;

    /**
     * 是否软删除:0=正常,1=已删除
     */
    private Integer isDeleted;

    /**
     * 删除时间(软删除)
     */
    private Date deletedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Suppliers other = (Suppliers) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getSupplierCode() == null ? other.getSupplierCode() == null : this.getSupplierCode().equals(other.getSupplierCode()))
            && (this.getSupplierName() == null ? other.getSupplierName() == null : this.getSupplierName().equals(other.getSupplierName()))
            && (this.getShortName() == null ? other.getShortName() == null : this.getShortName().equals(other.getShortName()))
            && (this.getMainBusiness() == null ? other.getMainBusiness() == null : this.getMainBusiness().equals(other.getMainBusiness()))
            && (this.getTaxNo() == null ? other.getTaxNo() == null : this.getTaxNo().equals(other.getTaxNo()))
            && (this.getAddress() == null ? other.getAddress() == null : this.getAddress().equals(other.getAddress()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getSalesPersonId() == null ? other.getSalesPersonId() == null : this.getSalesPersonId().equals(other.getSalesPersonId()))
            && (this.getFollowUpPersonId() == null ? other.getFollowUpPersonId() == null : this.getFollowUpPersonId().equals(other.getFollowUpPersonId()))
            && (this.getOwnerId() == null ? other.getOwnerId() == null : this.getOwnerId().equals(other.getOwnerId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getCreatedById() == null ? other.getCreatedById() == null : this.getCreatedById().equals(other.getCreatedById()))
            && (this.getUpdatedById() == null ? other.getUpdatedById() == null : this.getUpdatedById().equals(other.getUpdatedById()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()))
            && (this.getDeletedAt() == null ? other.getDeletedAt() == null : this.getDeletedAt().equals(other.getDeletedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSupplierCode() == null) ? 0 : getSupplierCode().hashCode());
        result = prime * result + ((getSupplierName() == null) ? 0 : getSupplierName().hashCode());
        result = prime * result + ((getShortName() == null) ? 0 : getShortName().hashCode());
        result = prime * result + ((getMainBusiness() == null) ? 0 : getMainBusiness().hashCode());
        result = prime * result + ((getTaxNo() == null) ? 0 : getTaxNo().hashCode());
        result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getSalesPersonId() == null) ? 0 : getSalesPersonId().hashCode());
        result = prime * result + ((getFollowUpPersonId() == null) ? 0 : getFollowUpPersonId().hashCode());
        result = prime * result + ((getOwnerId() == null) ? 0 : getOwnerId().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getCreatedById() == null) ? 0 : getCreatedById().hashCode());
        result = prime * result + ((getUpdatedById() == null) ? 0 : getUpdatedById().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        result = prime * result + ((getDeletedAt() == null) ? 0 : getDeletedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", supplierCode=").append(supplierCode);
        sb.append(", supplierName=").append(supplierName);
        sb.append(", shortName=").append(shortName);
        sb.append(", mainBusiness=").append(mainBusiness);
        sb.append(", taxNo=").append(taxNo);
        sb.append(", address=").append(address);
        sb.append(", phone=").append(phone);
        sb.append(", email=").append(email);
        sb.append(", salesPersonId=").append(salesPersonId);
        sb.append(", followUpPersonId=").append(followUpPersonId);
        sb.append(", ownerId=").append(ownerId);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedById=").append(updatedById);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", deletedAt=").append(deletedAt);
        sb.append("]");
        return sb.toString();
    }
}