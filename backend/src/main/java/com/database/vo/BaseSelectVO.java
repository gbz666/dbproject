package com.database.vo;

import lombok.Data;

@Data
public class BaseSelectVO {
    private Long id;      // 主键ID
    private String code;  // 业务编号
    private String name;  // 名称

}