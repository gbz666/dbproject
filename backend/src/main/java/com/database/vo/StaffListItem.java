package com.database.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class StaffListItem {
    private Long id;
    private String staffCode;
    private String staffName;
    private String email;
    private String phone;
    private String title;
    private Integer status;
    private Date lastLoginAt;
    private List<String> roleNames;
    private List<Integer> roleIds;
}
