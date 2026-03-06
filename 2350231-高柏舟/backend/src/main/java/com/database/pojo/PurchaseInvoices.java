package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 进项发票表
 * @TableName purchase_invoices
 */
@Data
public class PurchaseInvoices {
    /**
     * 进项发票主键ID
     */
    private Long id;

    /**
     * 发票号/发票编码(唯一)
     */
    private String invoiceNo;

    /**
     * 供应商ID(suppliers.id)
     */
    private Long supplierId;

    /**
     * 关联入库单ID(stock_ins.id)
     */
    private Long relatedStockInId;

    /**
     * 发票金额(含税)
     */
    private BigDecimal amount;

    /**
     * 开票日期
     */
    private Date invoiceDate;

    /**
     * 发票状态(unsettled/settled)
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
     * 登记人(staffs.id)
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
        PurchaseInvoices other = (PurchaseInvoices) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getInvoiceNo() == null ? other.getInvoiceNo() == null : this.getInvoiceNo().equals(other.getInvoiceNo()))
            && (this.getSupplierId() == null ? other.getSupplierId() == null : this.getSupplierId().equals(other.getSupplierId()))
            && (this.getRelatedStockInId() == null ? other.getRelatedStockInId() == null : this.getRelatedStockInId().equals(other.getRelatedStockInId()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getInvoiceDate() == null ? other.getInvoiceDate() == null : this.getInvoiceDate().equals(other.getInvoiceDate()))
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
        result = prime * result + ((getInvoiceNo() == null) ? 0 : getInvoiceNo().hashCode());
        result = prime * result + ((getSupplierId() == null) ? 0 : getSupplierId().hashCode());
        result = prime * result + ((getRelatedStockInId() == null) ? 0 : getRelatedStockInId().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getInvoiceDate() == null) ? 0 : getInvoiceDate().hashCode());
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
        sb.append(", invoiceNo=").append(invoiceNo);
        sb.append(", supplierId=").append(supplierId);
        sb.append(", relatedStockInId=").append(relatedStockInId);
        sb.append(", amount=").append(amount);
        sb.append(", invoiceDate=").append(invoiceDate);
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