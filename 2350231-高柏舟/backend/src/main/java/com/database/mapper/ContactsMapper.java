package com.database.mapper;

import com.database.pojo.Contacts;

/**
* @author 高柏舟
* @description 针对表【contacts(联系人表:可以属于客户或供应商)】的数据库操作Mapper
* @createDate 2025-12-10 19:58:56
* @Entity com.database.pojo.Contacts
*/
public interface ContactsMapper {

    int deleteByPrimaryKey(Long id);

    int insert(Contacts record);

    int insertSelective(Contacts record);

    Contacts selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Contacts record);

    int updateByPrimaryKey(Contacts record);

}
