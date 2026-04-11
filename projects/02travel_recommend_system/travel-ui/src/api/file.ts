/**
 * 文件上传与文件资源接口。
 */
import http from "@/api/http";

export interface UploadTokenParams {
  bizType?: string;
  fileName?: string;
  fileSize?: number;
  bizId?: number;
}

export interface UploadCallbackPayload {
  bucketName: string;
  objectKey: string;
  originalName: string;
  bizType: string;
  bizId?: number;
  thumbnailUrl?: string;
}

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
  createTime?: string;
  updateTime?: string;
}

export function getUploadToken(params: UploadTokenParams = {}): Promise<Record<string, unknown>> {
  return http.post("/api/common/files/upload-token", null, { params });
}

export function uploadCallback(payload: UploadCallbackPayload): Promise<number> {
  return http.post("/api/common/files/callback", payload);
}

export function getFileResource(id: number): Promise<FileResourceItem> {
  return http.get(`/api/common/files/${id}`);
}

export function deleteFileResource(id: number): Promise<void> {
  return http.delete(`/api/common/files/${id}`);
}
