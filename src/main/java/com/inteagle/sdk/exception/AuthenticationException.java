/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

/**
 * 认证失败异常 (HTTP 401)
 * <p>
 * 当以下情况发生时抛出:
 * <ul>
 *   <li>Access Key 无效或不存在</li>
 *   <li>Secret Key 错误</li>
 *   <li>签名验证失败</li>
 *   <li>凭证已过期或被禁用</li>
 * </ul>
 *
 * <h3>处理建议:</h3>
 * <ul>
 *   <li>检查 Access Key 和 Secret Key 是否正确</li>
 *   <li>确认凭证是否已激活</li>
 *   <li>检查系统时间是否正确 (签名验证依赖时间戳)</li>
 * </ul>
 */
public class AuthenticationException extends SdkException {

    private static final long serialVersionUID = 1L;
    private static final int STATUS_CODE = 401;
    private static final String ERROR_CODE = "AUTH_FAILED";

    public AuthenticationException(String message) {
        super(STATUS_CODE, ERROR_CODE, message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(STATUS_CODE, ERROR_CODE, message, cause);
    }

    /**
     * 创建 "凭证无效" 异常
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid access key or secret key");
    }

    /**
     * 创建 "签名无效" 异常
     */
    public static AuthenticationException invalidSignature() {
        return new AuthenticationException("Request signature verification failed");
    }

    /**
     * 创建 "凭证已过期" 异常
     */
    public static AuthenticationException expired() {
        return new AuthenticationException("Credentials have expired");
    }

    /**
     * 创建 "凭证已禁用" 异常
     */
    public static AuthenticationException disabled() {
        return new AuthenticationException("Credentials are disabled");
    }

    /**
     * 创建 "时间戳过期" 异常
     */
    public static AuthenticationException timestampExpired() {
        return new AuthenticationException("Request timestamp has expired, check system time");
    }
}
