package com.database.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.database.dto.excel.*;
import com.database.exception.BusinessException;
import com.database.mapper.*;
import com.database.pojo.*;
import com.database.util.ExcelNumberToStringConverter;
import com.database.util.ExcelRowReadHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Excel 一键导入服务（EasyExcel 注解绑定）
 * 按表头名绑定到 DTO（@ExcelProperty），按 sheet 名选择对应 head 类读取。
 * 支持表格只包含部分列。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final ExcelImportMapper excelImportMapper;
    private final ProductsMapper productsMapper;
    private final SuppliersMapper suppliersMapper;
    private final ContactsMapper contactsMapper;
    private final CustomersMapper customersMapper;
    private final PurchaseOrdersMapper purchaseOrdersMapper;
    private final PurchaseOrderItemsMapper purchaseOrderItemsMapper;
    private final PurchaseInvoicesMapper purchaseInvoicesMapper;
    private final PurchaseInvoiceDetailsMapper purchaseInvoiceDetailsMapper;
    private final SalesOrdersMapper salesOrdersMapper;
    private final SalesOrderItemsMapper salesOrderItemsMapper;
    private final SalesInvoicesMapper salesInvoicesMapper;
    private final SalesInvoiceDetailsMapper salesInvoiceDetailsMapper;
    private final StockInsMapper stockInsMapper;
    private final StockInItemsMapper stockInItemsMapper;
    private final InventoryMapper inventoryMapper;
    private final OutboundOrdersMapper outboundOrdersMapper;
    private final OutboundOrderItemsMapper outboundOrderItemsMapper;
    private final PaymentExpensesMapper paymentExpensesMapper;
    private final PaymentReceiptsMapper paymentReceiptsMapper;
    private final ProductCategoriesMapper productCategoriesMapper;
    private final StaffsMapper staffsMapper;

    /** 导入时若无指定员工则审计字段留空（不填默认值，与「没有就留空」一致） */
    private static final Long DEFAULT_STAFF_ID = null;
    private static final int DEFAULT_CATEGORY_ID = 8;

    /** 进项导入时用于兜底的“虚拟供应商”编码（请确保数据库中存在对应记录） */
    private static final String VIRTUAL_SUPPLIER_CODE = "VIRTUAL_SELF";

    private static final Map<String, Long> STAFF_MAP = Map.of(
            "高", 2L, "将", 3L, "清", 6L, "帆", 5L, "伟", 4L,
            "辉", 7L, "勇", 8L, "力", 9L
    );

    private static final Map<String, String> CATEGORY_MAP = Map.ofEntries(
            Map.entry("卡类", "卡类"), Map.entry("网卡", "卡类"), Map.entry("阵列卡", "卡类"), Map.entry("显卡", "卡类"),
            Map.entry("固态", "固态硬盘"), Map.entry("SSD", "固态硬盘"),
            Map.entry("机械", "机械硬盘"), Map.entry("HDD", "机械硬盘"),
            Map.entry("内存", "内存"), Map.entry("电源", "电源"),
            Map.entry("线材", "线材 数据线"), Map.entry("数据线", "线材 数据线"), Map.entry("HDMI", "线材 数据线"),
            Map.entry("CPU", "CPU"), Map.entry("处理器", "CPU"),
            Map.entry("其它", "其它"), Map.entry("其他", "其它")
    );

    private static final List<Map.Entry<String, String>> DEFAULT_CATEGORIES = List.of(
            Map.entry("卡类", "包含显卡、网卡、声卡及各类扩展适配卡"),
            Map.entry("固态硬盘", "包含 SATA、NVMe M.2 等协议的固态存储介质"),
            Map.entry("机械硬盘", "包含 3.5寸桌面级及 2.5寸笔记本级机械硬盘"),
            Map.entry("内存", "包含 DDR4、DDR5 等各类服务器与台式机内存条"),
            Map.entry("电源", "包含 ATX、SFX 规格及各类功率等级的电源供应器"),
            Map.entry("线材 数据线", "包含 HDMI、DP、SATA、USB 及各类内部跳线"),
            Map.entry("CPU", "包含 Intel、AMD 全系列中央处理器"),
            Map.entry("其它", "不属于上述明确分类的周边配件")
    );

    /**
     * 导入时 Sheet 的固定顺序：
     * 先基础数据（产品/供应商/客户），再业务单（采购/进项/销售/销项/入库/出库）。
     * 注意：必须先导入「销售」再导入「销项」，否则销项导入时找不到对应销售订单。
     */
    private static final List<String> IMPORT_SHEET_ORDER = List.of(
            "产品", "供应商", "客户",
            "采购", "进项",
            "销售", "销项",
            "入库", "出库",
            "付款", "收款"
    );

    /**
     * 各 Sheet 的表头所在行号（1-based：第 1 行=1，第 3 行=3）。
     * 未在此配置的 Sheet 默认为 1。
     */
    private static final Map<String, Integer> SHEET_HEAD_ROW = new LinkedHashMap<>();
    static {
        SHEET_HEAD_ROW.put("产品", 1);
        SHEET_HEAD_ROW.put("供应商", 1);
        SHEET_HEAD_ROW.put("客户", 1);
        SHEET_HEAD_ROW.put("采购", 3);
        SHEET_HEAD_ROW.put("进项", 3);
        SHEET_HEAD_ROW.put("销项", 3);
        SHEET_HEAD_ROW.put("销售", 3);
        SHEET_HEAD_ROW.put("入库", 3);
        SHEET_HEAD_ROW.put("出库", 3);
        SHEET_HEAD_ROW.put("付款", 3);
        SHEET_HEAD_ROW.put("收款", 3);
    }

    /** 导入过程计数器、错误收集与编码→ID 缓存（减少重复查库，加速几千条导入） */
    public static class ImportContext {
        public final AtomicInteger productsCount = new AtomicInteger(0);
        public final AtomicInteger suppliersCount = new AtomicInteger(0);
        public final AtomicInteger customersCount = new AtomicInteger(0);
        public final AtomicInteger purchaseCount = new AtomicInteger(0);
        /** 进项发票条数 */
        public final AtomicInteger purchaseInvoiceCount = new AtomicInteger(0);
        /** 销项发票条数 */
        public final AtomicInteger salesInvoiceCount = new AtomicInteger(0);
        public final AtomicInteger salesCount = new AtomicInteger(0);
        public final AtomicInteger stockInCount = new AtomicInteger(0);
        public final AtomicInteger outboundCount = new AtomicInteger(0);
        public final AtomicInteger paymentCount = new AtomicInteger(0);
        public final AtomicInteger receiptCount = new AtomicInteger(0);
        public final List<String> errors = new ArrayList<>();
        /** 供应商编码 -> 实体（用于更新或取 id） */
        public final Map<String, Suppliers> supplierCache = new HashMap<>();
        /** 客户编码 -> 实体 */
        public final Map<String, Customers> customerCache = new HashMap<>();
        /** 产品编码 -> product_id */
        public final Map<String, Long> productIdCache = new HashMap<>();
        /** 采购单号 -> purchase_order_id */
        public final Map<String, Long> purchaseOrderIdCache = new HashMap<>();
        /** 销售订单号 -> sales_order_id */
        public final Map<String, Long> salesOrderIdCache = new HashMap<>();
        /** 采购订单 id -> 入库单 id（同一采购单多次入库复用） */
        public final Map<Long, Long> stockInIdByPoIdCache = new HashMap<>();
        /** 采购订单 id -> 第一条明细的 product_id（入库用） */
        public final Map<Long, Long> productIdByPoIdCache = new HashMap<>();
        /** 客户名称 -> customer_id（出库按名称查） */
        public final Map<String, Long> customerIdByNameCache = new HashMap<>();
        /** 产品名称/型号 -> product_id（出库按名称查） */
        public final Map<String, Long> productIdByNameCache = new HashMap<>();
        /** 仓库名称关键字 -> warehouse_id（与 warehouses 表一致，避免外键失败） */
        public final Map<String, Long> warehouseIdByNameCache = new HashMap<>();
        /** 产品 id -> 实体（出库导入时用成本价填充销售明细金额） */
        public final Map<Long, Products> productCache = new HashMap<>();
    }

    @Transactional(rollbackFor = Exception.class)
    public int initProductCategories() {
        int inserted = 0;
        for (Map.Entry<String, String> e : DEFAULT_CATEGORIES) {
            if (excelImportMapper.selectCategoryIdByName(e.getKey()) != null) continue;
            ProductCategories cat = new ProductCategories();
            cat.setCategoryName(e.getKey());
            cat.setDescription(e.getValue());
            cat.setIsDeleted(0);
            productCategoriesMapper.insertSelective(cat);
            inserted++;
        }
        log.info("初始化产品分类: 本次新增 {} 条", inserted);
        return inserted;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要导入的 Excel 文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            throw new BusinessException("仅支持 .xlsx 或 .xls 格式");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }

        List<String> sheetNames = getSheetNames(bytes);
        ImportContext ctx = new ImportContext();

        int headRowDefault = 1;
        for (String sheetName : IMPORT_SHEET_ORDER) {
            int sheetIndex = sheetNames.indexOf(sheetName);
            if (sheetIndex < 0) continue;
            int headRow = SHEET_HEAD_ROW.getOrDefault(sheetName, headRowDefault);
            try {
                switch (sheetName) {
                    case "产品" -> readSheet(bytes, sheetIndex, ExcelProductRowDto.class, new ProductSheetListener(this, ctx), headRow);
                    case "供应商" -> readSheet(bytes, sheetIndex, ExcelSupplierRowDto.class, new SupplierSheetListener(this, ctx), headRow);
                    case "客户" -> readSheet(bytes, sheetIndex, ExcelCustomerRowDto.class, new CustomerSheetListener(this, ctx), headRow);
                    case "采购" -> readSheet(bytes, sheetIndex, ExcelPurchaseRowDto.class, new PurchaseSheetListener(this, ctx), headRow);
                    case "进项" -> readSheet(bytes, sheetIndex, ExcelPurchaseInvoiceRowDto.class, new PurchaseInvoiceSheetListener(this, ctx), headRow);
                    case "销项" -> readSheet(bytes, sheetIndex, ExcelSalesInvoiceRowDto.class, new SalesInvoiceSheetListener(this, ctx), headRow);
                    case "销售" -> readSheet(bytes, sheetIndex, ExcelSalesRowDto.class, new SalesSheetListener(this, ctx), headRow);
                    case "入库" -> readSheet(bytes, sheetIndex, ExcelStockInRowDto.class, new StockInSheetListener(this, ctx), headRow);
                    case "出库" -> readSheet(bytes, sheetIndex, ExcelOutboundRowDto.class, new OutboundSheetListener(this, ctx), headRow);
                    case "付款" -> readSheet(bytes, sheetIndex, ExcelPaymentRowDto.class, new PaymentSheetListener(this, ctx), headRow);
                    case "收款" -> readSheet(bytes, sheetIndex, ExcelReceiptRowDto.class, new ReceiptSheetListener(this, ctx), headRow);
                    default -> { }
                }
            } catch (Exception e) {
                log.warn("Sheet [{}] 读取异常: {}", sheetName, e.getMessage());
                ctx.errors.add(String.format("[%s] %s", sheetName, e.getMessage()));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("products", ctx.productsCount.get());
        result.put("suppliers", ctx.suppliersCount.get());
        result.put("customers", ctx.customersCount.get());
        result.put("purchases", ctx.purchaseCount.get());
        result.put("purchaseInvoices", ctx.purchaseInvoiceCount.get());
        result.put("salesInvoices", ctx.salesInvoiceCount.get());
        result.put("sales", ctx.salesCount.get());
        result.put("stockIns", ctx.stockInCount.get());
        result.put("outbounds", ctx.outboundCount.get());
        result.put("payments", ctx.paymentCount.get());
        result.put("receipts", ctx.receiptCount.get());
        // 聚合后的精简错误，用于前端友好展示
        result.put("errors", aggregateErrors(ctx.errors));
        // 原始逐行错误明细（含完整异常信息），便于排查导入异常
        result.put("rawErrors", ctx.errors);
        // 若存在错误，将完整错误明细写入日志文件，便于对照 Excel 数据排查
        if (!ctx.errors.isEmpty()) {
            String errorFile = writeErrorsToFile(ctx.errors, filename);
            result.put("errorFile", errorFile);
            log.warn("Excel 导入完成，存在 {} 条错误，详情见文件: {}", ctx.errors.size(), errorFile);
        } else {
            log.info("Excel 导入完成，无错误. 统计: products={}, suppliers={}, customers={}, purchases={}, purchaseInvoices={}, sales={}, stockIns={}, outbounds={}",
                    ctx.productsCount.get(), ctx.suppliersCount.get(), ctx.customersCount.get(),
                    ctx.purchaseCount.get(), ctx.purchaseInvoiceCount.get(), ctx.salesCount.get(),
                    ctx.stockInCount.get(), ctx.outboundCount.get());
        }
        result.put("message", "导入完成");
        return result;
    }

    /**
     * 将完整错误列表写入日志文件，方便人工对照 Excel 数据排查。
     * 文件路径示例: backend/logs/excel/excel-import-20260211-153045-数据源.log
     */
    private String writeErrorsToFile(List<String> errors, String originalFilename) {
        try {
            String ts = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date());
            String safeName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_") : "excel";
            String fileName = String.format("excel-import-%s-%s.log", ts, safeName);
            // 这里默认应用是在 backend 目录下启动，因此直接写到 backend/logs/excel/
            Path dir = Paths.get("logs", "excel");
            Files.createDirectories(dir);
            Path file = dir.resolve(fileName);
            Files.write(file, errors, StandardCharsets.UTF_8);
            return file.toAbsolutePath().toString();
        } catch (Exception e) {
            log.warn("写入 Excel 导入错误日志文件失败: {}", e.getMessage());
            return null;
        }
    }

    /** 按「表名+规范化错误信息」聚合，每类只留一条：表名 N 条失败: 短信息（示例: 第X行） */
    private static List<String> aggregateErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) return List.of();
        // key: "表名\t规范化短信息"  value: [示例行号, 条数]
        Map<String, int[]> grouped = new LinkedHashMap<>();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\[(.+?) 第(\\d+)行\\] (.+)$");
        for (String e : errors) {
            java.util.regex.Matcher m = p.matcher(e);
            String sheet;
            int rowNum;
            String rawMsg;
            if (m.matches()) {
                sheet = m.group(1);
                rowNum = Integer.parseInt(m.group(2));
                rawMsg = m.group(3);
            } else {
                if (e.startsWith("[") && e.contains("] ")) {
                    int bracketEnd = e.indexOf("] ");
                    sheet = e.substring(1, bracketEnd);
                    rawMsg = e.substring(bracketEnd + 2);
                } else {
                    sheet = "导入";
                    rawMsg = e;
                }
                rowNum = 0;
            }
            String shortMsg = normalizeErrorMsg(rawMsg);
            String key = sheet + "\t" + shortMsg;
            grouped.computeIfAbsent(key, k -> new int[]{rowNum, 0})[1]++;
            if (grouped.get(key)[0] == 0) grouped.get(key)[0] = rowNum;
        }
        List<String> out = new ArrayList<>();
        final int maxMsgLen = 80;
        for (Map.Entry<String, int[]> en : grouped.entrySet()) {
            String[] parts = en.getKey().split("\t", 2);
            String sheet = parts.length > 1 ? parts[0] : "导入";
            String msg = parts.length > 1 ? parts[1] : en.getKey();
            if (msg.length() > maxMsgLen) msg = msg.substring(0, maxMsgLen) + "…";
            int sampleRow = en.getValue()[0];
            int count = en.getValue()[1];
            if (sampleRow > 0) {
                out.add(String.format("[%s] %d 条失败: %s（示例: 第%d行）", sheet, count, msg, sampleRow));
            } else {
                out.add(String.format("[%s] %d 条失败: %s", sheet, count, msg));
            }
        }
        return out;
    }

    /** 规范化错误信息：取首行、统一同类描述便于合并为一条展示 */
    private static String normalizeErrorMsg(String msg) {
        if (msg == null) return "";
        String firstLine = msg.split("[\r\n]+")[0].trim();
        firstLine = firstLine.replaceAll(" 第\\d+行", " 第N行");
        firstLine = firstLine.replaceAll("Duplicate entry '[^']+'", "Duplicate entry '…'");
        if (firstLine.length() > 80) firstLine = firstLine.substring(0, 80);
        return firstLine;
    }

    private List<String> getSheetNames(byte[] bytes) {
        List<String> names = new ArrayList<>();
        try (InputStream is = new ByteArrayInputStream(bytes);
             Workbook wb = WorkbookFactory.create(is)) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                names.add(wb.getSheetName(i));
            }
        } catch (Exception e) {
            log.warn("获取 Sheet 名称失败，将尝试按索引读取: {}", e.getMessage());
        }
        return names;
    }

    /**
     * 读取指定 Sheet，表头行号可配置（1-based）。
     * @param headRowNumber 表头所在行号，1=第1行，3=第3行
     */
    private <T> void readSheet(byte[] bytes, int sheetIndex, Class<T> headClass, AnalysisEventListener<T> listener, int headRowNumber) {
        EasyExcel.read(new ByteArrayInputStream(bytes), headClass, listener)
                .registerConverter(new ExcelNumberToStringConverter())
                .sheet(sheetIndex)
                .headRowNumber(headRowNumber)
                .doRead();
    }

    /** 有值时按缩写解析员工 ID，空则返回 null（不填默认值） */
    private Long staffIdOrNull(String abbrev) {
        if (abbrev == null || abbrev.trim().isEmpty()) return null;
        String first = abbrev.trim().substring(0, Math.min(1, abbrev.trim().length()));
        Long id = STAFF_MAP.get(first);
        return id != null ? id : null;
    }

    /** 解析仓库 ID：先查缓存，再查库（名称或包含匹配），不存在则返回 null 并跳过该仓 */
    private Long resolveWarehouseId(String nameKey, ImportContext ctx) {
        if (nameKey == null || nameKey.isEmpty()) return null;
        return ctx.warehouseIdByNameCache.computeIfAbsent(nameKey,
                k -> excelImportMapper.selectWarehouseIdByName(k));
    }

    /** 空字符串视为 null，不填默认值 */
    private static String emptyToNull(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }

    private static Integer emptyToNullInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private int resolveCategoryId(String productName) {
        if (productName == null || productName.isEmpty()) return DEFAULT_CATEGORY_ID;
        String name = productName.toLowerCase();
        List<String> prefixes = new ArrayList<>(CATEGORY_MAP.keySet());
        prefixes.sort((a, b) -> Integer.compare(b.length(), a.length()));
        for (String pfx : prefixes) {
            if (name.contains(pfx.toLowerCase())) {
                String std = CATEGORY_MAP.get(pfx);
                Integer id = excelImportMapper.selectCategoryIdByName(std);
                return id != null ? id : DEFAULT_CATEGORY_ID;
            }
        }
        return DEFAULT_CATEGORY_ID;
    }

    // ----- 按 DTO 导入（由各 SheetListener 调用） -----

    void importProduct(ExcelProductRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        int catId = resolveCategoryId(dto.resolveProductName());
        if (productCategoriesMapper.selectByPrimaryKey(Long.valueOf(catId)) == null) {
            ctx.errors.add(String.format("[产品 第%d行] 分类不存在，已跳过", row));
            return;
        }
        Products p = new Products();
        p.setProductCode(dto.resolveProductCode());
        p.setProductName(dto.resolveProductName());
        p.setCategoryId(catId);
        p.setUnit("件");
        p.setDescription(dto.resolveDescription());
        p.setCreatedById(DEFAULT_STAFF_ID);
        p.setUpdatedById(DEFAULT_STAFF_ID);
        excelImportMapper.upsertProduct(p);
        ctx.productsCount.incrementAndGet();
    }

    void importSupplier(ExcelSupplierRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String code = dto.resolveSupplierCode();
        String name = dto.resolveSupplierName();
        if (code == null || code.isEmpty()) code = "SUP_" + System.currentTimeMillis();

        Long salesId = staffIdOrNull(dto.getSales());
        Long ownerId = staffIdOrNull(dto.getOwner());
        if (salesId != null && staffsMapper.selectByPrimaryKey(salesId) == null) {
            ctx.errors.add(String.format("[供应商 第%d行] 销售/归属人对应员工不存在，已跳过", row));
            return;
        }
        if (ownerId != null && staffsMapper.selectByPrimaryKey(ownerId) == null) {
            ctx.errors.add(String.format("[供应商 第%d行] 销售/归属人对应员工不存在，已跳过", row));
            return;
        }

        Suppliers existing = ctx.supplierCache.get(code);
        if (existing == null) {
            existing = suppliersMapper.selectBySupplierCode(code);
            if (existing != null) ctx.supplierCache.put(code, existing);
        }
        if (existing != null) {
            existing.setSupplierName(name);
            existing.setMainBusiness(dto.resolveMainBusiness());
            existing.setSalesPersonId(salesId);
            existing.setOwnerId(ownerId);
            existing.setUpdatedById(DEFAULT_STAFF_ID);
            suppliersMapper.updateSupplier(existing);
            Long supplierId = existing.getId();
                for (Object o : Arrays.asList(dto.getContact1(), dto.getContact2(), dto.getContact3())) {
                    String contactName = o == null ? null : o.toString().trim();
                    if (contactName != null && !contactName.isEmpty()) {
                        Contacts c = new Contacts();
                        c.setContactName(contactName);
                        c.setSupplierId(supplierId);
                        c.setCreatedById(DEFAULT_STAFF_ID);
                        c.setUpdatedById(DEFAULT_STAFF_ID);
                        contactsMapper.insert(c);
                    }
                }
        } else {
            Suppliers s = new Suppliers();
            s.setSupplierCode(code);
            s.setSupplierName(name);
            s.setMainBusiness(dto.resolveMainBusiness());
            s.setSalesPersonId(salesId);
            s.setOwnerId(ownerId);
            s.setCreatedById(DEFAULT_STAFF_ID);
            s.setUpdatedById(DEFAULT_STAFF_ID);
            suppliersMapper.insertSupplier(s);
            s.setSupplierCode(code);
            suppliersMapper.updateSupplierCode(s);
            Long supplierId = s.getId();
            ctx.supplierCache.put(code, s);
            for (Object o : Arrays.asList(dto.getContact1(), dto.getContact2(), dto.getContact3())) {
                String contactName = o == null ? null : o.toString().trim();
                if (contactName != null && !contactName.isEmpty()) {
                    Contacts c = new Contacts();
                    c.setContactName(contactName);
                    c.setSupplierId(supplierId);
                    c.setCreatedById(DEFAULT_STAFF_ID);
                    c.setUpdatedById(DEFAULT_STAFF_ID);
                    contactsMapper.insert(c);
                }
            }
        }
        ctx.suppliersCount.incrementAndGet();
    }

    void importCustomer(ExcelCustomerRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String code = dto.resolveCustomerCode();
        String name = dto.resolveCustomerName();

        Long salesId = staffIdOrNull(dto.getSales());
        Long followId = staffIdOrNull(dto.getFollowUp());
        Long ownerId = staffIdOrNull(dto.getOwner());
        if (salesId != null && staffsMapper.selectByPrimaryKey(salesId) == null) {
            ctx.errors.add(String.format("[客户 第%d行] 销售/跟单/归属人对应员工不存在，已跳过", row));
            return;
        }
        if (followId != null && staffsMapper.selectByPrimaryKey(followId) == null) {
            ctx.errors.add(String.format("[客户 第%d行] 销售/跟单/归属人对应员工不存在，已跳过", row));
            return;
        }
        if (ownerId != null && staffsMapper.selectByPrimaryKey(ownerId) == null) {
            ctx.errors.add(String.format("[客户 第%d行] 销售/跟单/归属人对应员工不存在，已跳过", row));
            return;
        }

        Customers existing = ctx.customerCache.get(code);
        if (existing == null) {
            existing = customersMapper.selectByCustomerCode(code);
            if (existing != null) ctx.customerCache.put(code, existing);
        }
        if (existing != null) {
            existing.setCustomerName(name);
            existing.setPaymentTermsDays(emptyToNullInt(dto.getPaymentTermsDays()));
            existing.setPaymentTermsNotes(emptyToNull(dto.getPaymentTermsNotes()));
            existing.setSalesPersonId(salesId);
            existing.setFollowUpPersonId(followId);
            existing.setOwnerId(ownerId);
            existing.setUpdatedById(DEFAULT_STAFF_ID);
            customersMapper.updateCustomer(existing);
        } else {
            Customers c = new Customers();
            c.setCustomerCode(code);
            c.setCustomerName(name);
            c.setPaymentTermsDays(emptyToNullInt(dto.getPaymentTermsDays()));
            c.setPaymentTermsNotes(emptyToNull(dto.getPaymentTermsNotes()));
            c.setSalesPersonId(salesId);
            c.setFollowUpPersonId(followId);
            c.setOwnerId(ownerId);
            c.setCreatedById(DEFAULT_STAFF_ID);
            c.setUpdatedById(DEFAULT_STAFF_ID);
            customersMapper.insertCustomer(c);
            ctx.customerCache.put(code, c);
        }
        ctx.customersCount.incrementAndGet();
    }

    void importPurchase(ExcelPurchaseRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String pCode = dto.resolvePurchaseCode();
        String sCode = dto.resolveSupplierCode();

        // 1) 解析供应商：
        //    - 如果 Excel 里是空 / N/A / #N/A 等，视为“无供应商”，直接挂到虚拟供应商
        //    - 否则优先按编码查真实供应商，查不到再尝试虚拟供应商
        Suppliers s = null;
        if (!isExcelNA(sCode)) {
            s = ctx.supplierCache.get(sCode);
            if (s == null) {
                s = suppliersMapper.selectBySupplierCode(sCode);
                if (s != null) ctx.supplierCache.put(sCode, s);
            }
        }
        if (s == null) {
            // Excel 上没有有效供应商编码，或者编码查不到真实供应商：挂到虚拟供应商
            Suppliers virtualSupplier = suppliersMapper.selectBySupplierCode(VIRTUAL_SUPPLIER_CODE);
            if (virtualSupplier == null) {
                ctx.errors.add(String.format(
                        "[采购 第%d行] 供应商编码[%s]未匹配，且未找到虚拟供应商(code=%s)，已跳过。请先插入虚拟供应商记录。",
                        row, sCode, VIRTUAL_SUPPLIER_CODE));
                return;
            }
            s = virtualSupplier;
            ctx.supplierCache.put(VIRTUAL_SUPPLIER_CODE, virtualSupplier);
            log.warn("Excel 采购导入: 采购单号 {} 第 {} 行供应商缺失/无效({})，已挂到虚拟供应商 [{}] (code={})",
                    pCode, row, sCode, virtualSupplier.getSupplierName(), virtualSupplier.getSupplierCode());
        }

        Long productId = ctx.productIdCache.get(dto.resolveProductCode());
        if (productId == null) {
            Products prod = productsMapper.selectByProductCode(dto.resolveProductCode());
            if (prod != null) {
                ctx.productIdCache.put(dto.resolveProductCode(), prod.getId());
                productId = prod.getId();
            }
        }
        if (productId == null) {
            ctx.errors.add(String.format("[采购 第%d行] 产品编码未匹配，已跳过", row));
            return;
        }

        BigDecimal qty = ExcelRowReadHelper.safeDecimal(firstNonEmpty(dto.getQuantity()), BigDecimal.ZERO);
        BigDecimal unitPrice = ExcelRowReadHelper.safeDecimal(firstNonEmpty(dto.getUnitPrice(), dto.getUnitPriceAlt()), BigDecimal.ZERO);
        BigDecimal totalAmt = qty.multiply(unitPrice);
        java.util.Date orderDate = ExcelRowReadHelper.parseDate(dto.resolveOrderDate());
        if (orderDate == null) orderDate = new java.util.Date();

        Long ownerId = DEFAULT_STAFF_ID;
        String remark = dto.getRemark();
        if (remark != null) {
            for (String k : STAFF_MAP.keySet()) {
                if (remark.contains(k)) {
                    ownerId = STAFF_MAP.get(k);
                    break;
                }
            }
        }

        Long orderId = ctx.purchaseOrderIdCache.get(pCode);
        if (orderId == null) {
            orderId = purchaseOrdersMapper.selectOrderIdByCode(pCode);
            if (orderId == null) {
                PurchaseOrders po = new PurchaseOrders();
                po.setPurchaseCode(pCode);
                po.setOrderDate(orderDate);
                po.setSupplierId(s.getId());
                po.setOrderStatus("completed");
                po.setTotalAmount(totalAmt);
                po.setNote(remark);
                po.setOwnerId(ownerId);
                po.setCreatedById(ownerId);
                po.setUpdatedById(ownerId);
                po.setIsDeleted(0);
                purchaseOrdersMapper.insert(po);
                orderId = po.getId();
            }
            ctx.purchaseOrderIdCache.put(pCode, orderId);
        }

        PurchaseOrderItems item = new PurchaseOrderItems();
        item.setPurchaseOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setRemark(remark);
        item.setCreatedById(ownerId);
        item.setUpdatedById(ownerId);
        purchaseOrderItemsMapper.insert(item);
        ctx.purchaseCount.incrementAndGet();
    }

    /**
     * 判断 Excel 中的字符串是否表示“空/无意义”的 N/A 值
     * 例如：空串、N/A、#N/A 等
     */
    private static boolean isExcelNA(String v) {
        if (v == null) return true;
        String s = v.trim();
        if (s.isEmpty()) return true;
        String up = s.toUpperCase();
        return "N/A".equals(up) || "#N/A".equals(up);
    }

    /** 按「进项发票」Excel 行导入 purchase_invoices + purchase_invoice_details */
    void importPurchaseInvoice(ExcelPurchaseInvoiceRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String pCode = dto.resolvePurchaseCode();
        if (pCode == null || pCode.isEmpty()) {
            ctx.errors.add(String.format("[进项 第%d行] 采购订单号为空，已跳过", row));
            return;
        }

        // 1. 定位采购订单
        Long poId = ctx.purchaseOrderIdCache.get(pCode);
        if (poId == null) {
            poId = purchaseOrdersMapper.selectOrderIdByCode(pCode);
            if (poId != null) ctx.purchaseOrderIdCache.put(pCode, poId);
        }
        if (poId == null) {
            ctx.errors.add(String.format("[进项 第%d行] 采购订单号未匹配，已跳过", row));
            return;
        }
        PurchaseOrders po = purchaseOrdersMapper.selectByPrimaryKey(poId);
        if (po == null) {
            ctx.errors.add(String.format("[进项 第%d行] 采购订单数据异常，已跳过", row));
            return;
        }

        // 1.1 解析并兜底供应商：
        //     - 若采购订单上的供应商 ID 为空或对应记录不存在/已删除，则挂到虚拟供应商
        Long supplierIdForInvoice;
        Suppliers poSupplier = null;
        if (po.getSupplierId() != null) {
            poSupplier = suppliersMapper.selectByPrimaryKey(po.getSupplierId());
        }
        if (poSupplier != null) {
            supplierIdForInvoice = poSupplier.getId();
        } else {
            // 采购单上没有有效供应商，尝试使用预先插入的“舟若上海信息科技有限公司”虚拟供应商
            Suppliers virtualSupplier = suppliersMapper.selectBySupplierCode(VIRTUAL_SUPPLIER_CODE);
            if (virtualSupplier == null) {
                ctx.errors.add(String.format(
                        "[进项 第%d行] 采购订单号 %s 对应供应商不存在，同时未找到虚拟供应商(code=%s)，已跳过。请先插入虚拟供应商记录。",
                        row, pCode, VIRTUAL_SUPPLIER_CODE));
                return;
            }
            supplierIdForInvoice = virtualSupplier.getId();
            log.warn("Excel 进项导入: 采购订单号 {} 第 {} 行供应商缺失，已挂到虚拟供应商 [{}] (code={})",
                    pCode, row, virtualSupplier.getSupplierName(), virtualSupplier.getSupplierCode());
        }

        // 2. 解析金额与日期（直接用字符串转 BigDecimal，避免中间经过 double 引起精度丢失）
        java.math.BigDecimal qty = parseDecimal(dto.getQuantity());
        java.math.BigDecimal unitPrice = parseDecimal(dto.getUnitPrice());
        java.math.BigDecimal amountExcl = parseDecimal(dto.getAmountExclusiveTax());
        java.math.BigDecimal taxRate = parseDecimal(dto.getTaxRate());
        java.math.BigDecimal taxAmount = parseDecimal(dto.getTaxAmount());
        java.math.BigDecimal amountIncl = parseDecimal(dto.getAmountInclusiveTax());
        java.util.Date invoiceDate = ExcelRowReadHelper.parseDate(dto.resolveInvoiceDate());

        // 3. 写入进项发票主表
        PurchaseInvoices inv = new PurchaseInvoices();
        String invoiceNo = "PI-" + pCode + "-" + row;
        if (invoiceNo.length() > 80) {
            invoiceNo = invoiceNo.substring(0, 80);
        }
        inv.setInvoiceNo(invoiceNo);
        inv.setSupplierId(supplierIdForInvoice);
        inv.setPurchaseOrderId(poId);
        inv.setRelatedStockInId(null);
        inv.setAmount(amountIncl);
        inv.setInvoiceDate(invoiceDate);
        inv.setStatus("unsettled");
        inv.setRemark(null);
        inv.setCreatedById(DEFAULT_STAFF_ID);
        inv.setUpdatedById(DEFAULT_STAFF_ID);
        inv.setIsDeleted(0);
        purchaseInvoicesMapper.insertSelective(inv);

        // 4. 写入进项发票明细
        PurchaseInvoiceDetails detail = new PurchaseInvoiceDetails();
        detail.setInvoiceId(inv.getId());
        detail.setItemName(dto.resolveItemName());
        detail.setSpecification(dto.resolveSpecification());
        detail.setUnit(dto.resolveUnit());
        detail.setQuantity(qty);
        detail.setUnitPrice(unitPrice);
        detail.setAmountExclusiveTax(amountExcl);
        detail.setTaxRate(taxRate);
        detail.setTaxAmount(taxAmount);
        detail.setAmountInclusiveTax(amountIncl);
        detail.setCreatedById(DEFAULT_STAFF_ID);
        detail.setUpdatedById(DEFAULT_STAFF_ID);
        purchaseInvoiceDetailsMapper.insertSelective(detail);

        log.debug("导入进项发票: 行={}, 采购订单号={}, 发票号={}, 含税金额={}",
                row, pCode, inv.getInvoiceNo(), amountIncl);
        ctx.purchaseInvoiceCount.incrementAndGet();
    }

    /** 按「销项发票」Excel 行导入 sales_invoices + sales_invoice_details */
    void importSalesInvoice(ExcelSalesInvoiceRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String soCode = dto.resolveSalesOrderCode();
        if (soCode == null || soCode.isEmpty()) {
            ctx.errors.add(String.format("[销项 第%d行] 销售订单号为空，已跳过", row));
            return;
        }

        // 1. 定位销售订单（销项导入必须在销售订单创建之后）
        Long soId = ctx.salesOrderIdCache.get(soCode);
        if (soId == null) {
            soId = salesOrdersMapper.selectOrderIdByCode(soCode);
            if (soId != null) ctx.salesOrderIdCache.put(soCode, soId);
        }
        if (soId == null) {
            ctx.errors.add(String.format("[销项 第%d行] 销售订单号未匹配，已跳过", row));
            return;
        }
        SalesOrders so = salesOrdersMapper.selectByPrimaryKey(soId);
        if (so == null || so.getCustomerId() == null) {
            ctx.errors.add(String.format("[销项 第%d行] 销售订单数据异常或客户缺失，已跳过", row));
            return;
        }

        Long customerId = so.getCustomerId();

        // 2. 解析金额与日期（沿用进项的高精度解析逻辑）
        java.math.BigDecimal qty = parseDecimal(dto.getQuantity());
        java.math.BigDecimal unitPriceIncl = parseDecimal(dto.getUnitPriceInclusiveTax());
        java.math.BigDecimal amountIncl = parseDecimal(dto.getAmountInclusiveTax());
        java.math.BigDecimal amountExcl = parseDecimal(dto.getAmountExclusiveTax());
        java.math.BigDecimal taxAmount = parseDecimal(dto.getTaxAmount());
        java.util.Date invoiceDate = ExcelRowReadHelper.parseDate(dto.resolveInvoiceDate());

        // 3. 写入销项发票主表
        SalesInvoices inv = new SalesInvoices();
        String invoiceNo = dto.getInvoiceNo();
        if (invoiceNo == null || invoiceNo.trim().isEmpty()) {
            invoiceNo = "SI-" + soCode + "-" + row;
        }
        if (invoiceNo.length() > 80) {
            invoiceNo = invoiceNo.substring(0, 80);
        }
        inv.setInvoiceNo(invoiceNo);
        inv.setCustomerId(customerId);
        inv.setSalesOrderId(soId);
        inv.setRelatedOutboundId(null);
        inv.setAmount(amountIncl);
        inv.setInvoiceDate(invoiceDate);
        inv.setStatus("unsettled");
        inv.setRemark(null);
        inv.setCreatedById(DEFAULT_STAFF_ID);
        inv.setUpdatedById(DEFAULT_STAFF_ID);
        inv.setIsDeleted(0);
        salesInvoicesMapper.insertSelective(inv);

        // 4. 写入销项发票明细
        SalesInvoiceDetails detail = new SalesInvoiceDetails();
        detail.setInvoiceId(inv.getId());
        detail.setItemName(dto.resolveItemName());
        detail.setSpecification(dto.resolveSpecification());
        detail.setUnit(dto.resolveUnit());
        detail.setQuantity(qty);
        // 这里将「含税单价」按明细单价入库
        detail.setUnitPrice(unitPriceIncl);
        detail.setAmountExclusiveTax(amountExcl);
        // 销项税率统一按 13%，若业务有变可改为从配置或 Excel 读取
        detail.setTaxRate(new java.math.BigDecimal("0.13"));
        detail.setTaxAmount(taxAmount);
        detail.setAmountInclusiveTax(amountIncl);
        detail.setCreatedById(DEFAULT_STAFF_ID);
        detail.setUpdatedById(DEFAULT_STAFF_ID);
        salesInvoiceDetailsMapper.insertSelective(detail);

        log.debug("导入销项发票: 行={}, 销售订单号={}, 发票号={}, 含税金额={}",
                row, soCode, inv.getInvoiceNo(), amountIncl);
        ctx.salesInvoiceCount.incrementAndGet();
    }

    /**
     * 专供「进项」导入使用的高精度金额解析：
     *  - 支持最多 8 位小数
     *  - 自动去掉千分位逗号、货币符号（￥/¥ 等）以及空格
     *  - 若带百分号（例如 13%），自动转成 0.13 形式存库
     *  - 不通过 double 中转，避免精度丢失
     */
    private static java.math.BigDecimal parseDecimal(String raw) {
        if (raw == null) return java.math.BigDecimal.ZERO;
        String s = raw.trim();
        if (s.isEmpty()) return java.math.BigDecimal.ZERO;

        boolean isPercent = s.contains("%");
        // 去掉百分号
        s = s.replace("%", "");
        // 去掉空格、中文空格
        s = s.replace("\u00A0", "").replace(" ", "");
        // 去掉常见货币符号
        s = s.replace("￥", "").replace("¥", "").replace("$", "");
        // 去掉千分位逗号
        s = s.replace(",", "");
        // 只保留数字、小数点和负号，防止 Excel 里混入其它符号
        s = s.replaceAll("[^0-9.\\-]", "");

        if (s.isEmpty() || ".".equals(s) || "-".equals(s) || "-.".equals(s)) {
            return java.math.BigDecimal.ZERO;
        }

        try {
            java.math.BigDecimal val = new java.math.BigDecimal(s);
            if (isPercent) {
                // 百分号字段（例如税率）按 13% -> 0.13 处理，保留最多 8 位
                val = val.movePointLeft(2);
            }
            return val.setScale(8, java.math.RoundingMode.HALF_UP);
        } catch (Exception e) {
            // 解析失败时返回 0，避免导入中断；详细问题已通过错误日志暴露
            return java.math.BigDecimal.ZERO;
        }
    }

    void importSales(ExcelSalesRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String orderCode = dto.resolveOrderCode();

        Customers cust = ctx.customerCache.get(dto.resolveCustomerCode());
        if (cust == null) {
            cust = customersMapper.selectByCustomerCode(dto.resolveCustomerCode());
            if (cust != null) ctx.customerCache.put(dto.resolveCustomerCode(), cust);
        }
        Long productId = ctx.productIdCache.get(dto.resolveProductCode());
        if (productId == null) {
            Products prod = productsMapper.selectByProductCode(dto.resolveProductCode());
            if (prod != null) {
                ctx.productIdCache.put(dto.resolveProductCode(), prod.getId());
                productId = prod.getId();
            }
        }
        if (cust == null || productId == null) {
            ctx.errors.add(String.format("[销售 第%d行] 客户或产品编码未匹配，已跳过", row));
            return;
        }

        BigDecimal qty = ExcelRowReadHelper.safeDecimal(dto.getQuantity(), BigDecimal.ZERO);
        BigDecimal unitPrice = ExcelRowReadHelper.safeDecimal(dto.resolveUnitPrice(), BigDecimal.ZERO);
        BigDecimal costPrice = ExcelRowReadHelper.safeDecimal(dto.resolveCostPrice(), BigDecimal.ZERO);
        if (unitPrice.compareTo(BigDecimal.ZERO) == 0) {
            Products prod = ctx.productCache.get(productId);
            if (prod == null) {
                prod = productsMapper.selectByPrimaryKey(productId);
                if (prod != null) ctx.productCache.put(productId, prod);
            }
            if (prod != null && prod.getCostPrice() != null) unitPrice = prod.getCostPrice();
        }
        java.util.Date orderDate = ExcelRowReadHelper.parseDate(dto.resolveOrderDate());
        if (orderDate == null) orderDate = new java.util.Date();

        Long followId = staffIdOrNull(dto.resolveFollowUp());

        Long orderId = ctx.salesOrderIdCache.get(orderCode);
        if (orderId == null) {
            orderId = salesOrdersMapper.selectOrderIdByCode(orderCode);
            if (orderId == null) {
                SalesOrders so = new SalesOrders();
                so.setOrderCode(orderCode);
                so.setOrderDate(orderDate);
                so.setCustomerId(cust.getId());
                so.setSalesPersonId(2L);
                so.setFollowUpPersonId(followId);
                so.setOrderStatus("completed");
                so.setCreatedById(DEFAULT_STAFF_ID);
                so.setUpdatedById(DEFAULT_STAFF_ID);
                salesOrdersMapper.insertSelective(so);
                orderId = so.getId();
            }
            ctx.salesOrderIdCache.put(orderCode, orderId);
        }

        SalesOrderItems item = new SalesOrderItems();
        item.setSalesOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setCostPrice(costPrice);
        item.setRemark(dto.resolveRemark());
        item.setCreatedById(DEFAULT_STAFF_ID);
        salesOrderItemsMapper.insert(item);

        if (costPrice.compareTo(BigDecimal.ZERO) > 0) {
            productsMapper.updateProductCostPrice(productId, costPrice);
        }
        ctx.salesCount.incrementAndGet();
    }

    void importStockIn(ExcelStockInRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String poCode = dto.resolvePurchaseOrderCode();

        Long poId = ctx.purchaseOrderIdCache.get(poCode);
        if (poId == null) {
            poId = purchaseOrdersMapper.selectOrderIdByCode(poCode);
            if (poId != null) ctx.purchaseOrderIdCache.put(poCode, poId);
        }
        if (poId == null) {
            ctx.errors.add(String.format("[入库 第%d行] 采购单号未匹配，已跳过", row));
            return;
        }

        Long productId = ctx.productIdByPoIdCache.get(poId);
        if (productId == null) {
            List<PurchaseOrderItems> items = purchaseOrderItemsMapper.selectByOrderId(poId);
            if (items == null || items.isEmpty()) {
                ctx.errors.add(String.format("[入库 第%d行] 采购单无明细，已跳过", row));
                return;
            }
            productId = items.get(0).getProductId();
            ctx.productIdByPoIdCache.put(poId, productId);
        }

        Long siId = ctx.stockInIdByPoIdCache.get(poId);
        StockIns existing = siId != null ? null : stockInsMapper.selectByPurchaseOrderId(poId);
        java.util.Date stockInDate = ExcelRowReadHelper.parseDate(dto.resolveStockInDate());
        if (siId == null) {
            if (existing == null) {
                StockIns si = new StockIns();
                si.setStockInCode("SI-" + poCode);
                si.setPurchaseOrderId(poId);
                si.setStockInDate(stockInDate);
                si.setCreatedById(DEFAULT_STAFF_ID);
                stockInsMapper.insert(si);
                siId = si.getId();
            } else {
                siId = existing.getId();
                if (stockInDate != null) {
                    existing.setStockInDate(stockInDate);
                    stockInsMapper.updateByPrimaryKeySelective(existing);
                }
            }
            ctx.stockInIdByPoIdCache.put(poId, siId);
        } else if (existing != null && stockInDate != null) {
            existing.setStockInDate(stockInDate);
            stockInsMapper.updateByPrimaryKeySelective(existing);
        }

        Map<String, String> qtyMap = Map.of(
                "上海", dto.resolveQtyShanghai() != null ? dto.resolveQtyShanghai() : "",
                "天津", dto.resolveQtyTianjin() != null ? dto.resolveQtyTianjin() : "",
                "深圳", dto.resolveQtyShenzhen() != null ? dto.resolveQtyShenzhen() : ""
        );
        for (String key : List.of("上海", "天津", "深圳")) {
            String qtyStr = qtyMap.get(key);
            BigDecimal qty = ExcelRowReadHelper.safeDecimal(qtyStr, BigDecimal.ZERO);
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;
            Long whId = resolveWarehouseId(key, ctx);
            if (whId == null) continue;
            StockInItems sii = new StockInItems();
            sii.setStockInId(siId);
            sii.setProductId(productId);
            sii.setWarehouseId(whId);
            sii.setQuantity(qty);
            sii.setCreatedById(DEFAULT_STAFF_ID);
            stockInItemsMapper.insert(sii);
            inventoryMapper.updateInventory(productId, whId, qty);
        }
        ctx.stockInCount.incrementAndGet();
    }

    void importOutbound(ExcelOutboundRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String orderCode = dto.resolveOrderCode();

        String custName = dto.resolveCustomerName();
        Long customerId = ctx.customerIdByNameCache.get(custName);
        if (customerId == null) {
            customerId = excelImportMapper.selectCustomerIdByName(custName);
            if (customerId != null) ctx.customerIdByNameCache.put(custName, customerId);
        }
        String prodName = dto.resolveProductName();
        Long productId = ctx.productIdByNameCache.get(prodName);
        if (productId == null) {
            productId = excelImportMapper.selectProductIdByName(prodName);
            if (productId != null) ctx.productIdByNameCache.put(prodName, productId);
        }
        if (customerId == null || productId == null) {
            ctx.errors.add(String.format("[出库 第%d行] 客户或产品名称未匹配，已跳过", row));
            return;
        }

        Long soId = ctx.salesOrderIdCache.get(orderCode);
        if (soId == null) {
            soId = salesOrdersMapper.selectOrderIdByCode(orderCode);
            if (soId != null) ctx.salesOrderIdCache.put(orderCode, soId);
        }
        if (soId == null) {
            SalesOrders so = new SalesOrders();
            so.setOrderCode(orderCode);
            java.util.Date od = ExcelRowReadHelper.parseDate(dto.getOrderDate());
            so.setOrderDate(od != null ? od : new java.util.Date());
            so.setCustomerId(customerId);
            so.setOrderStatus("shipped");
            so.setNote(dto.getNote());
            so.setCreatedById(DEFAULT_STAFF_ID);
            so.setUpdatedById(DEFAULT_STAFF_ID);
            salesOrdersMapper.insertSelective(so);
            soId = so.getId();
            ctx.salesOrderIdCache.put(orderCode, soId);
            SalesOrderItems item = new SalesOrderItems();
            item.setSalesOrderId(soId);
            item.setProductId(productId);
            BigDecimal orderQty = ExcelRowReadHelper.safeDecimal(dto.getOrderQty(), BigDecimal.ZERO);
            item.setQuantity(orderQty);
            Products prod = ctx.productCache.get(productId);
            if (prod == null) {
                prod = productsMapper.selectByPrimaryKey(productId);
                if (prod != null) ctx.productCache.put(productId, prod);
            }
            BigDecimal unitPrice = (prod != null && prod.getCostPrice() != null) ? prod.getCostPrice() : BigDecimal.ZERO;
            item.setUnitPrice(unitPrice);
            item.setCostPrice(unitPrice);
            item.setCreatedById(DEFAULT_STAFF_ID);
            salesOrderItemsMapper.insert(item);
        }

        String outboundCode = "OUT-" + orderCode;
        OutboundOrders existingOut = outboundOrdersMapper.selectByOutboundCode(outboundCode);
        Long outId;
        if (existingOut != null) {
            outId = existingOut.getId();
        } else {
            OutboundOrders out = new OutboundOrders();
            out.setOutboundCode(outboundCode);
            out.setSalesOrderId(soId);
            out.setOutboundDate(ExcelRowReadHelper.parseDate(dto.getOutboundDate()));
            out.setStatus("completed");
            out.setCreatedById(DEFAULT_STAFF_ID);
            out.setUpdatedById(DEFAULT_STAFF_ID);
            out.setIsDeleted(0);
            outboundOrdersMapper.insert(out);
            outId = out.getId();
        }

        BigDecimal qtyShanghai = ExcelRowReadHelper.safeDecimal(dto.getQtyShanghai(), BigDecimal.ZERO);
        BigDecimal qtyTianjin = ExcelRowReadHelper.safeDecimal(dto.getQtyTianjin(), BigDecimal.ZERO);
        BigDecimal qtyShenzhen = ExcelRowReadHelper.safeDecimal(dto.getQtyShenzhen(), BigDecimal.ZERO);
        for (Map.Entry<String, BigDecimal> e : Map.of("上海", qtyShanghai, "天津", qtyTianjin, "深圳", qtyShenzhen).entrySet()) {
            if (e.getValue().compareTo(BigDecimal.ZERO) > 0) {
                Long whId = resolveWarehouseId(e.getKey(), ctx);
                if (whId == null) continue;
                OutboundOrderItems oi = new OutboundOrderItems();
                oi.setOutboundOrderId(outId);
                oi.setProductId(productId);
                oi.setWarehouseId(whId);
                oi.setQuantity(e.getValue());
                oi.setCreatedById(DEFAULT_STAFF_ID);
                oi.setUpdatedById(DEFAULT_STAFF_ID);
                outboundOrderItemsMapper.insert(oi);
                inventoryMapper.updateInventory(productId, whId, e.getValue().negate());
            }
        }
        ctx.outboundCount.incrementAndGet();
    }

    void importPayment(ExcelPaymentRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String pCode = dto.resolvePurchaseCode();

        Long poId = ctx.purchaseOrderIdCache.get(pCode);
        if (poId == null) {
            poId = purchaseOrdersMapper.selectOrderIdByCode(pCode);
            if (poId != null) ctx.purchaseOrderIdCache.put(pCode, poId);
        }
        if (poId == null) {
            ctx.errors.add(String.format("[付款 第%d行] 采购订单号[%s]未匹配，已跳过", row, pCode));
            return;
        }

        PurchaseOrders po = purchaseOrdersMapper.selectByPrimaryKey(poId);
        if (po == null) {
            ctx.errors.add(String.format("[付款 第%d行] 采购订单数据异常，已跳过", row));
            return;
        }

        BigDecimal amount = parseDecimal(dto.getAmount());
        java.util.Date paymentDate = ExcelRowReadHelper.parseDate(dto.resolvePaymentDate());

        PaymentExpenses pe = new PaymentExpenses();
        String paymentNo = "PE-" + pCode + "-" + row;
        if (paymentNo.length() > 80) paymentNo = paymentNo.substring(0, 80);
        pe.setPaymentNo(paymentNo);
        pe.setSupplierId(po.getSupplierId());
        pe.setPurchaseInvoiceId(null);
        pe.setAmount(amount);
        pe.setPaymentDate(paymentDate);
        String methodRemark = dto.resolveMethodOrRemark();
        pe.setMethod(methodRemark != null ? "bank" : null);
        pe.setRemark(methodRemark);
        pe.setCreatedById(DEFAULT_STAFF_ID);
        pe.setUpdatedById(DEFAULT_STAFF_ID);
        pe.setIsDeleted(0);
        paymentExpensesMapper.insertSelective(pe);

        ctx.paymentCount.incrementAndGet();
    }

    void importReceipt(ExcelReceiptRowDto dto, ImportContext ctx, int row) {
        if (dto == null || !dto.isValid()) return;
        String soCode = dto.resolveSalesOrderCode();

        Long soId = ctx.salesOrderIdCache.get(soCode);
        if (soId == null) {
            soId = salesOrdersMapper.selectOrderIdByCode(soCode);
            if (soId != null) ctx.salesOrderIdCache.put(soCode, soId);
        }
        if (soId == null) {
            ctx.errors.add(String.format("[收款 第%d行] 销售订单号[%s]未匹配，已跳过", row, soCode));
            return;
        }

        SalesOrders so = salesOrdersMapper.selectByPrimaryKey(soId);
        if (so == null || so.getCustomerId() == null) {
            ctx.errors.add(String.format("[收款 第%d行] 销售订单数据异常或客户缺失，已跳过", row));
            return;
        }

        BigDecimal amount = parseDecimal(dto.getAmount());
        java.util.Date receiptDate = ExcelRowReadHelper.parseDate(dto.resolveReceiptDate());

        PaymentReceipts pr = new PaymentReceipts();
        String receiptNo = "PR-" + soCode + "-" + row;
        if (receiptNo.length() > 80) receiptNo = receiptNo.substring(0, 80);
        pr.setReceiptNo(receiptNo);
        pr.setCustomerId(so.getCustomerId());
        pr.setSalesInvoiceId(null);
        pr.setAmount(amount);
        pr.setReceiptDate(receiptDate);
        pr.setMethod("bank");
        pr.setRemark(dto.resolveRemark());
        pr.setCreatedById(DEFAULT_STAFF_ID);
        pr.setUpdatedById(DEFAULT_STAFF_ID);
        pr.setIsDeleted(0);
        paymentReceiptsMapper.insertSelective(pr);

        ctx.receiptCount.incrementAndGet();
    }

    private static Object firstNonNull(Object... objs) {
        if (objs == null) return null;
        for (Object o : objs) if (o != null) return o;
        return null;
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    // ----- 各 Sheet 的 EasyExcel 监听器（注解绑定的 DTO 逐行回调） -----

    private static class ProductSheetListener extends AnalysisEventListener<ExcelProductRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        ProductSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelProductRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importProduct(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[产品 第%d行] %s", row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class SupplierSheetListener extends AnalysisEventListener<ExcelSupplierRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        SupplierSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelSupplierRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importSupplier(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                String msg = e.getMessage() != null ? e.getMessage() : (e.getClass().getSimpleName() + (e.getCause() != null ? ": " + e.getCause().getMessage() : ""));
                ctx.errors.add(String.format("[供应商 第%d行] %s", row, msg));
                log.debug("[供应商 第{}行] 导入异常", row, e);
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class CustomerSheetListener extends AnalysisEventListener<ExcelCustomerRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        CustomerSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelCustomerRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importCustomer(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[客户 第%d行] %s", row, e.getMessage()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class PurchaseSheetListener extends AnalysisEventListener<ExcelPurchaseRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        PurchaseSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelPurchaseRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importPurchase(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[采购 第%d行] %s", row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    /** 进项发票 Sheet 监听器 */
    private static class PurchaseInvoiceSheetListener extends AnalysisEventListener<ExcelPurchaseInvoiceRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        PurchaseInvoiceSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelPurchaseInvoiceRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) {
                    service.importPurchaseInvoice(data, ctx, context.readRowHolder().getRowIndex() + 1);
                }
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[进项 第%d行] %s",
                        row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    /** 销项发票 Sheet 监听器 */
    private static class SalesInvoiceSheetListener extends AnalysisEventListener<ExcelSalesInvoiceRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        SalesInvoiceSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelSalesInvoiceRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) {
                    service.importSalesInvoice(data, ctx, context.readRowHolder().getRowIndex() + 1);
                }
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[销项 第%d行] %s",
                        row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class SalesSheetListener extends AnalysisEventListener<ExcelSalesRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        SalesSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelSalesRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importSales(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[销售 第%d行] %s", row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class StockInSheetListener extends AnalysisEventListener<ExcelStockInRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        StockInSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelStockInRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importStockIn(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[入库 第%d行] %s", row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class OutboundSheetListener extends AnalysisEventListener<ExcelOutboundRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        OutboundSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelOutboundRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importOutbound(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[出库 第%d行] %s", row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class PaymentSheetListener extends AnalysisEventListener<ExcelPaymentRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        PaymentSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelPaymentRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importPayment(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[付款 第%d行] %s", row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }

    private static class ReceiptSheetListener extends AnalysisEventListener<ExcelReceiptRowDto> {
        private final ExcelImportService service;
        private final ImportContext ctx;

        ReceiptSheetListener(ExcelImportService service, ImportContext ctx) {
            this.service = service;
            this.ctx = ctx;
        }

        @Override
        public void invoke(ExcelReceiptRowDto data, AnalysisContext context) {
            try {
                if (data != null && data.isValid()) service.importReceipt(data, ctx, context.readRowHolder().getRowIndex() + 1);
            } catch (Exception e) {
                int row = context.readRowHolder().getRowIndex() + 1;
                ctx.errors.add(String.format("[收款 第%d行] %s", row, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {}
    }
}
