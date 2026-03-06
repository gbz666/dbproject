package com.database.controller;

import com.database.service.StaffService;
import com.database.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffController {
    StaffService staffService;
    @Autowired
    StaffController(StaffService staffService) {
        this.staffService = staffService;
    }
    @GetMapping("/api/staff")
    public ResponseEntity<Result<Long>> getStaffs(@RequestParam String name){
        Result<Long> result = Result.success(staffService.getStaffIdByName(name));
        return ResponseEntity.ok(result);
    }
}
