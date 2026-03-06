package com.database.controller;

import com.database.service.ExcelImportService;
import com.database.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Excel 一键导入接口：支持按 sheet 自动识别并导入（产品、供应商、客户、采购、进项、销售、入库、出库）
 */
@RestController
@RequestMapping("/api/excel")
public class ExcelImportController {

    private final ExcelImportService excelImportService;

    @Autowired
    public ExcelImportController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    /**
     * POST /api/excel/init-product-categories: 初始化产品分类
     * 若 product_categories 表为空，导入产品前可先调用本接口
     * @return 200 OK，返回本次新增条数及提示信息
     */
    @PostMapping("/init-product-categories")
    public ResponseEntity<Result<Map<String, Object>>> initProductCategories() {
        int inserted = excelImportService.initProductCategories();
        Map<String, Object> data = Map.of(
                "inserted", inserted,
                "message", inserted > 0 ? "已初始化 " + inserted + " 个产品分类" : "产品分类已存在，无需初始化"
        );
        return ResponseEntity.ok(Result.success(data));
    }

    /**
     * POST /api/excel/import: 上传 Excel 文件并一键导入
     * 支持 sheet：产品、供应商、客户、采购、进项、销售、入库、出库
     * @param file 上传的 Excel 文件
     * @return 200 OK，返回各 sheet 导入条数及错误信息
     */
    @PostMapping("/import")
    public ResponseEntity<Result<Map<String, Object>>> importExcel(
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = excelImportService.importExcel(file);
        return ResponseEntity.ok(Result.success(result));
    }
}
