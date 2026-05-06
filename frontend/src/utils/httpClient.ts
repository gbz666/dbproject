import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import type { ApiResult } from "@/types/api";
import { getCookie, removeCookie } from "@/utils/cookie";

const TOKEN_KEY = "access_token";
const USER_KEY = "user_info"; 

// --- 错误类型定义 ---
export interface ApiError {
  message: string;
  status: number;
  data?: any;
}

// --- Axios 实例 ---
export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 95000,
});

// --- 请求拦截器 ---
axiosInstance.interceptors.request.use(
  (config) => {
    const token = getCookie(TOKEN_KEY) ?? localStorage.getItem(TOKEN_KEY);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// --- 响应拦截器 ---
function isLoginRequest(config: AxiosRequestConfig | undefined): boolean {
  if (!config?.url) return false;
  const url = typeof config.url === "string" ? config.url : config.url.toString();
  return url.includes("/auth/login");
}

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401 && !isLoginRequest(error.config)) {
        removeCookie(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        window.location.href = "/auth/login";
      }
      return Promise.reject<ApiError>({
        message: data?.message || `请求失败 (HTTP ${status})`,
        status,
        data,
      });
    }
    return Promise.reject<ApiError>({
      message: error.message || "网络错误",
      status: 0,
    });
  }
);

// --- httpClient 函数定义 ---
export interface HttpRequestOptions<TBody = unknown> {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: TBody;
  params?: Record<string, any>; // 查询参数
  headers?: Record<string, string>;
  auth?: boolean;
}

export const httpClient = async <TData>(
  endpoint: string,
  {
    method = "GET",
    body,
    params,
    headers,
    auth = true,
  }: HttpRequestOptions = {}
): Promise<TData> => {
  
  // 【关键修正】：移除 method === "GET" 的限制
  // 这样 PUT/DELETE/POST 也可以通过 URL 传递参数（如 currentStaffId）
  let url = endpoint;
  if (params && Object.keys(params).length > 0) {
    const query = new URLSearchParams(params).toString();
    url = `${endpoint}${endpoint.includes("?") ? "&" : "?"}${query}`;
  }

  const config: AxiosRequestConfig = {
    method,
    url: url,
    data: body,
    headers: {
      ...headers,
      ...(method !== "GET" && !(body instanceof FormData) && { "Content-Type": "application/json" }),
    },
  };

  if (!auth && config.headers) {
    delete config.headers.Authorization;
  }

  const response: AxiosResponse<ApiResult<TData>> = await axiosInstance.request(config);
  const apiResult = response.data;

  // 后端 Result 类用 code 字段标识状态（200=成功），而非 success 布尔值
  if (apiResult.code && apiResult.code !== 200) {
    throw {
      message: apiResult.message || "业务逻辑错误",
      status: response.status,
      data: apiResult,
    } as ApiError;
  }

  return apiResult.data as TData;
};