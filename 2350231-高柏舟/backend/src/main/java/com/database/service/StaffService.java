package com.database.service;

import com.database.mapper.StaffsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StaffService {
    private StaffsMapper staffsMapper;
    public StaffService(StaffsMapper staffsMapper) {
        this.staffsMapper = staffsMapper;
    }
    public Long getStaffIdByName(String name){
        return staffsMapper.selectByStaffName(name);
    }
}
