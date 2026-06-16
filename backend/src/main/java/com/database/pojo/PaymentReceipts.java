package com.database.pojo;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 客户收款记录表
 * @TableName payment_receipts
 */
@Data
public class PaymentReceipts {
    /**
     * 收款记录主键ID
     */
    private Long id;

    /**
     * 收款单编号(展示用)
     */
    private String receiptNo;

    /**
     * 收款客户ID(customers.id)
     */
    private Long customerId;

    /**
     * 关联销项发票ID(sales_invoices.id)
     */
    private Long salesInvoiceId;

    /**
     * 收款金额
     */
    private BigDecimal amount;

    /**
     * 收款日期
     */
    private Date receiptDate;

    /**
     * 收款方式(如 cash/bank/transfer)
     */
    private String method;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 登记人(staffs.id)
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

    /**
     * 是否软删除:0=正常,1=已删除
     */
    private Integer isDeleted;

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
        PaymentReceipts other = (PaymentReceipts) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getReceiptNo() == null ? other.getReceiptNo() == null : this.getReceiptNo().equals(other.getReceiptNo()))
            && (this.getCustomerId() == null ? other.getCustomerId() == null : this.getCustomerId().equals(other.getCustomerId()))
            && (this.getSalesInvoiceId() == null ? other.getSalesInvoiceId() == null : this.getSalesInvoiceId().equals(other.getSalesInvoiceId()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getReceiptDate() == null ? other.getReceiptDate() == null : this.getReceiptDate().equals(other.getReceiptDate()))
            && (this.getMethod() == null ? other.getMethod() == null : this.getMethod().equals(other.getMethod()))
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
        result = prime * result + ((getReceiptNo() == null) ? 0 : getReceiptNo().hashCode());
        result = prime * result + ((getCustomerId() == null) ? 0 : getCustomerId().hashCode());
        result = prime * result + ((getSalesInvoiceId() == null) ? 0 : getSalesInvoiceId().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getReceiptDate() == null) ? 0 : getReceiptDate().hashCode());
        result = prime * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
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
        sb.append(", receiptNo=").append(receiptNo);
        sb.append(", customerId=").append(customerId);
        sb.append(", salesInvoiceId=").append(salesInvoiceId);
        sb.append(", amount=").append(amount);
        sb.append(", receiptDate=").append(receiptDate);
        sb.append(", method=").append(method);
        sb.append(", remark=").append(remark);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", updatedById=").append(updatedById);
        sb.append("]");
        return sb.toString();
    }
}