/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

/**
 * 凭据信息
 * <p>
 * 通过 /me API 获取当前 AK/SK 对应的凭据信息。
 *
 * <p>使用示例:
 * <pre>{@code
 * InteagleClient client = InteagleClient.builder()
 *     .endpoint("https://api.example.com")
 *     .credentials("ak_xxx", "sk_xxx")
 *     .build();
 *
 * CredentialInfo info = client.me();
 * String customerId = info.getCustomerId();  // 用于 MQTT Topic
 * }</pre>
 */
public class CredentialInfo {

    private String customerId;
    private String credentialId;
    private String accessKeyId;

    public CredentialInfo() {
    }

    public CredentialInfo(String customerId, String credentialId, String accessKeyId) {
        this.customerId = customerId;
        this.credentialId = credentialId;
        this.accessKeyId = accessKeyId;
    }

    /**
     * 获取客户 ID
     * <p>
     * 用于 MQTT Topic 前缀: inteagle/{customerId}/...
     */
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * 获取凭据 ID
     */
    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    /**
     * 获取 Access Key ID
     */
    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    @Override
    public String toString() {
        return "CredentialInfo{" +
                "customerId='" + customerId + '\'' +
                ", credentialId='" + credentialId + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                '}';
    }
}
