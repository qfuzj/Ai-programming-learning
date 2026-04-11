/**
 * 通用接口响应模型。
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp?: string;
  requestId?: string;
}

export interface PageQuery {
  pageNum: number;
  pageSize: number;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
  totalPage: number;
}
