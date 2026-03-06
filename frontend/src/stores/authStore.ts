import { defineStore } from "pinia";
import { authService } from "@/services/authService";
import type { LoginResponse } from "@/types/api";
import { getCookie, setCookie, removeCookie } from "@/utils/cookie";

const TOKEN_KEY = "access_token";
const USER_KEY = "user_info";
const DEFAULT_TOKEN_MAX_AGE = 7200; // 秒，与后端默认一致

/** 前端存储的用户信息（与 LoginResponse 对应，仅 staffName 用于展示） */
export interface AuthUser {
  userId: number | null;
  staffName: string;
  role?: string;
}

function getStoredToken(): string | null {
  return getCookie(TOKEN_KEY);
}

function getStoredUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthUser) : null;
  } catch {
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: getStoredToken() as string | null,
    user: getStoredUser() as AuthUser | null,
    changePasswordLoading: false,
  }),

  getters: {
    isLoggedIn(): boolean {
      return !!this.token;
    },
  },

  actions: {
    /** 登录：调用接口并保存 token、用户信息（仅 staffName） */
    async login(staffName: string, password: string): Promise<LoginResponse> {
      const data = await authService.login(staffName, password);
      this.token = data.token;
      this.user = {
        userId: data.userId ?? null,
        staffName: data.staffName ?? "",
      };
      const expireSeconds = data.expiration ?? DEFAULT_TOKEN_MAX_AGE;
      setCookie(TOKEN_KEY, data.token, expireSeconds / 86400);
      localStorage.setItem(USER_KEY, JSON.stringify(this.user));
      return data;
    },

    /** 登出：先调后端（带 token），再清除本地状态 */
    async logout(): Promise<void> {
      if (this.token) {
        try {
          await authService.logout();
        } catch {
          // 忽略网络错误，仍会清除本地
        }
      }
      this.token = null;
      this.user = null;
      removeCookie(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },

    /** 修改密码（当前密码 + 新密码），成功后保持登录态，不登出；loading 由 store 管理 */
    async changePassword(currentPassword: string, newPassword: string): Promise<void> {
      this.changePasswordLoading = true;
      try {
        await authService.changePassword(currentPassword, newPassword);
      } finally {
        this.changePasswordLoading = false;
      }
    },

    /** 从 localStorage 恢复状态（用于刷新页面后保持登录态） */
    initFromStorage(): void {
      this.token = getStoredToken();
      this.user = getStoredUser();
    },
  },
});
