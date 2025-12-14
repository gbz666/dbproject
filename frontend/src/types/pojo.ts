// src/types/pojo.d.ts

/** 员工 (staffs) 简化类型 */
export interface Staff {
    id: number;
    staffName: string;
}

/** 客户 (Customers) 基础类型 - 对应数据库 Customers 表字段 */
export interface Customer {
    id: number;
    customerCode: string;
    customerName: string;
    address: string;
    phone: string;
    email: string;
    
    // 关联 ID 字段
    salesPersonId: number | null;
    followUpPersonId: number | null;
    ownerId: number | null;
    createdById: number | null; // 假设 Customers POJO 包含这些 ID
    updatedById: number | null; // 假设 Customers POJO 包含这些 ID
    
    // 账期与时间
    paymentTermsDays: number;
    paymentTermsNotes: string | null;
    createdAt: string;
    updatedAt: string;
}


// ===============================================
// 2. 客户详情 DTO 类型 (对应 Java 后端的 CustomerDetailDTO extends Customers)
// ===============================================

/** * 客户详情数据传输对象 (CustomerDetailDTO)
 * 继承自 Customer，并增加了关联查询得到的员工姓名。
 */
export interface CustomerDetailDTO extends Customer { 
    /** 负责销售的员工姓名 */
    salesPersonName: string | null; 
    
    /** 跟进人员工姓名 */
    followUpPersonName: string | null; 
    
    /** 客户归属人姓名 */
    ownerName: string | null;
}
/** 供应商 (suppliers) 类型 */
export interface Supplier {
    id: number;
    supplierCode: string;
    supplierName: string;
    shortName: string | null;
    mainBusiness: string | null;
    taxNo: string | null;
    address: string | null;
    phone: string | null;
    email: string | null;
    salesPersonId: number | null;
    followUpPersonId: number | null;
    ownerId: number | null;
    createdAt: string;
    updatedAt: string;
    // 假设可以附带关联的员工信息
    salesPerson?: Staff;
    followUpPerson?: Staff;
    owner?: Staff;
}