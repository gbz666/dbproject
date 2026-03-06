package com.database.pojo;

import lombok.Data;

/**
 * 员工表:存放平台员工信息
 * @TableName staffs
 */
@Data
public class Staffs {
    /**
     * 员工主键ID
     */
    private Long id;

    /**
     * 员工业务编号(对外或展示用，不用于逻辑关联)
     */
    private String staffCode;

    /**
     * 员工姓名
     */
    private String staffName;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 职位/职称
     */
    private String title;

    /**
     * 创建人ID，指向 staffs.id
     */
    private Long createdById;

    /**
     * 最后修改人ID，指向 staffs.id
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
        Staffs other = (Staffs) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getStaffCode() == null ? other.getStaffCode() == null : this.getStaffCode().equals(other.getStaffCode()))
            && (this.getStaffName() == null ? other.getStaffName() == null : this.getStaffName().equals(other.getStaffName()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getPhone() == null ? other.getPhone() == null : this.getPhone().equals(other.getPhone()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getCreatedById() == null ? other.getCreatedById() == null : this.getCreatedById().equals(other.getCreatedById()))
            && (this.getUpdatedById() == null ? other.getUpdatedById() == null : this.getUpdatedById().equals(other.getUpdatedById()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getStaffCode() == null) ? 0 : getStaffCode().hashCode());
        result = prime * result + ((getStaffName() == null) ? 0 : getStaffName().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getPhone() == null) ? 0 : getPhone().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
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
        sb.append(", staffCode=").append(staffCode);
        sb.append(", staffName=").append(staffName);
        sb.append(", email=").append(email);
        sb.append(", phone=").append(phone);
        sb.append(", title=").append(title);
        sb.append(", createdById=").append(createdById);
        sb.append(", updatedById=").append(updatedById);
        sb.append("]");
        return sb.toString();
    }
}