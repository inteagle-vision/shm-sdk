/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
