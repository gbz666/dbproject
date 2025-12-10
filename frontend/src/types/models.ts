// frontend/src/types/models.ts
export interface Customer {
  customer_id: number;
  customer_name: string;
  sales_person: string;
  follow_up_person: string;
  performance_owner: string;
  payment_terms_days: number;
  payment_terms_notes: string;
}

export interface Supplier {
  supplier_id: number;
  supplier_name: string;
  contact1: string;
  contact2: string;
  contact3: string;
  main_business: string;
  sales_person: string;
  follow_up_person: string;
  performance_owner: string;
}

export interface Product {
  product_id: number;
  product_name: string;
  product_description: string;
  category: string;
  created_at: string;
}

export interface SalesOrder {
    // 修正：order_id 在创建时可能没有，因此标记为可选
    order_id?: number; 
    // 修正：order_date 应该使用 ISO 格式字符串 (new Date().toISOString())
    order_date: string; 
    customer_id: number;
    product_id: number;
    quantity: number;
    unit_price: number;
    total_amount: number;
    cost_price: number;
    follow_up_person: string;
    customer_owner: string;
    performance_owner: string;
    purchase_subtotal: number;
    data_category: string;
    notes: string;
    created_at?: string; 
    status?: 'pending' | 'completed' | 'cancelled' | string; 
}

export interface PurchaseOrder {
  order_id: number;
  order_date: string;
  supplier_id: number;
  product_id: number;
  quantity: number;
  unit_price: number;
  total_amount: number;
  notes: string;
  created_at: string;
}

export interface StockIn {
  stock_in_id: number;
  purchase_order_id: number;
  stock_in_date: string;
  product_id: number;
  shanghai_qty: number;
  tianjin_qty: number;
  shenzhen_qty: number;
  total_qty: number;
  remaining_qty: number;
  serial_numbers: string;
  notes: string;
  created_at: string;
}

export interface StockOut {
  stock_out_id: number;
  sales_order_id: number;
  stock_out_date: string;
  product_id: number;
  shanghai_qty: number;
  tianjin_qty: number;
  shenzhen_qty: number;
  total_qty: number;
  remaining_qty: number;
  serial_numbers: string;
  notes: string;
  created_at: string;
}
export interface Receipt {
  receipt_id: number;
  sales_order_id: number;
  amount: number;
  receipt_date: string;
  payment_method: 'cash' | 'bank_transfer' | 'check' | 'credit_card';
  status: 'pending' | 'completed' | 'cancelled';
  notes: string;
  created_at: string;
}

export interface PurchaseInvoice {
  invoice_id: number;
  purchase_order_id: number;
  invoice_number: string;
  invoice_date: string;
  company_name: string;
  goods_or_services: string;
  specification_model: string;
  unit: string;
  quantity: number;
  unit_price_tax_included: number;
  total_amount_untax_included: number;
  total_amount_tax_included: number;
  tax_amount: number;
  tax_rate: number;
  status: 'pending' | 'received' | 'cancelled';
  notes: string;
  created_at: string;
}


export interface SalesInvoice {
  invoice_id: number;
  sales_order_id: number;
  invoice_number: string;
  invoice_date: string;
  company_name: string;
  goods_or_services: string;
  specification_model: string;
  unit: string;
  quantity: number;
  unit_price_tax_included: number;
  total_amount_tax_included: number;
  untaxed_sales_amount: number;
  tax_amount: number;
  tax_rate: number;
  status: 'pending' | 'issued' | 'cancelled';
  notes: string;
  created_at: string;
}
export interface Payment {
  payment_id: number;
  purchase_order_id: number;
  amount: number;
  payment_date: string;
  payment_method: 'cash' | 'bank_transfer' | 'check' | 'credit_card';
  status: 'pending' | 'completed' | 'cancelled';
  notes: string;
  created_at: string;
}
export interface User {
  id: number;
  username: string;
  role: 'admin' | 'sales' | 'warehouse' | 'finance';
  permissions: string[];
}