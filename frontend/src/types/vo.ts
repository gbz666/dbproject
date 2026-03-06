/** 对应后端 CustomerDetailVO.java */
export interface CustomerDetailVO {
  id: number;
  customerCode: string;
  customerName: string;
  address?: string;
  phone?: string;
  email?: string;
  salesPersonName?: string;
  followUpPersonName?: string;
  ownerName?: string;
  paymentTermsDays?: number;
  paymentTermsNotes?: string;
  createdByName?: string;
  updatedByName?: string;
  createdAt: string; 
  updatedAt: string;
}
/**
 * 供应商详情返回 VO
 */
export interface SupplierDetailVO {
    id: number;
    supplierCode: string;
    supplierName: string;
    shortName: string;
    mainBusiness: string;
    taxNo: string;
    address: string;
    phone: string;
    email: string;
    salesPersonName: string;
    followUpPersonName: string;
    ownerName: string;
    createdAt: string;
    updatedAt: string;
    createdByStaffName: string;
    updatedByStaffName: string;
}

export interface ProductVO {
  id: number;
  productCode: string;
  productName: string;
  categoryName: string;
  sku: string;
  unit: string;
  specification: string;
  description: string;
  costPrice: number;
  listPrice: number;
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseOrderVO {
  id: number;
  purchaseCode: string;
  orderDate: string;
  supplierCode: string;
  supplierName: string;
  supplierId: number;
  note: string;
  totalAmount: number;
  items: PurchaseOrderItemVO[];
}

export interface PurchaseOrderItemVO {
  productCode: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}


export interface SalesOrderVO {
  id: number;
  orderCode: string;
  orderDate: string;
  customerCode: string;
  customerName: string;
  followUpPersonName: string;
  salesPersonName: string;
  ownerName: string;
  note: string;
  items: OrderItemVO[];
}

export interface OrderItemVO {
  productCode: string;
  productName: string; // 注意：对应后端 VO 中的 ProductName (驼峰可能需注意一致性)
  quantity: number;
  unitPrice: number;
  costPrice:number;
  salesCount: number;
  salesTotalCount: number;
}
/** 分页包装类 */
export interface PageInfo<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}


export interface BaseSelectVO{
  code:string,
  name:string
}

export interface WarehouseStockVO {
  warehouseId: number;
  warehouseName?: string;
  quantity: number;
}

export interface OutboundDetailVO {
  salesOrderCode: string;
  orderDate: string;
  customerName: string;
  productCode: string;
  orderQuantity: number;
  orderRemark: string;
  firstOutboundDate: string;
  warehouseDetails: WarehouseStockVO[]; // 仓库出货数量列表
  subTotal: number;
  pendingQuantity: number;
  serialNumbers: string;
}

export interface StockInVO {
  id: number;
  stockInCode: string;
  purchaseOrderCode: string;
  supplierName: string;
  orderDate: string; // 后端 Date 传到前端通常为 ISO 字符串
  productCode: string;
  productName: string;
  purchaseQuantity: number;
  subTotal: number;
  pendingQuantity: number;
  note: string;
  creatorName: string;
  warehouseDetails: WarehouseStockVO[];
}

// @/types/vo.ts
export interface InventoryVO {
  productId: number;
  productCode: string;
  productName: string;
  specification: string;
  unit: string;
  costPrice: number;
  // 平铺各仓库库存
  shInventory: number; // 上海
  tjInventory: number; // 天津
  szInventory: number; // 深圳
  totalInventory: number;
}

/** 进项发票明细行（列表/编辑用），对应后端 PurchaseInvoiceDetails */
export interface PurchaseInvoiceDetailVO {
  id?: number;
  invoiceId?: number;
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

/** 进项发票 VO，对应后端 PurchaseInvoiceVO；列表为发票全貌（总金额、明细数），按 id 查询带 details */
export interface PurchaseInvoiceVO {
  id: number;
  purchaseCode: string;
  invoiceNo: string;
  invoiceDate: string;
  supplierId?: number;
  supplierName: string;
  orderDate?: string;
  orderNote?: string;
  remark?: string;
  /** 列表展示：发票含税总金额、明细行数 */
  totalAmount?: number;
  detailCount?: number;
  /** 按 id 查询时由服务端填充首行明细，列表不展示 */
  itemName?: string;
  specification?: string;
  unit?: string;
  quantity?: number;
  unitPrice?: number;
  amountExclusiveTax?: number;
  taxRate?: number;
  taxAmount?: number;
  amountInclusiveTax?: number;
  lastInvoiceDate?: string;
  avgInvoiceDays?: number;
  totalInvoicedAmount?: number;
  invoiceCount?: number;
  pendingInvoiceAmount?: number;
  /** 按 id 查询时的完整明细列表 */
  details?: PurchaseInvoiceDetailVO[];
}

/** 销项发票 VO，对应后端 SalesInvoiceVO */
export interface SalesInvoiceVO {
  id: number;
  // 来自 DTO 的字段
  salesOrderCode: string;
  invoiceNo: string;
  invoiceDate: string;
  companyName: string;
  itemName: string;
  specification: string;
  unit: string;
  quantity: number;
  unitPriceInclusiveTax: number;
  amountInclusiveTax: number;
  amountExclusiveTax: number;
  taxAmount: number;
  remark?: string;

  // 统计字段
  avgInvoiceDays?: number;        // 平均开票时间
  pendingInvoiceAmount?: number;  // 未开金额
}