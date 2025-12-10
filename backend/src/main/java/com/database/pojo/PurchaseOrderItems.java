package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 采购订单行项目表
 * @TableName purchase_order_items
 */
@Data
public class PurchaseOrderItems {
    /**
     * 采购订单行项目主键ID
     */
    private Long id;

    /**
     * 采购订单ID(purchase_orders.id)
     */
    private Long purchaseOrderId;

    /**
     * 产品ID(products.id)
     */
    private Long productId;

    /**
     * 采购数量
     */
    private BigDecimal quantity;

    /**
     * 采购单价
     */
    private BigDecimal unitPrice;

    /**
     * 行合计(生成列)
     */
    private BigDecimal lineTotal;

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
        PurchaseOrderItems other = (PurchaseOrderItems) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPurchaseOrderId() == null ? other.getPurchaseOrderId() == null : this.getPurchaseOrderId().equals(other.getPurchaseOrderId()))
            && (this.getProductId() == null ? other.getProductId() == null : this.getProductId().equals(other.getProductId()))
            && (this.getQuantity() == null ? other.getQuantity() == null : this.getQuantity().equals(other.getQuantity()))
            && (this.getUnitPrice() == null ? other.getUnitPrice() == null : this.getUnitPrice().equals(other.getUnitPrice()))
            && (this.getLineTotal() == null ? other.getLineTotal() == null : this.getLineTotal().equals(other.getLineTotal()))
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
        result = prime * result + ((getPurchaseOrderId() == null) ? 0 : getPurchaseOrderId().hashCode());
        result = prime * result + ((getProductId() == null) ? 0 : getProductId().hashCode());
        result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
        result = prime * result + ((getUnitPrice() == null) ? 0 : getUnitPrice().hashCode());
        result = prime * result + ((getLineTotal() == null) ? 0 : getLineTotal().hashCode());
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
        sb.append(", purchaseOrderId=").append(purchaseOrderId);
        sb.append(", productId=").append(productId);
        sb.append(", quantity=").append(quantity);
        sb.append(", unitPrice=").append(unitPrice);
        sb.append(", lineTotal=").append(lineTotal);
        sb.append(", remark=").append(remark);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", updatedById=").append(updatedById);
        sb.append("]");
        return sb.toString();
    }
}