// src/service/customerService.ts
import * as customerApi from '@/api/customerApi';
import type { PageInfo } from '@/types/api';
import type { Customer, CustomerDetailDTO } from '@/types/pojo';
import { ElMessage } from 'element-plus';

/**
 * 客户服务：封装 API 调用并处理通用异常
 */
export const customerService = {
    async fetchCustomers(pageNum: number, pageSize: number): Promise<PageInfo<CustomerDetailDTO> | null> {
        try {
            const pageInfo = await customerApi.getCustomersByPage(pageNum, pageSize);
            // 可以在这里对数据进行预处理或格式化
            return pageInfo;
        } catch (error) {
            console.error('获取客户列表失败:', error);
            // 弹出错误提示
            ElMessage.error((error as any).message || '网络或业务错误，请重试');
            return null;
        }
    }
};