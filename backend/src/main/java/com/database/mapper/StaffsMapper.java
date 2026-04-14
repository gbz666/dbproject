package com.database.mapper;

import com.database.pojo.Staffs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 高柏舟
* @description 针对表【staffs(员工表:存放平台员工信息)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Staffs
*/
public interface StaffsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Staffs record);

    int insertSelective(Staffs record);

    Staffs selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Staffs record);

    int updateByPrimaryKey(Staffs record);

    Long selectByStaffName(@Param("staffName") String staffName);

    int updateLastLoginTime(@Param("id") Long id);

    List<Staffs> selectPageList(@Param("keyword") String keyword,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    int selectCount(@Param("keyword") String keyword);
}
