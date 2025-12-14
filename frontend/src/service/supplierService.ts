// src/service/supplierService.ts
import * as supplierApi from '@/api/supplierApi';
import type { PageInfo } from '@/types/api';
import type { Supplier } from '@/types/pojo';
import { ElMessage } from 'element-plus';

/**
 * 供应商服务：封装 API 调用并处理通用异常
 */
export const supplierService = {
    async fetchSuppliers(pageNum: number, pageSize: number): Promise<PageInfo<Supplier> | null> {
        try {
            const pageInfo = await supplierApi.getSuppliersByPage(pageNum, pageSize);
            return pageInfo;
        } catch (error) {
            console.error('获取供应商列表失败:', error);
            ElMessage.error((error as any).message || '网络或业务错误，请重试');
            return null;
        }
    }
};