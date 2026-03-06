package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * 入库单/入库记录表
 * @TableName stock_ins
 */
@Data
public class StockIns {
    /**
     * 入库记录主键ID
     */
    private Long id;

    /**
     * 入库单编码(展示用)
     */
    private String stockInCode;

    /**
     * 来源采购订单ID(purchase_orders.id)
     */
    private Long purchaseOrderId;

    /**
     * 入库日期
     */
    private Date stockInDate;

    /**
     * 入库备注
     */
    private String note;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 操作人(staffs.id)
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
        StockIns other = (StockIns) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getStockInCode() == null ? other.getStockInCode() == null : this.getStockInCode().equals(other.getStockInCode()))
            && (this.getPurchaseOrderId() == null ? other.getPurchaseOrderId() == null : this.getPurchaseOrderId().equals(other.getPurchaseOrderId()))
            && (this.getStockInDate() == null ? other.getStockInDate() == null : this.getStockInDate().equals(other.getStockInDate()))
            && (this.getNote() == null ? other.getNote() == null : this.getNote().equals(other.getNote()))
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
        result = prime * result + ((getStockInCode() == null) ? 0 : getStockInCode().hashCode());
        result = prime * result + ((getPurchaseOrderId() == null) ? 0 : getPurchaseOrderId().hashCode());
        result = prime * result + ((getStockInDate() == null) ? 0 : getStockInDate().hashCode());
        result = prime * result + ((getNote() == null) ? 0 : getNote().hashCode());
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
        sb.append(", stockInCode=").append(stockInCode);
        sb.append(", purchaseOrderId=").append(purchaseOrderId);
        sb.append(", stockInDate=").append(stockInDate);
        sb.append(", note=").append(note);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", updatedById=").append(updatedById);
        sb.append("]");
        return sb.toString();
    }
}