package com.database.controller;

import com.database.aop.RequireRole;
import com.database.dto.StaffCreateRequest;
import com.database.dto.StaffUpdateRequest;
import com.database.pojo.Roles;
import com.database.service.StaffService;
import com.database.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    @Autowired
    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    /**
     * GET /api/staff: 根据员工姓名查询员工ID
     */
    @GetMapping
    public ResponseEntity<Result<Long>> getStaffIdByName(@RequestParam String name) {
        Long staffId = staffService.getStaffIdByName(name);
        return ResponseEntity.ok(Result.success(staffId));
    }

    /**
     * GET /api/staff/list: 分页查询员工列表（含角色）
     */
    @RequireRole({"后台管理", "总经理"})
    @GetMapping("/list")
    public ResponseEntity<Result<Map<String, Object>>> listStaffs(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> page = staffService.findStaffPage(keyword, pageNum, pageSize);
        return ResponseEntity.ok(Result.success(page));
    }

    /**
     * GET /api/staff/roles: 获取所有可用角色
     */
    @RequireRole({"后台管理", "总经理"})
    @GetMapping("/roles")
    public ResponseEntity<Result<List<Roles>>> getAllRoles() {
        return ResponseEntity.ok(Result.success(staffService.getAllRoles()));
    }

    /**
     * POST /api/staff/create: 新建员工
     */
    @RequireRole({"后台管理", "总经理"})
    @PostMapping("/create")
    public ResponseEntity<Result<Void>> createStaff(
            @Valid @RequestBody StaffCreateRequest request,
            HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        staffService.createStaff(request, operatorId);
        Result<Void> result = Result.success();
        result.setMessage("员工创建成功");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/staff/{id}: 编辑员工信息
     */
    @RequireRole({"后台管理", "总经理"})
    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> updateStaff(
            @PathVariable Long id,
            @RequestBody StaffUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        staffService.updateStaff(id, request, operatorId);
        return ResponseEntity.ok(Result.success("员工信息更新成功"));
    }

    /**
     * POST /api/staff/{id}/reset-password: 重置员工密码
     */
    @RequireRole({"后台管理", "总经理"})
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Result.fail(400, "新密码不能为空"));
        }
        staffService.resetPassword(id, newPassword);
        return ResponseEntity.ok(Result.success("密码重置成功"));
    }

    /**
     * PUT /api/staff/{id}/status: 切换员工账户状态
     */
    @RequireRole({"后台管理", "总经理"})
    @PutMapping("/{id}/status")
    public ResponseEntity<Result<Void>> toggleStatus(@PathVariable Long id) {
        staffService.toggleStatus(id);
        return ResponseEntity.ok(Result.success("状态切换成功"));
    }
}
