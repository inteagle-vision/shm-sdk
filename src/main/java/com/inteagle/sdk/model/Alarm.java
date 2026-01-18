/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * 告警信息
 * <p>
 * 字段说明:
 * - severity: ALERT(预警), ALARM(报警), ACTION(紧急)
 * - status: ACTIVE_UNACK, ACTIVE_ACK, CLEARED_UNACK, CLEARED_ACK
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Alarm {

    private String id;
    private String customerId;
    private String type;
    private String severity;
    private String status;

    // 来源信息
    private Originator originator;
    private String originatorId;
    private String originatorType;
    private String originatorName;

    // 详情
    private AlarmDetail detail;
    private Map<String, Object> details;

    // 状态标志
    private Boolean active;
    private Boolean acknowledged;
    private Boolean cleared;

    // 时间戳
    private Long startTs;
    private Long createdTime;
    private Long endTs;
    private Long ackTs;
    private Long clearTs;

    private String assigneeId;

    /**
     * 告警来源
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Originator {
        public String customerId;
        public String entityType;
        public String entityId;
    }

    /**
     * 告警详情
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlarmDetail {
        public String metric;
        public Object value;
        public Object threshold;
        public String message;
    }

    public Alarm() {
    }

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    public String getOriginatorType() {
        return originatorType;
    }

    public void setOriginatorType(String originatorType) {
        this.originatorType = originatorType;
    }

    public String getOriginatorName() {
        return originatorName;
    }

    public void setOriginatorName(String originatorName) {
        this.originatorName = originatorName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public Boolean getCleared() {
        return cleared;
    }

    public void setCleared(Boolean cleared) {
        this.cleared = cleared;
    }

    public Long getStartTs() {
        return startTs;
    }

    public void setStartTs(Long startTs) {
        this.startTs = startTs;
    }

    public Long getEndTs() {
        return endTs;
    }

    public void setEndTs(Long endTs) {
        this.endTs = endTs;
    }

    public Long getAckTs() {
        return ackTs;
    }

    public void setAckTs(Long ackTs) {
        this.ackTs = ackTs;
    }

    public Long getClearTs() {
        return clearTs;
    }

    public void setClearTs(Long clearTs) {
        this.clearTs = clearTs;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Originator getOriginator() {
        return originator;
    }

    public void setOriginator(Originator originator) {
        this.originator = originator;
    }

    public AlarmDetail getDetail() {
        return detail;
    }

    public void setDetail(AlarmDetail detail) {
        this.detail = detail;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    /**
     * 是否为严重告警 (ACTION 级别)
     */
    public boolean isCritical() {
        return "ACTION".equals(severity) || "CRITICAL".equals(severity);
    }

    /**
     * 是否需要关注 (活跃且未确认)
     */
    public boolean needsAttention() {
        return Boolean.TRUE.equals(active) && !Boolean.TRUE.equals(acknowledged);
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", severity='" + severity + '\'' +
                ", active=" + active +
                ", acknowledged=" + acknowledged +
                '}';
    }
}
