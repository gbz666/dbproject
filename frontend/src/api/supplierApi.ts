// src/api/supplierApi.ts (修改后)
import { httpClient } from '@/utils/httpClient';
import type { Supplier } from '@/types/pojo';
import type { PageInfo } from '@/types/api';

/**
 * 分页查询供应商列表
 * @param pageNum 当前页码
 * @param pageSize 每页大小
 * @returns 供应商列表的分页信息
 */
export const getSuppliersByPage = (pageNum: number, pageSize: number) => {
    return httpClient<PageInfo<Supplier>>('/api/suppliers', {
        method: 'GET',
        params: {
            pageNum,
            pageSize
        }
    });
};