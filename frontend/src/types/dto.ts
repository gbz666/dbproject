import type { WarehouseStockVO } from "./vo";

/** 对应后端 CustomerCreateRequest.java */
export interface CustomerCreateRequest {
  customerName: string;
  address?: string;
  phone?: string;
  email?: string;
  salesPersonName?: string;
  followUpPersonName?: string;
  ownerName?: string;
  paymentTermsDays?: number;
  paymentTermsNotes?: string;
}

/** 对应后端 CustomerUpdateRequest.java */
export interface CustomerUpdateRequest {
  customerName?: string;
  address?: string;
  phone?: string;
  email?: string;
  salesPersonName?: string;
  followUpPersonName?: string;
  ownerName?: string;
  paymentTermsDays?: number;
  paymentTermsNotes?: string;
}

/**
 * 供应商创建/更新请求 DTO
 */
export interface SupplierRequest {
    supplierName: string;
    shortName: string;
    mainBusiness?: string;
    taxNo?: string;
    address?: string;
    phone?: string;
    email?: string;
    salesPersonName?: string;    // 映射到 staffs.id
    followUpPersonName?: string; // 映射到 staffs.id
    ownerName?: string;          // 映射到 staffs.id
}

export interface ProductRequest {
  id?: number | null; // 更新时必填
  productName: string;
  categoryName: string;
  sku: string;
  unit: string;
  specification: string;
  description: string;
  costPrice: number;
  listPrice: number;
}

export interface ProductQuery {
  pageNum: number;
  pageSize: number;
  productName?: string;
  categoryName?: string;
  productCode?: string;
}

export interface PurchaseOrderDto {
  id?: number;
  orderDate: string;
  supplierCode: string;
  note: string;
  items: PurchaseOrderItemDto[];
}

export interface PurchaseOrderItemDto {
  productCode: string;
  quantity: number;
  unitPrice: number;
  remark?: string;
}

export interface SalesOrderDTO {
  id?: number;
  orderDate: string;
  customerCode: string;
  note?: string;
  items: SalesOrderItemDTO[];
}

export interface SalesOrderItemDTO {
  productCode: string;
  quantity: number;
  unitPrice: number;
}

export interface OutboundItemDTO {
  productCode: string;
  warehouseDetails: WarehouseStockVO[]; // 对应后端引用的 WarehouseStockVO
  serialNumbers?: string;
  remark?: string;
}

/** 出库单创建/修改 DTO */
export interface OutboundOrderDTO {
  id?: number;
  salesOrderCode: string;
  outboundDate: string; // 后端 Date 类型，前端传 ISO 字符串或格式化日期
  remark?: string;
  items: OutboundItemDTO[];
}

export interface StockInItemDTO {
  productCode: string;
  serialNumbers: string;
  remark?: string;
  warehouseDetails: WarehouseStockVO[];
}

export interface StockInDTO {
  id?: number;
  purchaseOrderCode: string;
  stockInDate: string | Date;
  note?: string;
  items: StockInItemDTO[];
}

/** 进项发票查询条件，对应后端 PurchaseInvoiceQuery */
export interface PurchaseInvoiceQuery {
  pageNum: number;
  pageSize: number;
  companyName?: string;   // 公司名称
  supplierName?: string;   // 供应商名称
  productModel?: string;   // 产品型号（规格）
  purchaseCode?: string;   // 采购订单号
  itemName?: string;       // 货物/服务名称
}

/** 进项发票单行明细 DTO，对应后端 PurchaseInvoiceDetailItemDTO */
export interface PurchaseInvoiceDetailItemDTO {
  itemName: string;
  specification: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  amountExclusiveTax: number;
  taxRate: number;
  taxAmount: number;
  amountInclusiveTax: number;
  remark?: string;
}

/** 进项发票创建/更新 DTO，一张发票可多行明细，对应后端 PurchaseInvoiceDTO */
export interface PurchaseInvoiceDTO {
  id?: number;                 // 更新时使用
  purchaseCode: string;        // 采购订单号
  supplierName?: string;
  supplierId?: number;
  invoiceNo: string;           // 发票号
  invoiceDate: string;         // 发票日期 YYYY-MM-DD
  remark?: string;             // 发票备注
  items: PurchaseInvoiceDetailItemDTO[];  // 至少一行
}

/** 销项发票查询条件，对应后端 SalesInvoiceQuery */
export interface SalesInvoiceQuery {
  pageNum: number;
  pageSize: number;
  companyName?: string;       // 公司名称 / 客户名称
  productModel?: string;      // 产品型号（规格）
  salesOrderCode?: string;    // 销售订单号
  itemName?: string;          // 货物/服务名称
  invoiceNo?: string;         // 发票编码
}

/** 销项发票单行明细 */
export interface SalesInvoiceDetailItemDTO {
  itemName?: string;
  specification?: string;
  unit?: string;
  quantity?: number;
  unitPriceInclusiveTax?: number;
  amountExclusiveTax?: number;
  taxAmount?: number;
  amountInclusiveTax?: number;
  remark?: string;
}

/** 销项发票创建/更新 DTO，支持一票多明细：优先用 items；或传扁平字段（兼容单明细） */
export interface SalesInvoiceDTO {
  id?: number;
  salesOrderCode: string;
  invoiceNo: string;
  invoiceDate: string;
  companyName?: string;
  remark?: string;
  /** 多明细时使用，至少一行 */
  items?: SalesInvoiceDetailItemDTO[];
  /** 单明细兼容字段 */
  itemName?: string;
  specification?: string;
  unit?: string;
  quantity?: number;
  unitPriceInclusiveTax?: number;
  amountInclusiveTax?: number;
  amountExclusiveTax?: number;
  taxAmount?: number;
}

/** 收款记录查询条件 */
export interface PaymentReceiptQuery {
  pageNum: number;
  pageSize: number;
  customerName?: string;
  receiptNo?: string;
  method?: string;
}

/** 收款记录创建/更新 DTO */
export interface PaymentReceiptDTO {
  id?: number;
  receiptNo?: string;
  customerId?: number;
  salesInvoiceId?: number;
  amount?: number;
  receiptDate?: string;
  method?: string;
  remark?: string;
}

/** 付款记录查询条件 */
export interface PaymentExpenseQuery {
  pageNum: number;
  pageSize: number;
  supplierName?: string;
  paymentNo?: string;
  method?: string;
}

/** 付款记录创建/更新 DTO */
export interface PaymentExpenseDTO {
  id?: number;
  paymentNo?: string;
  supplierId?: number;
  purchaseInvoiceId?: number;
  amount?: number;
  paymentDate?: string;
  method?: string;
  remark?: string;
}