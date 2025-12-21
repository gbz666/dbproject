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