// src/api/customerApi.ts (修改后)
import { httpClient } from '@/utils/httpClient';
import type { Customer, CustomerDetailDTO } from '@/types/pojo';
import type { PageInfo } from '@/types/api';

/**
 * 分页查询客户列表
 * @param pageNum 当前页码
 * @param pageSize 每页大小
 * @returns 客户列表的分页信息
 */
export const getCustomersByPage = (pageNum: number, pageSize: number) => {
    return httpClient<PageInfo<CustomerDetailDTO>>('/api/customers', {
        method: 'GET',
        params: {
            pageNum,
            pageSize
        }
    });
};