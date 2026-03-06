import { authApi } from "@/api/authApi";

/** 认证相关业务：封装 authApi，供 store 或其它层调用，Vue 层不直接调 API */
export const authService = {
  login(staffName: string, password: string) {
    return authApi.login(staffName, password);
  },

  logout() {
    return authApi.logout();
  },

  /** 修改密码（当前密码 + 新密码），成功后仍保持登录态 */
  changePassword(currentPassword: string, newPassword: string) {
    return authApi.changePassword(currentPassword, newPassword);
  },
};
