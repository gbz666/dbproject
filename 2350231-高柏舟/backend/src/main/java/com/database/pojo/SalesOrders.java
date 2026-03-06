package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 销售订单表
 * @TableName sales_orders
 */
@Data
public class SalesOrders {
    /**
     * 销售订单主键ID
     */
    private Long id;

    /**
     * 销售订单业务编号(例如 xsYYMMNNN，仅用于展示)
     */
    private String orderCode;

    /**
     * 订单日期
     */
    private Date orderDate;

    /**
     * 客户ID(customers.id)
     */
    private Long customerId;

    /**
     * 订单状态(建议使用字典表)
     */
    private String orderStatus;

    /**
     * 订单总金额(含税)
     */
    private BigDecimal totalAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 销售员(staffs.id)
     */
    private Long salesPersonId;

    /**
     * 跟进人(staffs.id)
     */
    private Long followUpPersonId;

    /**
     * 订单归属人(staffs.id)
     */
    private Long ownerId;

    /**
     * 订单备注/说明
     */
    private String note;

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
        SalesOrders other = (SalesOrders) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOrderCode() == null ? other.getOrderCode() == null : this.getOrderCode().equals(other.getOrderCode()))
            && (this.getOrderDate() == null ? other.getOrderDate() == null : this.getOrderDate().equals(other.getOrderDate()))
            && (this.getCustomerId() == null ? other.getCustomerId() == null : this.getCustomerId().equals(other.getCustomerId()))
            && (this.getOrderStatus() == null ? other.getOrderStatus() == null : this.getOrderStatus().equals(other.getOrderStatus()))
            && (this.getTotalAmount() == null ? other.getTotalAmount() == null : this.getTotalAmount().equals(other.getTotalAmount()))
            && (this.getCurrency() == null ? other.getCurrency() == null : this.getCurrency().equals(other.getCurrency()))
            && (this.getSalesPersonId() == null ? other.getSalesPersonId() == null : this.getSalesPersonId().equals(other.getSalesPersonId()))
            && (this.getFollowUpPersonId() == null ? other.getFollowUpPersonId() == null : this.getFollowUpPersonId().equals(other.getFollowUpPersonId()))
            && (this.getOwnerId() == null ? other.getOwnerId() == null : this.getOwnerId().equals(other.getOwnerId()))
            && (this.getNote() == null ? other.getNote() == null : this.getNote().equals(other.getNote()))
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
        result = prime * result + ((getOrderCode() == null) ? 0 : getOrderCode().hashCode());
        result = prime * result + ((getOrderDate() == null) ? 0 : getOrderDate().hashCode());
        result = prime * result + ((getCustomerId() == null) ? 0 : getCustomerId().hashCode());
        result = prime * result + ((getOrderStatus() == null) ? 0 : getOrderStatus().hashCode());
        result = prime * result + ((getTotalAmount() == null) ? 0 : getTotalAmount().hashCode());
        result = prime * result + ((getCurrency() == null) ? 0 : getCurrency().hashCode());
        result = prime * result + ((getSalesPersonId() == null) ? 0 : getSalesPersonId().hashCode());
        result = prime * result + ((getFollowUpPersonId() == null) ? 0 : getFollowUpPersonId().hashCode());
        result = prime * result + ((getOwnerId() == null) ? 0 : getOwnerId().hashCode());
        result = prime * result + ((getNote() == null) ? 0 : getNote().hashCode());
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
        sb.append(", orderCode=").append(orderCode);
        sb.append(", orderDate=").append(orderDate);
        sb.append(", customerId=").append(customerId);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", totalAmount=").append(totalAmount);
        sb.append(", currency=").append(currency);
        sb.append(", salesPersonId=").append(salesPersonId);
        sb.append(", followUpPersonId=").append(followUpPersonId);
        sb.append(", ownerId=").append(ownerId);
        sb.append(", note=").append(note);
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