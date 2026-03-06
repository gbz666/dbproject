import { httpClient } from "@/utils/httpClient";
import type { LoginResponse } from "@/types/api";

const BASE_PATH = "/api/auth";

export const authApi = {
  /** 登录（员工姓名 + 密码），不携带 token。后端接收字段：staffName, password */
  login(staffName: string, password: string) {
    return httpClient<LoginResponse>(`${BASE_PATH}/login`, {
      method: "POST",
      body: { staffName, password },
      auth: false,
    });
  },

  /** 登出，需携带当前 token */
  logout() {
    return httpClient<void>(`${BASE_PATH}/logout`, {
      method: "POST",
      auth: true,
    });
  },

  /** 修改密码（当前密码 + 新密码），需登录 */
  changePassword(currentPassword: string, newPassword: string) {
    return httpClient<void>(`${BASE_PATH}/change-password`, {
      method: "POST",
      body: { currentPassword, newPassword },
      auth: true,
    });
  },
};
