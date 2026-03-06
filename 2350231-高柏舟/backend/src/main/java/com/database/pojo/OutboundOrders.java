package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * 出库单表
 * @TableName outbound_orders
 */
@Data
public class OutboundOrders {
    /**
     * 出库单主键ID
     */
    private Long id;

    /**
     * 出库单编码(用于展示)
     */
    private String outboundCode;

    /**
     * 关联销售订单ID(sales_orders.id)
     */
    private Long salesOrderId;

    /**
     * 出库日期
     */
    private Date outboundDate;

    /**
     * 出库单状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

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
     * 是否软删除
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
        OutboundOrders other = (OutboundOrders) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOutboundCode() == null ? other.getOutboundCode() == null : this.getOutboundCode().equals(other.getOutboundCode()))
            && (this.getSalesOrderId() == null ? other.getSalesOrderId() == null : this.getSalesOrderId().equals(other.getSalesOrderId()))
            && (this.getOutboundDate() == null ? other.getOutboundDate() == null : this.getOutboundDate().equals(other.getOutboundDate()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
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
        result = prime * result + ((getOutboundCode() == null) ? 0 : getOutboundCode().hashCode());
        result = prime * result + ((getSalesOrderId() == null) ? 0 : getSalesOrderId().hashCode());
        result = prime * result + ((getOutboundDate() == null) ? 0 : getOutboundDate().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
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
        sb.append(", outboundCode=").append(outboundCode);
        sb.append(", salesOrderId=").append(salesOrderId);
        sb.append(", outboundDate=").append(outboundDate);
        sb.append(", status=").append(status);
        sb.append(", remark=").append(remark);
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