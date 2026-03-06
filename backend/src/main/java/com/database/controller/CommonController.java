package com.database.controller;

import com.database.pojo.Warehouses;
import com.database.service.CommonService;
import com.database.vo.BaseSelectVO;
import com.database.vo.Result;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/util")
public class CommonController {

    private final CommonService commonService;

    @Autowired
    public CommonController(CommonService commonService) {
        this.commonService = commonService;
    }

    /**
     * GET /api/util/search/customer: 分页搜索客户（下拉/选择用）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键字（可选）
     * @return 200 OK
     */
    @GetMapping("/search/customer")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> getCustomers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        PageInfo<BaseSelectVO> page = commonService.getCustomerPage(pageNum, pageSize, keyword);
        return ResponseEntity.ok(Result.success(page));
    }

    /**
     * GET /api/util/search/product: 分页搜索产品（下拉/选择用）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键字
     * @return 200 OK
     */
    @GetMapping("/search/product")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> searchProduct(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String keyword) {
        PageInfo<BaseSelectVO> list = commonService.getProductPage(pageNum, pageSize, keyword);
        return ResponseEntity.ok(Result.success(list));
    }

    /**
     * GET /api/util/search/supplier: 分页搜索供应商（下拉/选择用）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键字
     * @return 200 OK
     */
    @GetMapping("/search/supplier")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> searchSupplier(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String keyword) {
        PageInfo<BaseSelectVO> list = commonService.getSupplierPage(pageNum, pageSize, keyword);
        return ResponseEntity.ok(Result.success(list));
    }

    /**
     * GET /api/util/search/productType: 分页搜索产品类型（下拉/选择用）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键字
     * @return 200 OK
     */
    @GetMapping("/search/productType")
    public ResponseEntity<Result<PageInfo<BaseSelectVO>>> searchProductType(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String keyword) {
        PageInfo<BaseSelectVO> list = commonService.getProductType(pageNum, pageSize, keyword);
        return ResponseEntity.ok(Result.success(list));
    }

    /**
     * GET /api/util/warehouses: 查询未删除的仓库列表（出库/入库下拉用）
     * @return 200 OK，列表项含 id, warehouseCode, warehouseName, location 等
     */
    @GetMapping("/warehouses")
    public ResponseEntity<Result<List<Warehouses>>> getWarehouses() {
        List<Warehouses> list = commonService.getWarehouseList();
        return ResponseEntity.ok(Result.success(list));
    }
}
