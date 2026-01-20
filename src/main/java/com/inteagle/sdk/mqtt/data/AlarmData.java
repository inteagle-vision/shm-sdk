/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 告警数据模型 (3A 格式)
 * <p>
 * 告警数据包含设备产生的告警信息，包括告警类型、严重级别、状态等。
 *
 * <p>严重级别 (3A 格式):
 * <ul>
 *   <li>ACTION - 需要立即处理的严重告警</li>
 *   <li>ALARM - 需要关注的告警</li>
 *   <li>ALERT - 提示性告警</li>
 * </ul>
 *
 * <p>使用示例:
 * <pre>{@code
 * AlarmData alarm = AlarmData.from(message);
 * String alarmType = alarm.getAlarmType();
 * AlarmSeverity severity = alarm.getSeverity();
 * if (severity == AlarmSeverity.ACTION) {
 *     // 处理紧急告警
 * }
 * }</pre>
 */
public class AlarmData {

    /**
     * 告警严重级别 (3A 格式)
     */
    public enum AlarmSeverity {
        ACTION,  // 严重告警，需要立即处理
        ALARM,   // 一般告警，需要关注
        ALERT,   // 提示告警
        UNKNOWN;

        public static AlarmSeverity fromString(String text) {
            if (text == null) return UNKNOWN;
            try {
                return AlarmSeverity.valueOf(text.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    /**
     * 告警状态
     */
    public enum AlarmStatus {
        ACTIVE_UNACK,    // 活跃未确认
        ACTIVE_ACK,      // 活跃已确认
        CLEARED_UNACK,   // 已清除未确认
        CLEARED_ACK,     // 已清除已确认
        UNKNOWN;

        public static AlarmStatus fromString(String text) {
            if (text == null) return UNKNOWN;
            try {
                return AlarmStatus.valueOf(text.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    private final String topic;
    private final long timestamp;
    private final String id;
    private final String alarmType;
    private final AlarmSeverity severity;
    private final AlarmStatus status;
    private final String originatorType;
    private final String originatorId;
    private final Map<String, Object> detail;
    private final JsonNode rawPayload;

    private AlarmData(Builder builder) {
        this.topic = builder.topic;
        this.timestamp = builder.timestamp;
        this.id = builder.id;
        this.alarmType = builder.alarmType;
        this.severity = builder.severity;
        this.status = builder.status;
        this.originatorType = builder.originatorType;
        this.originatorId = builder.originatorId;
        this.detail = builder.detail;
        this.rawPayload = builder.rawPayload;
    }

    /**
     * 从 JsonNode 解析告警数据
     */
    public static AlarmData from(String topic, long timestamp, JsonNode payload) {
        Builder builder = new Builder()
                .topic(topic)
                .timestamp(timestamp)
                .rawPayload(payload);

        if (payload != null) {
            builder.id(getTextValue(payload, "id"))
                    .alarmType(getTextValue(payload, "alarmType"))
                    .severity(AlarmSeverity.fromString(getTextValue(payload, "severity")))
                    .status(AlarmStatus.fromString(getTextValue(payload, "status")));

            // 解析 originator
            JsonNode originator = payload.path("originator");
            if (!originator.isMissingNode()) {
                builder.originatorType(getTextValue(originator, "entityType"))
                        .originatorId(getTextValue(originator, "id"));
            }

            // 解析 detail
            JsonNode detailNode = payload.path("detail");
            if (!detailNode.isMissingNode() && detailNode.isObject()) {
                Map<String, Object> detail = new HashMap<>();
                Iterator<Map.Entry<String, JsonNode>> fields = detailNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    detail.put(field.getKey(), jsonNodeToObject(field.getValue()));
                }
                builder.detail(detail);
            }
        }

        return builder.build();
    }

    private static String getTextValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static Object jsonNodeToObject(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isBoolean()) return node.booleanValue();
        if (node.isInt()) return node.intValue();
        if (node.isLong()) return node.longValue();
        if (node.isDouble()) return node.doubleValue();
        if (node.isTextual()) return node.textValue();
        return node.toString();
    }

    // ==================== Getters ====================

    public String getTopic() { return topic; }
    public long getTimestamp() { return timestamp; }
    public String getId() { return id; }
    public String getAlarmType() { return alarmType; }
    public AlarmSeverity getSeverity() { return severity; }
    public AlarmStatus getStatus() { return status; }
    public String getOriginatorType() { return originatorType; }
    public String getOriginatorId() { return originatorId; }
    public Map<String, Object> getDetail() { return detail != null ? new HashMap<>(detail) : new HashMap<>(); }
    public JsonNode getRawPayload() { return rawPayload; }

    /**
     * 是否是严重告警 (ACTION)
     */
    public boolean isCritical() {
        return severity == AlarmSeverity.ACTION;
    }

    /**
     * 是否是活跃告警
     */
    public boolean isActive() {
        return status == AlarmStatus.ACTIVE_UNACK || status == AlarmStatus.ACTIVE_ACK;
    }

    /**
     * 是否已清除
     */
    public boolean isCleared() {
        return status == AlarmStatus.CLEARED_UNACK || status == AlarmStatus.CLEARED_ACK;
    }

    /**
     * 获取详情中的值
     */
    public Object getDetailValue(String key) {
        return detail != null ? detail.get(key) : null;
    }

    @Override
    public String toString() {
        return "AlarmData{" +
                "id='" + id + '\'' +
                ", alarmType='" + alarmType + '\'' +
                ", severity=" + severity +
                ", status=" + status +
                ", originatorId='" + originatorId + '\'' +
                '}';
    }

    // ==================== Builder ====================

    public static class Builder {
        private String topic;
        private long timestamp;
        private String id;
        private String alarmType;
        private AlarmSeverity severity = AlarmSeverity.UNKNOWN;
        private AlarmStatus status = AlarmStatus.UNKNOWN;
        private String originatorType;
        private String originatorId;
        private Map<String, Object> detail;
        private JsonNode rawPayload;

        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }
        public Builder id(String id) { this.id = id; return this; }
        public Builder alarmType(String alarmType) { this.alarmType = alarmType; return this; }
        public Builder severity(AlarmSeverity severity) { this.severity = severity; return this; }
        public Builder status(AlarmStatus status) { this.status = status; return this; }
        public Builder originatorType(String originatorType) { this.originatorType = originatorType; return this; }
        public Builder originatorId(String originatorId) { this.originatorId = originatorId; return this; }
        public Builder detail(Map<String, Object> detail) { this.detail = detail; return this; }
        public Builder rawPayload(JsonNode rawPayload) { this.rawPayload = rawPayload; return this; }
        public AlarmData build() { return new AlarmData(this); }
    }
}
