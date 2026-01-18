/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

/**
 * 服务器错误异常 (HTTP 5xx)
 * <p>
 * 当服务器发生内部错误时抛出。
 *
 * <h3>处理建议:</h3>
 * <ul>
 *   <li>实现重试机制 (使用指数退避)</li>
 *   <li>记录错误日志用于排查</li>
 *   <li>如果持续发生，联系技术支持</li>
 * </ul>
 */
public class ServerException extends SdkException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "SERVER_ERROR";

    public ServerException(int statusCode, String message) {
        super(statusCode, ERROR_CODE, message);
    }

    public ServerException(int statusCode, String message, Throwable cause) {
        super(statusCode, ERROR_CODE, message, cause);
    }

    /**
     * 创建 500 内部错误
     */
    public static ServerException internalError(String message) {
        return new ServerException(500, message);
    }

    /**
     * 创建 502 网关错误
     */
    public static ServerException badGateway(String message) {
        return new ServerException(502, message);
    }

    /**
     * 创建 503 服务不可用
     */
    public static ServerException serviceUnavailable(String message) {
        return new ServerException(503, message);
    }

    /**
     * 创建 504 网关超时
     */
    public static ServerException gatewayTimeout(String message) {
        return new ServerException(504, message);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
