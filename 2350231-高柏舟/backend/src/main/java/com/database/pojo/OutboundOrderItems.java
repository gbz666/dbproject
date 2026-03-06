package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 出库单行项目表
 * @TableName outbound_order_items
 */
@Data
public class OutboundOrderItems {
    /**
     * 出库单行项目主键ID
     */
    private Long id;

    /**
     * 出库单ID(outbound_orders.id)
     */
    private Long outboundOrderId;

    /**
     * 产品ID(products.id)
     */
    private Long productId;

    /**
     * 出库仓库ID(warehouses.id)
     */
    private Long warehouseId;

    /**
     * 出库数量
     */
    private BigDecimal quantity;

    /**
     * 序列号列表(JSON数组或NULL)
     */
    private Object serialNumbers;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 创建人(staffs.id)
     */
    private Long createdById;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    /**
     * 最后修改人(staffs.id)
     */
    private Long updatedById;

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
        OutboundOrderItems other = (OutboundOrderItems) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOutboundOrderId() == null ? other.getOutboundOrderId() == null : this.getOutboundOrderId().equals(other.getOutboundOrderId()))
            && (this.getProductId() == null ? other.getProductId() == null : this.getProductId().equals(other.getProductId()))
            && (this.getWarehouseId() == null ? other.getWarehouseId() == null : this.getWarehouseId().equals(other.getWarehouseId()))
            && (this.getQuantity() == null ? other.getQuantity() == null : this.getQuantity().equals(other.getQuantity()))
            && (this.getSerialNumbers() == null ? other.getSerialNumbers() == null : this.getSerialNumbers().equals(other.getSerialNumbers()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getCreatedById() == null ? other.getCreatedById() == null : this.getCreatedById().equals(other.getCreatedById()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getUpdatedById() == null ? other.getUpdatedById() == null : this.getUpdatedById().equals(other.getUpdatedById()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOutboundOrderId() == null) ? 0 : getOutboundOrderId().hashCode());
        result = prime * result + ((getProductId() == null) ? 0 : getProductId().hashCode());
        result = prime * result + ((getWarehouseId() == null) ? 0 : getWarehouseId().hashCode());
        result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
        result = prime * result + ((getSerialNumbers() == null) ? 0 : getSerialNumbers().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getCreatedById() == null) ? 0 : getCreatedById().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getUpdatedById() == null) ? 0 : getUpdatedById().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", outboundOrderId=").append(outboundOrderId);
        sb.append(", productId=").append(productId);
        sb.append(", warehouseId=").append(warehouseId);
        sb.append(", quantity=").append(quantity);
        sb.append(", serialNumbers=").append(serialNumbers);
        sb.append(", remark=").append(remark);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", updatedById=").append(updatedById);
        sb.append("]");
        return sb.toString();
    }
}