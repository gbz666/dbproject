import { httpClient } from "@/utils/httpClient";

const BASE_PATH = "/api/staff";

export interface StaffListItem {
  id: number;
  staffCode: string | null;
  staffName: string;
  email: string | null;
  phone: string | null;
  title: string | null;
  status: number | null;
  lastLoginAt: string | null;
  roleNames: string[];
  roleIds: number[];
}

export interface StaffPageResult {
  list: StaffListItem[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface RoleItem {
  id: number;
  roleName: string;
  description: string | null;
}

export interface StaffCreatePayload {
  staffName: string;
  staffCode?: string;
  email?: string;
  phone?: string;
  title?: string;
  password: string;
  roleIds?: number[];
}

export interface StaffUpdatePayload {
  staffName?: string;
  staffCode?: string;
  email?: string;
  phone?: string;
  title?: string;
  roleIds?: number[];
}

export const staffManageApi = {
  /** 分页查询员工列表 */
  list(pageNum = 1, pageSize = 10, keyword?: string) {
    const params: Record<string, any> = { pageNum, pageSize };
    if (keyword) params.keyword = keyword;
    return httpClient<StaffPageResult>(`${BASE_PATH}/list`, {
      method: "GET",
      params,
      auth: true,
    });
  },

  /** 获取所有可用角色 */
  getRoles() {
    return httpClient<RoleItem[]>(`${BASE_PATH}/roles`, {
      method: "GET",
      auth: true,
    });
  },

  /** 新建员工 */
  create(payload: StaffCreatePayload) {
    return httpClient<void>(`${BASE_PATH}/create`, {
      method: "POST",
      body: payload,
      auth: true,
    });
  },

  /** 编辑员工 */
  update(id: number, payload: StaffUpdatePayload) {
    return httpClient<void>(`${BASE_PATH}/${id}`, {
      method: "PUT",
      body: payload,
      auth: true,
    });
  },

  /** 重置密码 */
  resetPassword(id: number, newPassword: string) {
    return httpClient<void>(`${BASE_PATH}/${id}/reset-password`, {
      method: "POST",
      body: { newPassword },
      auth: true,
    });
  },

  /** 切换账户状态（启用/禁用） */
  toggleStatus(id: number) {
    return httpClient<void>(`${BASE_PATH}/${id}/status`, {
      method: "PUT",
      auth: true,
    });
  },
};
