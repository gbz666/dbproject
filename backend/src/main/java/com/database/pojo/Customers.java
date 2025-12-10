package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * 客户基础信息表
 * @TableName customers
 */
@Data
public class Customers {
    /**
     * 客户主键ID
     */
    private Long id;

    /**
     * 客户业务编号(对外展示，不作为外键)
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 注册地址/地址
     */
    private String address;

    /**
     * 客户电话/手机号
     */
    private String phone;

    /**
     * 客户邮箱
     */
    private String email;

    /**
     * 负责销售的员工ID(staffs.id)
     */
    private Long salesPersonId;

    /**
     * 跟进人员工ID(staffs.id)
     */
    private Long followUpPersonId;

    /**
     * 客户归属人(staffs.id)
     */
    private Long ownerId;

    /**
     * 付款天数(账期)
     */
    private Integer paymentTermsDays;

    /**
     * 付款条款/备注
     */
    private String paymentTermsNotes;

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
        Customers other = (Customers) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCustomerCode() == null ? other.getCustomerCode() == null : this.getCustomerCode().equals(other.getCustomerCode()))
            && (this.getCustomerName() == null ? other.getCustomerName() == null : this.getCustomerName().equals(other.getCustomerName()))
            && (this.getAddress() == null ? other.getAddress() == null : this.getAddress().equals(other.getAddress()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getSalesPersonId() == null ? other.getSalesPersonId() == null : this.getSalesPersonId().equals(other.getSalesPersonId()))
            && (this.getFollowUpPersonId() == null ? other.getFollowUpPersonId() == null : this.getFollowUpPersonId().equals(other.getFollowUpPersonId()))
            && (this.getOwnerId() == null ? other.getOwnerId() == null : this.getOwnerId().equals(other.getOwnerId()))
            && (this.getPaymentTermsDays() == null ? other.getPaymentTermsDays() == null : this.getPaymentTermsDays().equals(other.getPaymentTermsDays()))
            && (this.getPaymentTermsNotes() == null ? other.getPaymentTermsNotes() == null : this.getPaymentTermsNotes().equals(other.getPaymentTermsNotes()))
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
        result = prime * result + ((getCustomerCode() == null) ? 0 : getCustomerCode().hashCode());
        result = prime * result + ((getCustomerName() == null) ? 0 : getCustomerName().hashCode());
        result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getSalesPersonId() == null) ? 0 : getSalesPersonId().hashCode());
        result = prime * result + ((getFollowUpPersonId() == null) ? 0 : getFollowUpPersonId().hashCode());
        result = prime * result + ((getOwnerId() == null) ? 0 : getOwnerId().hashCode());
        result = prime * result + ((getPaymentTermsDays() == null) ? 0 : getPaymentTermsDays().hashCode());
        result = prime * result + ((getPaymentTermsNotes() == null) ? 0 : getPaymentTermsNotes().hashCode());
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
        sb.append(", customerCode=").append(customerCode);
        sb.append(", customerName=").append(customerName);
        sb.append(", address=").append(address);
        sb.append(", phone=").append(phone);
        sb.append(", email=").append(email);
        sb.append(", salesPersonId=").append(salesPersonId);
        sb.append(", followUpPersonId=").append(followUpPersonId);
        sb.append(", ownerId=").append(ownerId);
        sb.append(", paymentTermsDays=").append(paymentTermsDays);
        sb.append(", paymentTermsNotes=").append(paymentTermsNotes);
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