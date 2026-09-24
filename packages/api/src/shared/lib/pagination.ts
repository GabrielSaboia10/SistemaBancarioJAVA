export interface PaginationParams {
  page: number
  limit: number
  skip: number
}

export interface PaginatedResult<T> {
  data: T[]
  total: number
  page: number
  limit: number
  totalPages: number
}

export function parsePagination(query: Record<string, unknown>): PaginationParams {
  const page = Math.max(1, Number(query.page) || 1)
  const limit = Math.min(100, Math.max(1, Number(query.limit) || 20))
  return { page, limit, skip: (page - 1) * limit }
}

export function paginate<T>(data: T[], total: number, { page, limit }: PaginationParams): PaginatedResult<T> {
  return { data, total, page, limit, totalPages: Math.ceil(total / limit) }
}
