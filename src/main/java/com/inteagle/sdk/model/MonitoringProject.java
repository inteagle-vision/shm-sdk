/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 监测项目信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class MonitoringProject {

    /**
     * 项目 ID (服务端返回 projectId)
     */
    @JsonAlias({"projectId", "id"})
    private String id;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 监测类型 (服务端返回 monitoringType)
     */
    @JsonAlias({"monitoringType", "type"})
    private String type;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 地区
     */
    private String region;

    /**
     * 项目状态 (服务端返回 status 字符串)
     * inProgress -> true, completed/planning -> false
     */
    @JsonAlias({"status", "active"})
    private Object activeOrStatus;

    /**
     * 客户 ID
     */
    private String customerId;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;

    private Map<String, Object> additionalInfo;

    public MonitoringProject() {
    }

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("projectId")
    public void setProjectId(String projectId) {
        this.id = projectId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 获取项目是否活跃
     * 兼容服务端返回的 status 字符串和 active 布尔值
     */
    public Boolean getActive() {
        if (activeOrStatus == null) {
            return null;
        }
        if (activeOrStatus instanceof Boolean) {
            return (Boolean) activeOrStatus;
        }
        // status 字符串转换为 active 布尔值
        String status = String.valueOf(activeOrStatus);
        return "inProgress".equalsIgnoreCase(status) || "active".equalsIgnoreCase(status);
    }

    public void setActive(Boolean active) {
        this.activeOrStatus = active;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.activeOrStatus = status;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "MonitoringProject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
