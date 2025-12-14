// 🛠️ 文件名: /src/utils/httpClient.ts (改进版)

import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
// import { getCookie, removeCookie } from "./cookie"; // 假设这部分代码存在
import type { ApiResult } from "@/types/api"; // 确保 ApiResult 路径正确

// --- 错误类型定义 ---
export interface ApiError {
  message: string; // 后端返回的或网络错误信息
  status: number; // HTTP 状态码 (0 表示网络错误)
  data?: any; // 后端返回的原始错误数据体
}

// --- Axios 实例创建 (保持不变) ---
export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 95000,
});

// --- 请求/响应拦截器 (保持不变) ---
// ... (您的现有拦截器代码) ...
axiosInstance.interceptors.request.use(
  (config) => {
    let token = localStorage.getItem("access_token");
    // if (!token) {
    //   token = getCookie("access_token"); // 假设 getCookie 存在
    // }
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        localStorage.removeItem("access_token");
        if (window.location.pathname !== "/auth/login") {
          window.location.href = "/auth/login";
        }
      }
      return Promise.reject<ApiError>({
        message: data?.message || `请求失败 (HTTP ${status})`,
        status,
        data,
      });
    } else if (error.request) {
      return Promise.reject<ApiError>({
        message: "网络错误，请检查您的网络连接",
        status: 0,
      });
    } else {
      return Promise.reject<ApiError>({
        message: error.message || "未知错误",
        status: 0,
      });
    }
  }
);

// --- httpClient 函数定义 ---
export interface HttpRequestOptions<TBody = unknown> {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: TBody;
  /** 新增: 查询参数对象，用于 GET/DELETE 请求 */
  params?: Record<string, any>;
  headers?: Record<string, string>;
  auth?: boolean;
}

/**
 * 封装的 HTTP 客户端：负责调用、数据剥离和业务成功检查
 * @param endpoint 请求路径
 * @param options 请求配置
 * @returns Promise<TData> 业务数据
 */
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
  let url = endpoint;
  // 1. 处理 GET 请求的查询参数 (params)
  if (params && Object.keys(params).length > 0 && method === "GET") {
    const query = new URLSearchParams(params).toString();
    url = `${endpoint}${endpoint.includes("?") ? "&" : "?"}${query}`;
  }

  const config: AxiosRequestConfig = {
    method,
    url: url, // 使用处理后的 url
    data: body,
    headers: {
      ...headers,
      // 默认设置 content-type 为 json，除非是 GET 请求
      ...(method !== "GET" && { "Content-Type": "application/json" }),
    },
    // 关键：GET 请求的参数不通过 data 传递，已经在 url 中处理
    // 注意：axios 自身 config 也有 params 字段，但我们选择在 httpClient 统一处理 URL 拼接
  };

  if (!auth && config.headers) {
    delete config.headers.Authorization;
  }

  try {
    const response: AxiosResponse<ApiResult<TData>> =
      await axiosInstance.request(config);

    const apiResult = response.data;

    if (apiResult.success === false) {
      throw {
        message: apiResult.message || "业务逻辑处理失败",
        status: response.status,
        data: apiResult,
      } as ApiError;
    }

    if (apiResult.data === null || apiResult.data === undefined) {
      return undefined as TData;
    }

    return apiResult.data;
  } catch (error) {
    throw error;
  }
};
