/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.response;

import com.inteagle.sdk.model.MqttInfo;

/**
 * /me/mqtt 接口响应
 */
public class MqttInfoResponse {

    private int status;
    private int code;
    private MqttInfo data;
    private String msg;
    private String message;

    public boolean isSuccess() {
        return status == 0 || code == 0;
    }

    public MqttInfo getData() {
        return data;
    }

    public void setData(MqttInfo data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return msg != null ? msg : message;
    }
}
