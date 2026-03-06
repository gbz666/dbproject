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