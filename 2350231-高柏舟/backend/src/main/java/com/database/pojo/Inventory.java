package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 库存表(复合主键 product_id+warehouse_id)
 * @TableName inventory
 */
@Data
public class Inventory {
    /**
     * 产品ID(products.id)，复合主键一部分
     */
    private Long productId;

    /**
     * 仓库ID(warehouses.id)，复合主键一部分
     */
    private Long warehouseId;

    /**
     * 当前库存数量(可带小数)
     */
    private BigDecimal quantity;

    /**
     * 被占用/预留库存数量
     */
    private BigDecimal reservedQuantity;

    /**
     * 最近一次入库时间
     */
    private Date lastStockInAt;

    /**
     * 最近一次出库时间
     */
    private Date lastStockOutAt;

    /**
     * 库存最后更新时间
     */
    private Date updatedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

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
        Inventory other = (Inventory) that;
        return (this.getProductId() == null ? other.getProductId() == null : this.getProductId().equals(other.getProductId()))
            && (this.getWarehouseId() == null ? other.getWarehouseId() == null : this.getWarehouseId().equals(other.getWarehouseId()))
            && (this.getQuantity() == null ? other.getQuantity() == null : this.getQuantity().equals(other.getQuantity()))
            && (this.getReservedQuantity() == null ? other.getReservedQuantity() == null : this.getReservedQuantity().equals(other.getReservedQuantity()))
            && (this.getLastStockInAt() == null ? other.getLastStockInAt() == null : this.getLastStockInAt().equals(other.getLastStockInAt()))
            && (this.getLastStockOutAt() == null ? other.getLastStockOutAt() == null : this.getLastStockOutAt().equals(other.getLastStockOutAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getCreatedById() == null ? other.getCreatedById() == null : this.getCreatedById().equals(other.getCreatedById()))
            && (this.getUpdatedById() == null ? other.getUpdatedById() == null : this.getUpdatedById().equals(other.getUpdatedById()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()))
            && (this.getDeletedAt() == null ? other.getDeletedAt() == null : this.getDeletedAt().equals(other.getDeletedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getProductId() == null) ? 0 : getProductId().hashCode());
        result = prime * result + ((getWarehouseId() == null) ? 0 : getWarehouseId().hashCode());
        result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
        result = prime * result + ((getReservedQuantity() == null) ? 0 : getReservedQuantity().hashCode());
        result = prime * result + ((getLastStockInAt() == null) ? 0 : getLastStockInAt().hashCode());
        result = prime * result + ((getLastStockOutAt() == null) ? 0 : getLastStockOutAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
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
        sb.append(", productId=").append(productId);
        sb.append(", warehouseId=").append(warehouseId);
        sb.append(", quantity=").append(quantity);
        sb.append(", reservedQuantity=").append(reservedQuantity);
        sb.append(", lastStockInAt=").append(lastStockInAt);
        sb.append(", lastStockOutAt=").append(lastStockOutAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedById=").append(updatedById);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", deletedAt=").append(deletedAt);
        sb.append("]");
        return sb.toString();
    }
}