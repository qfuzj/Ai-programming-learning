/**
 * 文件上传与文件资源接口。
 */
import http from "@/api/http";

/**
 * 获取上传凭证 参数类型
 */
export interface UploadTokenParams {
  bizType?: string;
  fileName?: string;
  fileSize?: number;
  bizId?: number;
}
/**
 * 对应-文件上传回调DTO
 */
export interface UploadCallbackPayload {
  bucketName: string;
  objectKey: string;
  originalName: string;
  bizType: string;
  bizId?: number;
  thumbnailUrl?: string;
}

/**
 * 对应-文件资源实体
 */
export interface FileResourceItem {
  id: number;
  bucketName?: string;
  objectKey?: string;
  originalName?: string;
  fileSize?: number;
  fileType?: string;
  fileExtension?: string;
  fileHash?: string;
  url?: string;
  thumbnailUrl?: string;
  bizType?: string;
  bizId?: number;
  uploaderId?: number;
  uploaderType?: number;
  status?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface UploadTokenResult {
  [key: string]: unknown;
}

export function getUploadToken(params: UploadTokenParams = {}): Promise<UploadTokenResult> {
  return http.post("/api/common/files/upload-token", null, { params });
}

export function uploadCallback(payload: UploadCallbackPayload): Promise<number> {
  return http.post("/api/common/files/callback", payload);
}

export function getFileResource(id: number): Promise<FileResourceItem> {
  return http.get<FileResourceItem>(`/api/common/files/${id}`);
}

export function deleteFileResource(id: number): Promise<void> {
  return http.delete<void>(`/api/common/files/${id}`);
}
