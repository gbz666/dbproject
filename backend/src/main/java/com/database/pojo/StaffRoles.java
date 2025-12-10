package com.database.pojo;

import java.util.Date;
import lombok.Data;

/**
 * 员工-角色多对多关联表
 * @TableName staff_roles
 */
@Data
public class StaffRoles {
    /**
     * 员工角色关联主键
     */
    private Long id;

    /**
     * 员工ID，外键指向 staffs.id
     */
    private Long staffId;

    /**
     * 角色ID，外键指向 roles.id
     */
    private Integer roleId;

    /**
     * 分配时间
     */
    private Date assignedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

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
        StaffRoles other = (StaffRoles) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getStaffId() == null ? other.getStaffId() == null : this.getStaffId().equals(other.getStaffId()))
            && (this.getRoleId() == null ? other.getRoleId() == null : this.getRoleId().equals(other.getRoleId()))
            && (this.getAssignedAt() == null ? other.getAssignedAt() == null : this.getAssignedAt().equals(other.getAssignedAt()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getStaffId() == null) ? 0 : getStaffId().hashCode());
        result = prime * result + ((getRoleId() == null) ? 0 : getRoleId().hashCode());
        result = prime * result + ((getAssignedAt() == null) ? 0 : getAssignedAt().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", staffId=").append(staffId);
        sb.append(", roleId=").append(roleId);
        sb.append(", assignedAt=").append(assignedAt);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}