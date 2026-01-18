/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.response;

import com.inteagle.sdk.exception.SdkException;

/**
 * API 响应包装器
 * <p>
 * 服务端响应格式:
 * <pre>
 * // 成功
 * {"code": 0, "data": {...}}
 *
 * // 错误
 * {"code": 40001, "message": "Invalid parameter"}
 * </pre>
 *
 * @param <T> 数据类型
 */
public class ApiResponseWrapper<T> {

    /**
     * 响应码：0 表示成功，非 0 表示错误
     */
    private int code;

    /**
     * 响应数据（成功时返回）
     */
    private T data;

    /**
     * 错误消息（错误时返回）
     */
    private String message;

    public ApiResponseWrapper() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return code == 0;
    }

    /**
     * 获取数据，如果响应失败则抛出异常
     *
     * @return 响应数据
     * @throws SdkException 如果响应失败
     */
    public T getDataOrThrow() throws SdkException {
        if (!isSuccess()) {
            throw new SdkException(code, "API_ERROR", message != null ? message : "Unknown error");
        }
        return data;
    }
}
