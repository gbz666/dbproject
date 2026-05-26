package com.database.service;

import com.database.dto.StaffCreateRequest;
import com.database.dto.StaffUpdateRequest;
import com.database.exception.BusinessException;
import com.database.mapper.RolesMapper;
import com.database.mapper.StaffRolesMapper;
import com.database.mapper.StaffsMapper;
import com.database.pojo.Roles;
import com.database.pojo.StaffRoles;
import com.database.pojo.Staffs;
import com.database.vo.StaffListItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class StaffService {

    private final StaffsMapper staffsMapper;
    private final StaffRolesMapper staffRolesMapper;
    private final RolesMapper rolesMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StaffService(StaffsMapper staffsMapper, StaffRolesMapper staffRolesMapper, RolesMapper rolesMapper) {
        this.staffsMapper = staffsMapper;
        this.staffRolesMapper = staffRolesMapper;
        this.rolesMapper = rolesMapper;
    }

    public Long getStaffIdByName(String name) {
        return staffsMapper.selectByStaffName(name);
    }

    public Staffs getStaffByStaffName(String staffName) {
        Long id = staffsMapper.selectByStaffName(staffName);
        return id == null ? null : staffsMapper.selectByPrimaryKey(id);
    }

    public void updateLastLoginTime(Long id) {
        staffsMapper.updateLastLoginTime(id);
    }

    public Staffs getStaffById(Long id) {
        return id == null ? null : staffsMapper.selectByPrimaryKey(id);
    }

    public void updatePassword(Long id, String encodedPassword) {
        Staffs staff = new Staffs();
        staff.setId(id);
        staff.setPassword(encodedPassword);
        staffsMapper.updateByPrimaryKeySelective(staff);
    }

    // ==================== 员工管理 ====================

    /**
     * 分页查询员工列表（含角色信息）
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findStaffPage(String keyword, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Staffs> staffList = staffsMapper.selectPageList(keyword, offset, pageSize);
        int total = staffsMapper.selectCount(keyword);

        List<StaffListItem> items = new ArrayList<>();
        for (Staffs s : staffList) {
            StaffListItem item = new StaffListItem();
            item.setId(s.getId());
            item.setStaffCode(s.getStaffCode());
            item.setStaffName(s.getStaffName());
            item.setEmail(s.getEmail());
            item.setPhone(s.getPhone());
            item.setTitle(s.getTitle());
            item.setStatus(s.getStatus());
            item.setLastLoginAt(s.getLastLoginAt());
            item.setRoleNames(staffRolesMapper.selectRoleNamesByStaffId(s.getId()));
            item.setRoleIds(staffRolesMapper.selectRoleIdsByStaffId(s.getId()));
            items.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", items);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 新建员工
     */
    @Transactional(rollbackFor = Exception.class)
    public void createStaff(StaffCreateRequest req, Long operatorId) {
        Long existing = staffsMapper.selectByStaffName(req.getStaffName());
        if (existing != null) {
            throw new BusinessException("员工姓名已存在");
        }

        Staffs staff = new Staffs();
        staff.setStaffCode(blankToNull(req.getStaffCode()));
        staff.setStaffName(req.getStaffName());
        staff.setEmail(blankToNull(req.getEmail()));
        staff.setPhone(blankToNull(req.getPhone()));
        staff.setTitle(blankToNull(req.getTitle()));
        staff.setPassword(passwordEncoder.encode(req.getPassword()));
        staff.setStatus(1);
        staff.setCreatedById(operatorId);
        staffsMapper.insertSelective(staff);

        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            assignRolesInternal(staff.getId(), req.getRoleIds());
        }
        log.info("管理员 {} 创建了员工 {}(ID={})", operatorId, req.getStaffName(), staff.getId());
    }

    /**
     * 编辑员工基本信息 + 角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStaff(Long staffId, StaffUpdateRequest req, Long operatorId) {
        Staffs existing = staffsMapper.selectByPrimaryKey(staffId);
        if (existing == null) {
            throw new BusinessException("员工不存在", 404);
        }

        Staffs update = new Staffs();
        update.setId(staffId);
        if (req.getStaffName() != null) update.setStaffName(req.getStaffName());
        if (req.getStaffCode() != null) update.setStaffCode(blankToNull(req.getStaffCode()));
        if (req.getEmail() != null) update.setEmail(blankToNull(req.getEmail()));
        if (req.getPhone() != null) update.setPhone(blankToNull(req.getPhone()));
        if (req.getTitle() != null) update.setTitle(blankToNull(req.getTitle()));
        update.setUpdatedById(operatorId);
        staffsMapper.updateByPrimaryKeySelective(update);

        if (req.getRoleIds() != null) {
            staffRolesMapper.deleteByStaffId(staffId);
            if (!req.getRoleIds().isEmpty()) {
                assignRolesInternal(staffId, req.getRoleIds());
            }
        }
        log.info("管理员 {} 编辑了员工 ID={}", operatorId, staffId);
    }

    /**
     * 重置员工密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long staffId, String newPassword) {
        Staffs existing = staffsMapper.selectByPrimaryKey(staffId);
        if (existing == null) {
            throw new BusinessException("员工不存在", 404);
        }
        Staffs update = new Staffs();
        update.setId(staffId);
        update.setPassword(passwordEncoder.encode(newPassword));
        staffsMapper.updateByPrimaryKeySelective(update);
        log.info("员工 ID={} 密码已重置", staffId);
    }

    /**
     * 切换员工账户状态（启用/禁用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long staffId) {
        Staffs existing = staffsMapper.selectByPrimaryKey(staffId);
        if (existing == null) {
            throw new BusinessException("员工不存在", 404);
        }
        Staffs update = new Staffs();
        update.setId(staffId);
        update.setStatus(existing.getStatus() == null || existing.getStatus() == 1 ? 0 : 1);
        staffsMapper.updateByPrimaryKeySelective(update);
        log.info("员工 ID={} 状态切换为 {}", staffId, update.getStatus());
    }

    /**
     * 获取所有角色
     */
    public List<Roles> getAllRoles() {
        return rolesMapper.selectAll();
    }

    /** 空串 / 全空白 → null，避免 UNIQUE 列（如 staff_code）多条空串触发唯一键冲突 */
    private static String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }

    private void assignRolesInternal(Long staffId, List<Integer> roleIds) {
        for (Integer roleId : roleIds) {
            StaffRoles sr = new StaffRoles();
            sr.setStaffId(staffId);
            sr.setRoleId(roleId);
            staffRolesMapper.insertSelective(sr);
        }
    }
}
