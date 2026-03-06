package com.database.controller;

import com.database.service.StaffService;
import com.database.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param name 员工姓名
     * @return 200 OK
     */
    @GetMapping
    public ResponseEntity<Result<Long>> getStaffIdByName(@RequestParam String name) {
        Long staffId = staffService.getStaffIdByName(name);
        return ResponseEntity.ok(Result.success(staffId));
    }
}
