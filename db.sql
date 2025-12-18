-- 若要多次运行，先按依赖顺序删除表(从子表到父表)
DROP TABLE IF EXISTS payment_expenses;
DROP TABLE IF EXISTS payment_receipts;
DROP TABLE IF EXISTS purchase_invoice_details;
DROP TABLE IF EXISTS purchase_invoices;
DROP TABLE IF EXISTS sales_invoice_details;
DROP TABLE IF EXISTS sales_invoices;
DROP TABLE IF EXISTS outbound_order_items;
DROP TABLE IF EXISTS outbound_orders;
DROP TABLE IF EXISTS stock_in_items;
DROP TABLE IF EXISTS stock_ins;
DROP TABLE IF EXISTS purchase_order_items;
DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS sales_order_items;
DROP TABLE IF EXISTS sales_orders;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS warehouses;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS product_categories;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS staff_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS staffs;

-- =================================================
-- 基础:员工表 staffs、角色 roles、员工角色关联 staff_roles
-- =================================================

-- =================================================
-- 员工、角色、权限
-- =================================================

CREATE TABLE staffs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工主键ID',
    staff_code VARCHAR(50) UNIQUE COMMENT '员工业务编号(对外或展示用，不用于逻辑关联)',
    staff_name VARCHAR(100) NOT NULL COMMENT '员工姓名',
    email VARCHAR(150) COMMENT '电子邮箱',
    phone VARCHAR(30) COMMENT '联系电话',
    title VARCHAR(100) COMMENT '职位/职称',
    -- 新增审计字段 ( BIGINT 且允许 NULL )
    created_by_id BIGINT COMMENT '创建人ID，指向 staffs.id',
    updated_by_id BIGINT COMMENT '最后修改人ID，指向 staffs.id'
) ENGINE=InnoDB COMMENT='员工表:存放平台员工信息';

