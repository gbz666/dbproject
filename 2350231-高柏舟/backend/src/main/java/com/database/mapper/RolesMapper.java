package com.database.mapper;

import com.database.pojo.Roles;

/**
* @author 高柏舟
* @description 针对表【roles(系统角色表)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Roles
*/
public interface RolesMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Roles record);

    int insertSelective(Roles record);

    Roles selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Roles record);

    int updateByPrimaryKey(Roles record);

}
