package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 进项发票明细
 * @TableName purchase_invoice_details
 */
@Data
public class PurchaseInvoiceDetails {
    /**
     * 进项发票明细主键ID
     */
    private Long id;

    /**
     * 发票ID(purchase_invoices.id)
     */
    private Long invoiceId;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 规格
     */
    private String specification;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 不含税金额
     */
    private BigDecimal amountExclusiveTax;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 税额
     */
    private BigDecimal taxAmount;

    /**
     * 含税金额
     */
    private BigDecimal amountInclusiveTax;

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
        PurchaseInvoiceDetails other = (PurchaseInvoiceDetails) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getInvoiceId() == null ? other.getInvoiceId() == null : this.getInvoiceId().equals(other.getInvoiceId()))
            && (this.getItemName() == null ? other.getItemName() == null : this.getItemName().equals(other.getItemName()))
            && (this.getSpecification() == null ? other.getSpecification() == null : this.getSpecification().equals(other.getSpecification()))
            && (this.getUnit() == null ? other.getUnit() == null : this.getUnit().equals(other.getUnit()))
            && (this.getQuantity() == null ? other.getQuantity() == null : this.getQuantity().equals(other.getQuantity()))
            && (this.getUnitPrice() == null ? other.getUnitPrice() == null : this.getUnitPrice().equals(other.getUnitPrice()))
            && (this.getAmountExclusiveTax() == null ? other.getAmountExclusiveTax() == null : this.getAmountExclusiveTax().equals(other.getAmountExclusiveTax()))
            && (this.getTaxRate() == null ? other.getTaxRate() == null : this.getTaxRate().equals(other.getTaxRate()))
            && (this.getTaxAmount() == null ? other.getTaxAmount() == null : this.getTaxAmount().equals(other.getTaxAmount()))
            && (this.getAmountInclusiveTax() == null ? other.getAmountInclusiveTax() == null : this.getAmountInclusiveTax().equals(other.getAmountInclusiveTax()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getCreatedById() == null ? other.getCreatedById() == null : this.getCreatedById().equals(other.getCreatedById()))
            && (this.getUpdatedById() == null ? other.getUpdatedById() == null : this.getUpdatedById().equals(other.getUpdatedById()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getInvoiceId() == null) ? 0 : getInvoiceId().hashCode());
        result = prime * result + ((getItemName() == null) ? 0 : getItemName().hashCode());
        result = prime * result + ((getSpecification() == null) ? 0 : getSpecification().hashCode());
        result = prime * result + ((getUnit() == null) ? 0 : getUnit().hashCode());
        result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
        result = prime * result + ((getUnitPrice() == null) ? 0 : getUnitPrice().hashCode());
        result = prime * result + ((getAmountExclusiveTax() == null) ? 0 : getAmountExclusiveTax().hashCode());
        result = prime * result + ((getTaxRate() == null) ? 0 : getTaxRate().hashCode());
        result = prime * result + ((getTaxAmount() == null) ? 0 : getTaxAmount().hashCode());
        result = prime * result + ((getAmountInclusiveTax() == null) ? 0 : getAmountInclusiveTax().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getCreatedById() == null) ? 0 : getCreatedById().hashCode());
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
        sb.append(", invoiceId=").append(invoiceId);
        sb.append(", itemName=").append(itemName);
        sb.append(", specification=").append(specification);
        sb.append(", unit=").append(unit);
        sb.append(", quantity=").append(quantity);
        sb.append(", unitPrice=").append(unitPrice);
        sb.append(", amountExclusiveTax=").append(amountExclusiveTax);
        sb.append(", taxRate=").append(taxRate);
        sb.append(", taxAmount=").append(taxAmount);
        sb.append(", amountInclusiveTax=").append(amountInclusiveTax);
        sb.append(", remark=").append(remark);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedById=").append(updatedById);
        sb.append("]");
        return sb.toString();
    }
}