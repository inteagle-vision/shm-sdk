/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

/**
 * MQTT 连接信息
 * <p>
 * 通过 /me/mqtt API 获取 MQTT 连接所需的配置信息。
 *
 * <p>使用示例:
 * <pre>{@code
 * InteagleClient client = InteagleClient.builder()
 *     .endpoint("https://api.example.com")
 *     .credentials("ak_xxx", "sk_xxx")
 *     .build();
 *
 * MqttInfo mqttInfo = client.mqttInfo();
 * String broker = mqttInfo.getBrokerHost();
 * int port = mqttInfo.getBrokerPort();
 * String customerId = mqttInfo.getCustomerId();
 * }</pre>
 */
public class MqttInfo {

    private String customerId;
    private String topicPrefix;
    private String brokerHost;
    private int brokerPort;
    private boolean useTls;

    public MqttInfo() {
    }

    public MqttInfo(String customerId, String topicPrefix, String brokerHost, int brokerPort, boolean useTls) {
        this.customerId = customerId;
        this.topicPrefix = topicPrefix;
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
        this.useTls = useTls;
    }

    /**
     * 获取客户 ID
     * <p>
     * 用于 MQTT Topic: {topicPrefix}/{customerId}/...
     */
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * 获取 Topic 前缀
     * <p>
     * 默认为 "inteagle"
     */
    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    /**
     * 获取 MQTT Broker 地址
     */
    public String getBrokerHost() {
        return brokerHost;
    }

    public void setBrokerHost(String brokerHost) {
        this.brokerHost = brokerHost;
    }

    /**
     * 获取 MQTT Broker 端口
     */
    public int getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }

    /**
     * 是否使用 TLS
     */
    public boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    @Override
    public String toString() {
        return "MqttInfo{" +
                "customerId='" + customerId + '\'' +
                ", topicPrefix='" + topicPrefix + '\'' +
                ", brokerHost='" + brokerHost + '\'' +
                ", brokerPort=" + brokerPort +
                ", useTls=" + useTls +
                '}';
    }
}
