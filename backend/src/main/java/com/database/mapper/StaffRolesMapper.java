package com.database.mapper;

import com.database.pojo.StaffRoles;

/**
* @author 高柏舟
* @description 针对表【staff_roles(员工-角色多对多关联表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.StaffRoles
*/
public interface StaffRolesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(StaffRoles record);

    int insertSelective(StaffRoles record);

    StaffRoles selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(StaffRoles record);

    int updateByPrimaryKey(StaffRoles record);

}
