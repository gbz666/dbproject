package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 销售订单行项目表
 * @TableName sales_order_items
 */
@Data
public class SalesOrderItems {
    /**
     * 销售订单行项目主键ID
     */
    private Long id;

    /**
     * 销售订单ID(sales_orders.id)
     */
    private Long salesOrderId;

    /**
     * 产品ID(products.id)
     */
    private Long productId;

    /**
     * 数量(含小数)
     */
    private BigDecimal quantity;

    /**
     * 销售单价(含税/或按业务定义)
     */
    private BigDecimal unitPrice;

    /**
     * 行折扣金额
     */
    private BigDecimal discount;

    /**
     * 税率(如 0.13 表示 13%)
     */
    private BigDecimal taxRate;

    /**
     * 行合计(生成列，含税)
     */
    private BigDecimal lineTotal;

    /**
     * 行备注
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
        SalesOrderItems other = (SalesOrderItems) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getSalesOrderId() == null ? other.getSalesOrderId() == null : this.getSalesOrderId().equals(other.getSalesOrderId()))
            && (this.getProductId() == null ? other.getProductId() == null : this.getProductId().equals(other.getProductId()))
            && (this.getQuantity() == null ? other.getQuantity() == null : this.getQuantity().equals(other.getQuantity()))
            && (this.getUnitPrice() == null ? other.getUnitPrice() == null : this.getUnitPrice().equals(other.getUnitPrice()))
            && (this.getDiscount() == null ? other.getDiscount() == null : this.getDiscount().equals(other.getDiscount()))
            && (this.getTaxRate() == null ? other.getTaxRate() == null : this.getTaxRate().equals(other.getTaxRate()))
            && (this.getLineTotal() == null ? other.getLineTotal() == null : this.getLineTotal().equals(other.getLineTotal()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getCreatedById() == null ? other.getCreatedById() == null : this.getCreatedById().equals(other.getCreatedById()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSalesOrderId() == null) ? 0 : getSalesOrderId().hashCode());
        result = prime * result + ((getProductId() == null) ? 0 : getProductId().hashCode());
        result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
        result = prime * result + ((getUnitPrice() == null) ? 0 : getUnitPrice().hashCode());
        result = prime * result + ((getDiscount() == null) ? 0 : getDiscount().hashCode());
        result = prime * result + ((getTaxRate() == null) ? 0 : getTaxRate().hashCode());
        result = prime * result + ((getLineTotal() == null) ? 0 : getLineTotal().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getCreatedById() == null) ? 0 : getCreatedById().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", salesOrderId=").append(salesOrderId);
        sb.append(", productId=").append(productId);
        sb.append(", quantity=").append(quantity);
        sb.append(", unitPrice=").append(unitPrice);
        sb.append(", discount=").append(discount);
        sb.append(", taxRate=").append(taxRate);
        sb.append(", lineTotal=").append(lineTotal);
        sb.append(", remark=").append(remark);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", createdById=").append(createdById);
        sb.append("]");
        return sb.toString();
    }
}