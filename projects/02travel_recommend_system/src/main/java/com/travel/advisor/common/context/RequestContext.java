package com.travel.advisor.common.context;

/**
 * 请求上下文工具类，使用 ThreadLocal 存储每个请求的唯一标识（requestId），以便在日志记录和分布式追踪中使用。
 */
public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    public static String getRequestId() {
        return REQUEST_ID_HOLDER.get();
    }

    public static void clear() {
        REQUEST_ID_HOLDER.remove();
    }
}
