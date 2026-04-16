import request from "@/utils/request";
import type { AxiosRequestConfig } from "axios";

const http = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.get<any, T>(url, config);
  },
  post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.post<any, T>(url, data, config);
  },
  put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.put<any, T>(url, data, config);
  },
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.delete<any, T>(url, config);
  },
};

export default http;
