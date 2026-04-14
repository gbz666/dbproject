package com.database.dto;

import lombok.Data;

import java.util.List;

@Data
public class StaffUpdateRequest {
    private String staffName;
    private String staffCode;
    private String email;
    private String phone;
    private String title;
    private List<Integer> roleIds;
}
