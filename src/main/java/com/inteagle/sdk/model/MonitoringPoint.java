/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.inteagle.sdk.model.attr.AttributeHelper;

import java.util.List;
import java.util.Map;

/**
 * 监测点信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class MonitoringPoint {

    @JsonAlias("pointId")
    private String id;
    private String name;
    @JsonAlias("pointType")
    private String type;
    private String projectId;
    private String deviceId;
    private String deviceName;
    private String customerId;
    private Boolean active;
    private Double latitude;
    private Double longitude;
    private Long createdAt;  // 毫秒时间戳
    private Long updatedAt;
    private Map<String, Object> additionalInfo;

    // Nop API 返回的额外字段
    private String pointCode;
    private String monitoringItem;
    private String unit;
    private String status;
    private String description;
    private Location location;
    private Double warningThreshold;
    private Double alarmThreshold;
    private List<AlarmRule> alarmRules;
    private Map<String, Object> attributes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        public String address;
        public Double latitude;
        public Double longitude;
        public Double elevation;
    }

    public MonitoringPoint() {
    }

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPointCode() {
        return pointCode;
    }

    public void setPointCode(String pointCode) {
        this.pointCode = pointCode;
    }

    public String getMonitoringItem() {
        return monitoringItem;
    }

    public void setMonitoringItem(String monitoringItem) {
        this.monitoringItem = monitoringItem;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(Double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public Double getAlarmThreshold() {
        return alarmThreshold;
    }

    public void setAlarmThreshold(Double alarmThreshold) {
        this.alarmThreshold = alarmThreshold;
    }

    public List<AlarmRule> getAlarmRules() {
        return alarmRules;
    }

    public void setAlarmRules(List<AlarmRule> alarmRules) {
        this.alarmRules = alarmRules;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * 是否有位置信息
     */
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    // ==================== 类型安全的属性访问方法 ====================

    /** 获取指定类型的属性值 */
    public <T> T getAttribute(String key, Class<T> type) {
        return AttributeHelper.getAttribute(attributes, key, type);
    }

    /** 获取复杂类型的属性值 */
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

    /** 获取布尔属性 */
    public Boolean getBoolAttr(String key) {
        return AttributeHelper.getBool(attributes, key);
    }

    /** 检查是否存在指定属性 */
    public boolean hasAttribute(String key) {
        return AttributeHelper.hasAttribute(attributes, key);
    }

    @Override
    public String toString() {
        return "MonitoringPoint{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", active=" + active +
                '}';
    }
}
