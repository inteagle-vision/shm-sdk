/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

/**
 * 限流异常 (HTTP 429)
 * <p>
 * 当请求频率超过限制时抛出。
 *
 * <h3>处理建议:</h3>
 * <ul>
 *   <li>等待 {@link #getRetryAfterMs()} 毫秒后重试</li>
 *   <li>实现指数退避重试策略</li>
 *   <li>考虑缓存频繁访问的数据</li>
 * </ul>
 */
public class RateLimitException extends SdkException {

    private static final long serialVersionUID = 1L;
    private static final int STATUS_CODE = 429;
    private static final String ERROR_CODE = "RATE_LIMITED";

    /**
     * 建议的重试等待时间 (毫秒)
     */
    private final long retryAfterMs;

    public RateLimitException(String message) {
        this(message, 60000L); // 默认 60 秒
    }

    public RateLimitException(String message, long retryAfterMs) {
        super(STATUS_CODE, ERROR_CODE, message);
        this.retryAfterMs = retryAfterMs;
    }

    /**
     * 获取建议的重试等待时间 (毫秒)
     */
    public long getRetryAfterMs() {
        return retryAfterMs;
    }

    /**
     * 获取建议的重试等待时间 (秒)
     */
    public long getRetryAfterSeconds() {
        return retryAfterMs / 1000;
    }

    @Override
    public boolean isRetryable() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("RateLimitException{retryAfterMs=%d, message='%s'}",
                retryAfterMs, getMessage());
    }
}
