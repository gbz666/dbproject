export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export interface ApiResult<T> {
  success: boolean
  message?: string
  data: T|null
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  expiredAt: string
}

export interface SearchFilters {
  keyword?: string
  tags?: string[]
  authorId?: string
  page: number
  pageSize: number
  sort?: 'latest' | 'popular' | 'rating'
}
