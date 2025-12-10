// 🛠️ 文件名: /src/utils/httpClient.ts

import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import { getCookie, removeCookie } from "./cookie"; 
import type { ApiResult } from "@/types/api"; 

// --- 错误类型定义 ---
/**
 * HTTP 客户端抛出的统一错误结构
 */
export interface ApiError {
  message: string; // 后端返回的或网络错误信息
  status: number; // HTTP 状态码 (0 表示网络错误)
  data?: any; // 后端返回的原始错误数据体
}

// --- Axios 实例创建 ---
export const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  timeout: 95000,
});

// --- 请求拦截器 - 保持不变 ---
axiosInstance.interceptors.request.use(
  (config) => {
    let token = localStorage.getItem("access_token");
    if (!token) {
      token = getCookie("access_token");
    }
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// --- 响应拦截器 - 仅处理认证和错误抛出（不进行数据剥离） ---
axiosInstance.interceptors.response.use(
  (response) => {
    // 成功拦截器：必须返回完整的 AxiosResponse
    // ⚠️ 关键修改：直接返回 response 对象，不再返回 response.data
    return response; 
  },
  (error) => {
    if (error.response) {
      // 服务器返回错误 (4xx, 5xx)
      const { status, data } = error.response;

      if (status === 401) {
        // ... (401 认证处理逻辑保持不变)
        localStorage.removeItem("access_token");
        // ...
        if (window.location.pathname !== "/auth/login") {
          window.location.href = "/auth/login";
        }
      }

      // 返回服务器的错误信息 (符合 ApiError 结构)
      return Promise.reject<ApiError>({ 
        message: data?.message || `请求失败 (HTTP ${status})`,
        status,
        data,
      });
    } else if (error.request) {
      // ... (网络错误处理)
      return Promise.reject<ApiError>({
        message: "网络错误，请检查您的网络连接",
        status: 0,
      });
    } else {
      // ... (其他错误处理)
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
  { method = "GET", body, headers, auth = true }: HttpRequestOptions = {}
): Promise<TData> => {
  const config: AxiosRequestConfig = {
    method,
    url: endpoint,
    data: body,
    headers: {
      ...headers,
    },
  };
  
  if (!auth && config.headers) {
    delete config.headers.Authorization;
  }
  
  try {
    // ⚠️ 关键修改：直接等待完整的 AxiosResponse
    const response: AxiosResponse<ApiResult<TData>> = await axiosInstance.request(config);
    
    const apiResult = response.data;

    // 检查业务逻辑是否成功（现在在 httpClient 内部进行）
    if (apiResult.success === false) {
      // 业务逻辑失败，手动抛出 ApiError 结构
      throw {
          message: apiResult.message || "业务逻辑处理失败",
          status: response.status,
          data: apiResult
      } as ApiError;
    }

    // 返回剥离后的业务数据
    if (apiResult.data === null || apiResult.data === undefined) {
        // 针对 DELETE 操作返回 void 的情况，返回 undefined/void
        return undefined as TData; 
    }

    return apiResult.data;

  } catch (error) {
    // 捕获拦截器抛出的 ApiError 或 httpClient 内部抛出的错误
    throw error;
  }
};