/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.inteagle.sdk.model.attr.AttributeHelper;

import java.util.List;
import java.util.Map;

/**
 * 设备信息
 * <p>
 * 字段说明:
 * <ul>
 *   <li>deviceId - 设备唯一标识</li>
 *   <li>customerId - 所属客户 ID</li>
 *   <li>name - 设备名称</li>
 *   <li>type - 设备类型</li>
 *   <li>status - 连接状态 ("online" 或 "offline")</li>
 *   <li>deviceProfileId - 设备配置 ID</li>
 *   <li>createdAt - 创建时间 (ISO 8601 格式)</li>
 *   <li>attributes - 扩展属性</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Device {

    private String deviceId;
    private String customerId;
    private String name;
    private String type;
    private String status;
    private String deviceProfileId;
    private String createdAt;
    private String description;
    private String serialNumber;
    private String manufacturer;
    private String model;
    private Map<String, Object> attributes;

    /**
     * 设备关联的监测点列表
     * <p>
     * 仅当 includeMonitoringPoints=true 时返回
     */
    private List<MonitoringPointRef> monitoringPoints;

    /**
     * 设备关联的告警规则列表
     * <p>
     * 仅当 includeAlarmRules=true 时返回
     */
    private List<AlarmRule> alarmRules;

    public Device() {
    }

    /**
     * 监测点简要信息
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MonitoringPointRef {
        private String pointId;
        private String name;
        private String pointType;

        public String getPointId() { return pointId; }
        public void setPointId(String pointId) { this.pointId = pointId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPointType() { return pointType; }
        public void setPointType(String pointType) { this.pointType = pointType; }
    }

    // ==================== Getters & Setters ====================

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceProfileId() {
        return deviceProfileId;
    }

    public void setDeviceProfileId(String deviceProfileId) {
        this.deviceProfileId = deviceProfileId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // ==================== 类型安全的属性访问方法 ====================

    /**
     * 获取指定类型的属性值
     * <p>
     * 使用示例:
     * <pre>
     * Boolean active = device.getAttribute("active", Boolean.class);
     * MyCustomAttr attr = device.getAttribute("myAttr", MyCustomAttr.class);
     * </pre>
     *
     * @param key  属性键名
     * @param type 目标类型
     * @param <T>  返回类型
     * @return 转换后的属性值，如果不存在或转换失败返回 null
     */
    public <T> T getAttribute(String key, Class<T> type) {
        return AttributeHelper.getAttribute(attributes, key, type);
    }

    /**
     * 获取复杂类型的属性值（如 List、泛型等）
     * <p>
     * 使用示例:
     * <pre>
     * List&lt;Target&gt; targets = device.getAttribute("targets", AttrTypes.TARGET_LIST);
     * List&lt;String&gt; tags = device.getAttribute("tags", new TypeReference&lt;List&lt;String&gt;&gt;(){});
     * </pre>
     *
     * @param key     属性键名
     * @param typeRef 类型引用
     * @param <T>     返回类型
     * @return 转换后的属性值，如果不存在或转换失败返回 null
     */
    public <T> T getAttribute(String key, TypeReference<T> typeRef) {
        return AttributeHelper.getAttribute(attributes, key, typeRef);
    }

    /** 获取字符串属性 */
    public String getStringAttr(String key) {
        return AttributeHelper.getString(attributes, key);
    }

    /** 获取整数属性 */
    public Integer getIntAttr(String key) {
        return AttributeHelper.getInt(attributes, key);
    }

    /** 获取长整数属性 */
    public Long getLongAttr(String key) {
        return AttributeHelper.getLong(attributes, key);
    }

    /** 获取浮点数属性 */
    public Double getDoubleAttr(String key) {
        return AttributeHelper.getDouble(attributes, key);
    }

    /** 获取布尔属性 */
    public Boolean getBoolAttr(String key) {
        return AttributeHelper.getBool(attributes, key);
    }

    /** 检查是否存在指定属性 */
    public boolean hasAttribute(String key) {
        return AttributeHelper.hasAttribute(attributes, key);
    }

    public List<MonitoringPointRef> getMonitoringPoints() {
        return monitoringPoints;
    }

    public void setMonitoringPoints(List<MonitoringPointRef> monitoringPoints) {
        this.monitoringPoints = monitoringPoints;
    }

    public List<AlarmRule> getAlarmRules() {
        return alarmRules;
    }

    public void setAlarmRules(List<AlarmRule> alarmRules) {
        this.alarmRules = alarmRules;
    }

    /**
     * 判断设备是否在线
     *
     * @return true 如果设备在线
     */
    public boolean isOnline() {
        return "online".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Device{" +
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
