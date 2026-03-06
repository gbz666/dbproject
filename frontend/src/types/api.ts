
export interface AuthTokens {
  accessToken: string
  refreshToken: string
  expiredAt: string
}

/** 登录接口返回的数据，对应后端 LoginResponse（仅使用 staffName，无 username） */
export interface LoginResponse {
  token: string
  userId: number | null
  staffName: string
  expiration: number | null
}

export interface SearchFilters {
  keyword?: string
  tags?: string[]
  authorId?: string
  page: number
  pageSize: number
  sort?: 'latest' | 'popular' | 'rating'
}
export interface ApiResult<T = unknown> {
    code: number;
    message: string;
    success: boolean;
    data: T;
}

/**
 * PageHelper 返回的分页信息结构
 * @template T 列表数据类型
 */
export interface PageInfo<T> {
    pageNum: number;      // 当前页码
    pageSize: number;     // 每页数量
    size: number;         // 当前页实际数量
    startRow: number;     // 当前页起始行号
    endRow: number;       // 当前页结束行号
    total: number;        // 总记录数
    pages: number;        // 总页数
    list: T[];            // 列表数据
    prePage: number;      // 上一页
    nextPage: number;     // 下一页
    isFirstPage: boolean; // 是否第一页
    isLastPage: boolean;  // 是否最后一页
    hasPreviousPage: boolean; // 是否有上一页
    hasNextPage: boolean;     // 是否有下一页
    navigatePages: number;    // 导航页码数
    navigatepageNums: number[]; // 导航页码数组
    navigateFirstPage: number;  // 导航第一页
    navigateLastPage: number;   // 导航最后一页
}