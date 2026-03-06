package com.database.service;

import com.database.mapper.StaffsMapper;
import com.database.pojo.Staffs;
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

    /**
     * 根据员工姓名查询员工信息
     */
    public Staffs getStaffByStaffName(String staffName) {
        Long id = staffsMapper.selectByStaffName(staffName);
        return id == null ? null : staffsMapper.selectByPrimaryKey(id);
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime(Long id) {
        staffsMapper.updateLastLoginTime(id);
    }

    /**
     * 根据 ID 查询员工（用于修改密码等）
     */
    public Staffs getStaffById(Long id) {
        return id == null ? null : staffsMapper.selectByPrimaryKey(id);
    }

    /**
     * 更新员工密码（BCrypt 已加密的字符串）
     */
    public void updatePassword(Long id, String encodedPassword) {
        Staffs staff = new Staffs();
        staff.setId(id);
        staff.setPassword(encodedPassword);
        staffsMapper.updateByPrimaryKeySelective(staff);
    }
}
