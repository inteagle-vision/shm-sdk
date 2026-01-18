/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt;

/**
 * Topic 构建器 - 帮助用户构建 MQTT 订阅 Topic
 *
 * <p>Topic 格式: inteagle/{customerId}/p/{projectId}/[类型]/[ID]
 *
 * <p>使用示例:
 * <pre>{@code
 * String topic = TopicBuilder.forCustomer(customerId)
 *     .project(projectId)
 *     .device(deviceId)
 *     .build();
 *
 * // 订阅项目下所有数据
 * String allTopic = TopicBuilder.forCustomer(customerId)
 *     .project(projectId)
 *     .all()
 *     .build();
 * }</pre>
 */
public class TopicBuilder {

    private static final String PREFIX = "inteagle";

    private final String customerId;
    private String projectId;
    private String resourceType;  // d (device), mp (monitoring point)
    private String resourceId;
    private boolean wildcard = false;

    private TopicBuilder(String customerId) {
        this.customerId = customerId;
    }

    /**
     * 创建指定客户的 Topic 构建器
     * @param customerId 客户 ID
     */
    public static TopicBuilder forCustomer(String customerId) {
        return new TopicBuilder(customerId);
    }

    /**
     * 设置项目 ID
     */
    public TopicBuilder project(String projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * 设置设备 ID
     */
    public TopicBuilder device(String deviceId) {
        this.resourceType = "d";
        this.resourceId = deviceId;
        return this;
    }

    /**
     * 设置监测点 ID
     */
    public TopicBuilder monitoringPoint(String pointId) {
        this.resourceType = "mp";
        this.resourceId = pointId;
        return this;
    }

    /**
     * 订阅所有子级数据 (使用 # 通配符)
     */
    public TopicBuilder all() {
        this.wildcard = true;
        return this;
    }

    /**
     * 构建 Topic 字符串
     */
    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX).append("/").append(customerId);

        if (projectId != null) {
            sb.append("/p/").append(projectId);
        }

        if (resourceType != null && resourceId != null) {
            sb.append("/").append(resourceType).append("/").append(resourceId);
        }

        if (wildcard) {
            sb.append("/#");
        }

        return sb.toString();
    }

    /**
     * 构建项目级别的 Topic (订阅项目下所有数据)
     * @param customerId 客户 ID
     * @param projectId 项目 ID
     */
    public static String projectTopic(String customerId, String projectId) {
        return forCustomer(customerId).project(projectId).all().build();
    }

    /**
     * 构建设备级别的 Topic
     * @param customerId 客户 ID
     * @param projectId 项目 ID
     * @param deviceId 设备 ID
     */
    public static String deviceTopic(String customerId, String projectId, String deviceId) {
        return forCustomer(customerId).project(projectId).device(deviceId).build();
    }

    /**
     * 构建监测点级别的 Topic
     * @param customerId 客户 ID
     * @param projectId 项目 ID
     * @param pointId 监测点 ID
     */
    public static String pointTopic(String customerId, String projectId, String pointId) {
        return forCustomer(customerId).project(projectId).monitoringPoint(pointId).build();
    }
}
