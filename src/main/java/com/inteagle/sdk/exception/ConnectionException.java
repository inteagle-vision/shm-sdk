/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

/**
 * 连接异常
 * <p>
 * 当网络连接失败时抛出，例如:
 * <ul>
 *   <li>无法连接到服务器</li>
 *   <li>DNS 解析失败</li>
 *   <li>连接超时</li>
 *   <li>SSL/TLS 握手失败</li>
 * </ul>
 *
 * <h3>处理建议:</h3>
 * <ul>
 *   <li>检查网络连接</li>
 *   <li>检查服务端点 URL 是否正确</li>
 *   <li>实现重试机制</li>
 * </ul>
 */
public class ConnectionException extends SdkException {

    private static final long serialVersionUID = 1L;
    private static final int STATUS_CODE = 0; // 非 HTTP 错误
    private static final String ERROR_CODE = "CONNECTION_ERROR";

    public ConnectionException(String message) {
        super(STATUS_CODE, ERROR_CODE, message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(STATUS_CODE, ERROR_CODE, message, cause);
    }

    /**
     * 创建连接超时异常
     */
    public static ConnectionException timeout(String endpoint) {
        return new ConnectionException("Connection timeout: " + endpoint);
    }

    /**
     * 创建 DNS 解析失败异常
     */
    public static ConnectionException dnsFailure(String host) {
        return new ConnectionException("DNS resolution failed: " + host);
    }

    /**
     * 创建 SSL 握手失败异常
     */
    public static ConnectionException sslFailure(String message, Throwable cause) {
        return new ConnectionException("SSL handshake failed: " + message, cause);
    }

    /**
     * 创建连接拒绝异常
     */
    public static ConnectionException refused(String endpoint) {
        return new ConnectionException("Connection refused: " + endpoint);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