CREATE TABLE roles (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色主键ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称(如 sales, admin 等)',
    description VARCHAR(255) COMMENT '角色说明',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB  COMMENT='系统角色表';

CREATE TABLE staff_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '员工角色关联主键',
    staff_id BIGINT NOT NULL COMMENT '员工ID，外键指向 staffs.id',
    role_id INT NOT NULL COMMENT '角色ID，外键指向 roles.id',
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_staff_roles (staff_id, role_id),

    CONSTRAINT fk_staff_roles_staff FOREIGN KEY (staff_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_staff_roles_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB  COMMENT='员工-角色多对多关联表';

-- 审计字段外键(延后添加约束，因 staffs 表已存在)
ALTER TABLE staffs
    ADD CONSTRAINT fk_staffs_created_by FOREIGN KEY (created_by_id) REFERENCES staffs(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    ADD CONSTRAINT fk_staffs_updated_by FOREIGN KEY (updated_by_id) REFERENCES staffs(id)
        ON UPDATE CASCADE ON DELETE SET NULL;

-- =================================================
-- 客户与供应商(customers, suppliers)及联系人 contacts
-- =================================================

CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户主键ID',
    customer_code VARCHAR(50) UNIQUE NOT NULL COMMENT '客户业务编号(对外展示，不作为外键)',
    customer_name VARCHAR(200) NOT NULL COMMENT '客户名称',
    address VARCHAR(255) COMMENT '注册地址/地址',
    phone VARCHAR(50) COMMENT '客户电话/手机号',
    email VARCHAR(150) COMMENT '客户邮箱',

    -- 关联的员工(使用员工主键 id)
    sales_person_id BIGINT NULL COMMENT '负责销售的员工ID(staffs.id)',
    follow_up_person_id BIGINT NULL COMMENT '跟进人员工ID(staffs.id)',
    owner_id BIGINT NULL COMMENT '客户归属人(staffs.id)',

    payment_terms_days INT DEFAULT 0 COMMENT '付款天数(账期)',
    payment_terms_notes TEXT COMMENT '付款条款/备注',

    -- 审计 & 软删除
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除:0=正常,1=已删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_customers_sales_person FOREIGN KEY (sales_person_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_customers_follow_up FOREIGN KEY (follow_up_person_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_customers_owner FOREIGN KEY (owner_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_customers_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_customers_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='客户基础信息表';

CREATE TABLE suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '供应商主键ID',
    supplier_code VARCHAR(50) UNIQUE NOT NULL COMMENT '供应商业务编号(对外展示，不作为外键)',
    supplier_name VARCHAR(200) NOT NULL COMMENT '供应商名称',
    short_name VARCHAR(100) COMMENT '供应商简称',
    main_business TEXT COMMENT '主营业务描述',
    tax_no VARCHAR(100) COMMENT '税号/纳税识别号',
    address VARCHAR(255) COMMENT '注册地址/地址',
    phone VARCHAR(50) COMMENT '供应商电话',
    email VARCHAR(150) COMMENT '供应商邮箱',

    -- 关联人员
    sales_person_id BIGINT NULL COMMENT '负责销售/关系维护的员工ID(staffs.id)',
    follow_up_person_id BIGINT NULL COMMENT '跟进人员工ID(staffs.id)',
    owner_id BIGINT NULL COMMENT '供应商归属人(staffs.id)',

    -- 审计 & 软删除
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除:0=正常,1=已删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_suppliers_sales_person FOREIGN KEY (sales_person_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_suppliers_follow_up FOREIGN KEY (follow_up_person_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_suppliers_owner FOREIGN KEY (owner_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_suppliers_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_suppliers_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='供应商基础信息表';

CREATE TABLE contacts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '联系人主键ID',
    contact_name VARCHAR(200) NOT NULL COMMENT '联系人姓名',
    title VARCHAR(100) COMMENT '职务/头衔',
    phone VARCHAR(50) COMMENT '联系电话',
    mobile VARCHAR(50) COMMENT '手机',
    email VARCHAR(150) COMMENT '邮箱',
    note TEXT COMMENT '备注/说明',

    -- 联系人所属(只能属于客户或供应商之一)
    customer_id BIGINT NULL COMMENT '关联客户ID(customers.id)',
    supplier_id BIGINT NULL COMMENT '关联供应商ID(suppliers.id)',

    -- 审计 & 软删除
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除:0=正常,1=已删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_contacts_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_contacts_supplier FOREIGN KEY (supplier_id)
        REFERENCES suppliers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_contacts_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_contacts_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='联系人表:可以属于客户或供应商';

-- =================================================
-- 商品分类与商品(product_categories, products)
-- =================================================

CREATE TABLE product_categories (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品分类主键ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除:0=正常,1=已删除'
) ENGINE=InnoDB  COMMENT='商品分类表';

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品主键ID',
    product_code VARCHAR(50) UNIQUE NOT NULL COMMENT '产品业务编号(对外展示，不参与FK)',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    category_id INT NULL COMMENT '所属分类ID(product_categories.id)',
    sku VARCHAR(100) COMMENT '库存单位/SKU',
    unit VARCHAR(50) COMMENT '计量单位(如 件、kg)',
    specification VARCHAR(255) COMMENT '规格型号',
    description TEXT COMMENT '产品描述/备注',
    
    -- 建议维护默认成本与参考售价
    cost_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '默认成本价(金额，单位元)',
    list_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '参考售价(金额，单位元)',

    -- 审计 & 软删除
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除:0=正常,1=已删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_products_category FOREIGN KEY (category_id)
        REFERENCES product_categories(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_products_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_products_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='产品表';

-- =================================================
-- 仓库与库存(warehouses, inventory)
-- inventory 使用复合主键 (product_id, warehouse_id)
-- =================================================

CREATE TABLE warehouses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仓库主键ID',
    warehouse_code VARCHAR(50) UNIQUE COMMENT '仓库编码(展示用)',
    warehouse_name VARCHAR(200) NOT NULL COMMENT '仓库名称',
    location VARCHAR(255) COMMENT '仓库地址/位置描述',
    contact_phone VARCHAR(50) COMMENT '仓库联系人电话',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除:0=正常,1=已删除'
) ENGINE=InnoDB  COMMENT='仓库表';

CREATE TABLE inventory (
    product_id BIGINT NOT NULL COMMENT '产品ID(products.id)，复合主键一部分',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID(warehouses.id)，复合主键一部分',
    quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '当前库存数量(可带小数)',
    reserved_quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '被占用/预留库存数量',
    last_stock_in_at DATETIME NULL COMMENT '最近一次入库时间',
    last_stock_out_at DATETIME NULL COMMENT '最近一次出库时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '库存最后更新时间',

    -- 审计 & 软删除(库存记录一般不软删，但为统一加入)
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    PRIMARY KEY (product_id, warehouse_id),

    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_inventory_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='库存表(复合主键 product_id+warehouse_id)';

-- =================================================
-- 销售模块:sales_orders, sales_order_items, outbound_orders, outbound_order_items
-- =================================================

CREATE TABLE sales_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '销售订单主键ID',
    order_code VARCHAR(80) UNIQUE NOT NULL COMMENT '销售订单业务编号(例如 xsYYMMNNN，仅用于展示)',
    order_date DATE NOT NULL COMMENT '订单日期',
    customer_id BIGINT NOT NULL COMMENT '客户ID(customers.id)',
    order_status VARCHAR(50) DEFAULT 'draft' COMMENT '订单状态(建议使用字典表)',
    total_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '订单总金额(含税)',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',

    sales_person_id BIGINT NULL COMMENT '销售员(staffs.id)',
    follow_up_person_id BIGINT NULL COMMENT '跟进人(staffs.id)',
    owner_id BIGINT NULL COMMENT '订单归属人(staffs.id)',

    note TEXT COMMENT '订单备注/说明',

    -- 审计 & 软删除
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除:0=正常,1=已删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_sales_orders_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_orders_sales_person FOREIGN KEY (sales_person_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_orders_follow_up FOREIGN KEY (follow_up_person_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_orders_owner FOREIGN KEY (owner_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_orders_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_sales_orders_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='销售订单表';

CREATE TABLE sales_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '销售订单行项目主键ID',
    sales_order_id BIGINT NOT NULL COMMENT '销售订单ID(sales_orders.id)',
    product_id BIGINT NOT NULL COMMENT '产品ID(products.id)',
    quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '数量(含小数)',
    unit_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '销售单价(含税/或按业务定义)',
    discount DECIMAL(18,2) DEFAULT 0.00 COMMENT '行折扣金额',
    tax_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '税率(如 0.13 表示 13%)',
    line_total DECIMAL(18,2) GENERATED ALWAYS AS (quantity * unit_price - discount) VIRTUAL COMMENT '行合计(生成列，含税)',
    remark VARCHAR(255) COMMENT '行备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',

    CONSTRAINT fk_sales_order_items_order FOREIGN KEY (sales_order_id)
        REFERENCES sales_orders(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_order_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_order_items_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='销售订单行项目表';

CREATE TABLE outbound_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库单主键ID',
    outbound_code VARCHAR(80) UNIQUE COMMENT '出库单编码(用于展示)',
    sales_order_id BIGINT NULL COMMENT '关联销售订单ID(sales_orders.id)',
    outbound_date DATE COMMENT '出库日期',
    status VARCHAR(50) DEFAULT 'draft' COMMENT '出库单状态',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_outbound_orders_sales_order FOREIGN KEY (sales_order_id)
        REFERENCES sales_orders(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_outbound_orders_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_outbound_orders_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='出库单表';

CREATE TABLE outbound_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '出库单行项目主键ID',
    outbound_order_id BIGINT NOT NULL COMMENT '出库单ID(outbound_orders.id)',
    product_id BIGINT NOT NULL COMMENT '产品ID(products.id)',
    warehouse_id BIGINT NOT NULL COMMENT '出库仓库ID(warehouses.id)',
    quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '出库数量',
    serial_numbers JSON NULL COMMENT '序列号列表(JSON数组或NULL)',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',

    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_outbound_items_outbound FOREIGN KEY (outbound_order_id)
        REFERENCES outbound_orders(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_outbound_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_outbound_items_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_outbound_items_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_outbound_items_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='出库单行项目表';

-- =================================================
-- 采购模块:purchase_orders, purchase_order_items, stock_ins, stock_in_items
-- =================================================

CREATE TABLE purchase_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '采购订单主键ID',
    purchase_code VARCHAR(80) UNIQUE COMMENT '采购订单编码(展示用)',
    order_date DATE COMMENT '采购订单日期',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID(suppliers.id)',
    order_status VARCHAR(50) DEFAULT 'draft' COMMENT '采购订单状态',
    total_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '采购订单总金额(含税)',
    note TEXT COMMENT '采购备注',

    follow_up_person_id BIGINT NULL COMMENT '跟进人(staffs.id)',
    owner_id BIGINT NULL COMMENT '采购负责人(staffs.id)',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_purchase_orders_supplier FOREIGN KEY (supplier_id)
        REFERENCES suppliers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_orders_follow_up FOREIGN KEY (follow_up_person_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_orders_owner FOREIGN KEY (owner_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_orders_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_purchase_orders_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='采购订单表';

CREATE TABLE purchase_order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '采购订单行项目主键ID',
    purchase_order_id BIGINT NOT NULL COMMENT '采购订单ID(purchase_orders.id)',
    product_id BIGINT NOT NULL COMMENT '产品ID(products.id)',
    -- 修正:移除 warehouse_id，仓库信息由入库单决定
    quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '采购数量',
    unit_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '采购单价',
    line_total DECIMAL(18,2) GENERATED ALWAYS AS (quantity * unit_price) VIRTUAL COMMENT '行合计(生成列)',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    
    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_purchase_items_order FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_items_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_purchase_items_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='采购订单行项目表';

CREATE TABLE stock_ins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库记录主键ID',
    stock_in_code VARCHAR(80) UNIQUE COMMENT '入库单编码(展示用)',
    purchase_order_id BIGINT NULL COMMENT '来源采购订单ID(purchase_orders.id)',
    stock_in_date DATE COMMENT '入库日期',
    note TEXT COMMENT '入库备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '操作人(staffs.id)',

    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_stock_ins_purchase_order FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_stock_ins_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_stock_ins_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='入库单/入库记录表';

CREATE TABLE stock_in_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '入库行项目主键ID',
    stock_in_id BIGINT NOT NULL COMMENT '入库单ID(stock_ins.id)',
    product_id BIGINT NOT NULL COMMENT '产品ID(products.id)',

    warehouse_id BIGINT NOT NULL COMMENT '入库仓库ID(warehouses.id)',
    quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '入库数量',
    serial_numbers JSON NULL COMMENT '序列号数组(JSON)',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '操作人(staffs.id)',

    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_stock_in_items_stock_in FOREIGN KEY (stock_in_id)
        REFERENCES stock_ins(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_stock_in_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_stock_in_items_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_stock_in_items_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_stock_in_items_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='入库行项目表';

-- =================================================
-- 销售与采购发票、发票明细(sales_invoices, sales_invoice_details, purchase_invoices, purchase_invoice_details)
-- =================================================

CREATE TABLE sales_invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '销项发票主键ID',
    invoice_no VARCHAR(80) UNIQUE NOT NULL COMMENT '发票号/发票编码(唯一)',
    customer_id BIGINT NOT NULL COMMENT '开票客户ID(customers.id)',
    related_outbound_id BIGINT NULL COMMENT '关联出库单ID(outbound_orders.id)',
    amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '发票金额(含税)',
    invoice_date DATE COMMENT '开票日期',
    status VARCHAR(50) DEFAULT 'unsettled' COMMENT '发票状态(unsettled/settled)',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '开票人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_sales_invoices_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_invoices_outbound FOREIGN KEY (related_outbound_id)
        REFERENCES outbound_orders(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_invoices_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_sales_invoices_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='销项发票表';

CREATE TABLE sales_invoice_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '销项发票明细主键ID',
    invoice_id BIGINT NOT NULL COMMENT '发票ID(sales_invoices.id)',
    item_name VARCHAR(255) COMMENT '项货物或者应税劳务，服务名称',
    specification VARCHAR(255) COMMENT '规格型号',
    unit VARCHAR(50) DEFAULT '个' COMMENT '计量单位',
    quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '数量',
    unit_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '单价',
    amount_exclusive_tax DECIMAL(18,2) DEFAULT 0.00 COMMENT '不含税金额',
    tax_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '税率',
    tax_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '税额',
    amount_inclusive_tax DECIMAL(18,2) DEFAULT 0.00 COMMENT '含税金额',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_sales_invoice_details_invoice FOREIGN KEY (invoice_id)
        REFERENCES sales_invoices(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_sales_invoice_details_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_sales_invoice_details_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='销项发票明细';

CREATE TABLE purchase_invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进项发票主键ID',
    invoice_no VARCHAR(80) UNIQUE NOT NULL COMMENT '发票号/发票编码(唯一)',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID(suppliers.id)',
    related_stock_in_id BIGINT NULL COMMENT '关联入库单ID(stock_ins.id)',
    amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '发票金额(含税)',
    invoice_date DATE COMMENT '开票日期',
    status VARCHAR(50) DEFAULT 'unsettled' COMMENT '发票状态(unsettled/settled)',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '登记人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否软删除',
    deleted_at DATETIME NULL COMMENT '删除时间(软删除)',

    CONSTRAINT fk_purchase_invoices_supplier FOREIGN KEY (supplier_id)
        REFERENCES suppliers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_invoices_stockin FOREIGN KEY (related_stock_in_id)
        REFERENCES stock_ins(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_invoices_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_purchase_invoices_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='进项发票表';

CREATE TABLE purchase_invoice_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进项发票明细主键ID',
    invoice_id BIGINT NOT NULL COMMENT '发票ID(purchase_invoices.id)',
    item_name VARCHAR(255) COMMENT '项目名称',
    specification VARCHAR(255) COMMENT '规格',
    unit VARCHAR(50) COMMENT '计量单位',
    quantity DECIMAL(18,4) DEFAULT 0.0000 COMMENT '数量',
    unit_price DECIMAL(18,2) DEFAULT 0.00 COMMENT '单价',
    amount_exclusive_tax DECIMAL(18,2) DEFAULT 0.00 COMMENT '不含税金额',
    tax_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '税率',
    tax_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '税额',
    amount_inclusive_tax DECIMAL(18,2) DEFAULT 0.00 COMMENT '含税金额',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    created_by_id BIGINT NULL COMMENT '创建人(staffs.id)',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_purchase_invoice_details_invoice FOREIGN KEY (invoice_id)
        REFERENCES purchase_invoices(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_invoice_details_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_purchase_invoice_details_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='进项发票明细';

-- =================================================
-- 财务收付模块:payment_receipts, payment_expenses
-- =================================================

CREATE TABLE payment_receipts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收款记录主键ID',
    receipt_no VARCHAR(80) UNIQUE COMMENT '收款单编号(展示用)',
    customer_id BIGINT NULL COMMENT '收款客户ID(customers.id)',
    sales_invoice_id BIGINT NULL COMMENT '关联销项发票ID(sales_invoices.id)',
    amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '收款金额',
    receipt_date DATE COMMENT '收款日期',
    method VARCHAR(50) COMMENT '收款方式(如 cash/bank/transfer)',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '登记人(staffs.id)',
    
    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_payment_receipts_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_payment_receipts_invoice FOREIGN KEY (sales_invoice_id)
        REFERENCES sales_invoices(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_payment_receipts_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_payment_receipts_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='客户收款记录表';

CREATE TABLE payment_expenses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '付款记录主键ID',
    payment_no VARCHAR(80) UNIQUE COMMENT '付款单编号(展示用)',
    supplier_id BIGINT NULL COMMENT '付款供应商ID(suppliers.id)',
    purchase_invoice_id BIGINT NULL COMMENT '关联进项发票ID(purchase_invoices.id)',
    amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '付款金额',
    payment_date DATE COMMENT '付款日期',
    method VARCHAR(50) COMMENT '付款方式(如 cash/bank/transfer)',
    remark VARCHAR(255) COMMENT '备注',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by_id BIGINT NULL COMMENT '操作人(staffs.id)',
    
    -- 修正:添加 updated_at/updated_by_id 审计字段
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    updated_by_id BIGINT NULL COMMENT '最后修改人(staffs.id)',

    CONSTRAINT fk_payment_expenses_supplier FOREIGN KEY (supplier_id)
        REFERENCES suppliers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_payment_expenses_invoice FOREIGN KEY (purchase_invoice_id)
        REFERENCES purchase_invoices(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_payment_expenses_created_by FOREIGN KEY (created_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_payment_expenses_updated_by FOREIGN KEY (updated_by_id)
        REFERENCES staffs(id) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB  COMMENT='付款记录表';


-- =================================================
-- 索引
-- =================================================

CREATE INDEX idx_products_code_name ON products(product_code, product_name);
CREATE INDEX idx_customers_code_name ON customers(customer_code, customer_name);
CREATE INDEX idx_suppliers_code_name ON suppliers(supplier_code, supplier_name);
CREATE INDEX idx_sales_orders_code_date ON sales_orders(order_code, order_date);
CREATE INDEX idx_purchase_orders_code_date ON purchase_orders(purchase_code, order_date);
CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory(warehouse_id);


-- =================================================
-- 说明
-- =================================================
/*
  1) 关于 biz_id 与 pk 的处理:
     - 所有业务编号统一命名为 *_code(例如 customer_code, product_code)
     - 这些 code 字段仅用于展示或对外查找，不建议在业务关联时以 code 建 FK 或 JOIN。
     - 所有表的逻辑关联均使用主键 id(BIGINT / INT)。

  2) 关于软删除:
     - 大多数业务表包含 is_deleted 与 deleted_at 字段。
     - 软删除后，需在查询时加上 WHERE is_deleted = 0。

  3) 关于审计字段:
     - 每张表都包含 created_at / updated_at，并在关键表包含 created_by_id / updated_by_id 指向 staffs.id。
     - created_by_id 与 updated_by_id 外键使用 ON DELETE SET NULL，避免删除员工时级联删除业务数据。

  4) 关于金额/精度:
     - 金额统一 DECIMAL(18,2)，数量 DECIMAL(18,4)，税率 DECIMAL(5,4)。
     - 如需不同精度，可按表或字段具体业务要求调整。

  5) 关于外键策略:
     - 大部分业务关联使用 ON DELETE RESTRICT 防止误删。
     - 如果某些场景确实需要级联删除(例如某些临时表)，可再单独调整。

  6) 关于 inventory:
     - inventory 使用复合主键 (product_id, warehouse_id)，保证同一商品在同一仓库仅有一条库存记录。

  7) contacts 表:
     - 使用 customer_id / supplier_id 两列建立真实外键，并加 CHECK 确保两者不能同时为空或同时非空(只能属于其一)。

  8) 下一步建议(可选):
     - 提供字典表(如 order_status, invoice_status, payment_method)替换硬编码字符串(更易扩展)。
     - 为关键变更操作(入库/出库/调整)添加库存流水表 (inventory_transactions)，记录 before/after/operator/serials，便于审计与回滚。
     - 考虑为敏感表建立触发器或使用事务封装复杂业务，保证一致性(例如下发出库同时扣减库存、生成出库明细与日志)。
*/

