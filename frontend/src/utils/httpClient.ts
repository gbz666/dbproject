import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import type { ApiResult } from "@/types/api"; 

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
    const token = localStorage.getItem("access_token");
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// --- 响应拦截器 ---
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        localStorage.removeItem("access_token");
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
      ...(method !== "GET" && { "Content-Type": "application/json" }),
    },
  };

  if (!auth && config.headers) {
    delete config.headers.Authorization;
  }

  const response: AxiosResponse<ApiResult<TData>> = await axiosInstance.request(config);
  const apiResult = response.data;

  if (apiResult.success === false) {
    throw {
      message: apiResult.message || "业务逻辑错误",
      status: response.status,
      data: apiResult,
    } as ApiError;
  }

  return apiResult.data as TData;
};