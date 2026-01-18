/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

/**
 * SDK 异常基类
 * <p>
 * 所有 SDK 操作可能抛出的异常的父类。
 * 提供统一的错误码和错误消息结构。
 *
 * <h3>异常层次结构:</h3>
 * <pre>
 * SdkException
 * ├── AuthenticationException  (401 - 认证失败)
 * ├── AuthorizationException   (403 - 权限不足)
 * ├── NotFoundException        (404 - 资源不存在)
 * ├── RateLimitException       (429 - 限流)
 * ├── ConnectionException      (网络错误)
 * └── ServerException          (5xx - 服务器错误)
 * </pre>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * try {
 *     client.devices().get("non-existent");
 * } catch (NotFoundException e) {
 *     // 设备不存在
 * } catch (AuthenticationException e) {
 *     // 凭证无效
 * } catch (SdkException e) {
 *     // 其他错误
 *     System.err.println("Error: " + e.getErrorCode() + " - " + e.getMessage());
 * }
 * }</pre>
 */
public class SdkException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * HTTP 状态码 (如 400, 401, 404, 500)
     * <p>
     * 对于非 HTTP 错误 (如网络错误)，使用 0
     */
    private final int statusCode;

    /**
     * 错误码 (如 "AUTH_FAILED", "NOT_FOUND", "RATE_LIMITED")
     * <p>
     * 用于程序化错误处理
     */
    private final String errorCode;

    /**
     * 请求 ID (如果有)
     * <p>
     * 用于追踪和调试
     */
    private final String requestId;

    /**
     * 构造函数
     *
     * @param statusCode HTTP 状态码
     * @param errorCode  错误码
     * @param message    错误消息
     */
    public SdkException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.requestId = null;
    }

    /**
     * 构造函数 (带原因)
     *
     * @param statusCode HTTP 状态码
     * @param errorCode  错误码
     * @param message    错误消息
     * @param cause      原因
     */
    public SdkException(int statusCode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.requestId = null;
    }

    /**
     * 构造函数 (完整)
     *
     * @param statusCode HTTP 状态码
     * @param errorCode  错误码
     * @param message    错误消息
     * @param requestId  请求 ID
     * @param cause      原因
     */
    public SdkException(int statusCode, String errorCode, String message, String requestId, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.requestId = requestId;
    }

    /**
     * 获取 HTTP 状态码
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 获取错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取请求 ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 是否是客户端错误 (4xx)
     */
    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * 是否是服务器错误 (5xx)
     */
    public boolean isServerError() {
        return statusCode >= 500;
    }

    /**
     * 是否可重试
     * <p>
     * 一般来说，网络错误和服务器错误可重试，客户端错误不可重试
     */
    public boolean isRetryable() {
        return statusCode == 0 || statusCode >= 500 || statusCode == 429;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("{statusCode=").append(statusCode);
        sb.append(", errorCode='").append(errorCode).append('\'');
        if (requestId != null) {
            sb.append(", requestId='").append(requestId).append('\'');
        }
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
