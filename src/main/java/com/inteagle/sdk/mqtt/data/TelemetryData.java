/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 遥测数据模型
 * <p>
 * 遥测数据是设备上报的实时测量值，如温度、湿度、压力等。
 *
 * <p>使用示例:
 * <pre>{@code
 * TelemetryData telemetry = TelemetryData.from(message);
 * Double temperature = telemetry.getDouble("temperature");
 * Map<String, Object> allValues = telemetry.getValues();
 * }</pre>
 */
public class TelemetryData {

    private final String topic;
    private final long timestamp;
    private final String deviceId;
    private final Map<String, Object> values;
    private final JsonNode rawPayload;

    private TelemetryData(String topic, long timestamp, String deviceId,
                          Map<String, Object> values, JsonNode rawPayload) {
        this.topic = topic;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.values = values;
        this.rawPayload = rawPayload;
    }

    /**
     * 从 JsonNode 解析遥测数据
     */
    public static TelemetryData from(String topic, long timestamp, JsonNode payload) {
        String deviceId = extractDeviceId(topic);
        Map<String, Object> values = parseValues(payload);
        return new TelemetryData(topic, timestamp, deviceId, values, payload);
    }

    /**
     * 从 topic 中提取设备 ID
     * Topic 格式: inteagle/{customerId}/d/{deviceId} 或 inteagle/{customerId}/p/{projectId}/d/{deviceId}
     */
    private static String extractDeviceId(String topic) {
        if (topic == null) return null;
        String[] parts = topic.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("d".equals(parts[i]) || "mp".equals(parts[i])) {
                return parts[i + 1];
            }
        }
        return null;
    }

    /**
     * 解析 payload 中的所有值
     */
    private static Map<String, Object> parseValues(JsonNode payload) {
        Map<String, Object> values = new HashMap<>();
        if (payload != null && payload.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = payload.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                values.put(field.getKey(), jsonNodeToObject(field.getValue()));
            }
        }
        return values;
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
    public String getDeviceId() { return deviceId; }
    public Map<String, Object> getValues() { return new HashMap<>(values); }
    public JsonNode getRawPayload() { return rawPayload; }

    /**
     * 获取指定键的值
     */
    public Object get(String key) {
        return values.get(key);
    }

    /**
     * 获取指定键的 Double 值
     */
    public Double getDouble(String key) {
        Object value = values.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取指定键的 Integer 值
     */
    public Integer getInt(String key) {
        Object value = values.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取指定键的 Long 值
     */
    public Long getLong(String key) {
        Object value = values.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取指定键的 String 值
     */
    public String getString(String key) {
        Object value = values.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取指定键的 Boolean 值
     */
    public Boolean getBoolean(String key) {
        Object value = values.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    /**
     * 检查是否包含指定键
     */
    public boolean hasKey(String key) {
        return values.containsKey(key);
    }

    /**
     * 获取所有键名
     */
    public java.util.Set<String> getKeys() {
        return values.keySet();
    }

    /**
     * 创建一个只包含指定键的过滤副本
     *
     * @param keysToKeep 需要保留的键集合
     * @return 过滤后的 TelemetryData，如果没有匹配的键则返回 null
     */
    public TelemetryData filter(java.util.Set<String> keysToKeep) {
        if (keysToKeep == null || keysToKeep.isEmpty()) {
            return null;
        }

        Map<String, Object> filteredValues = new HashMap<>();
        for (String key : keysToKeep) {
            if (values.containsKey(key)) {
                filteredValues.put(key, values.get(key));
            }
        }

        if (filteredValues.isEmpty()) {
            return null;
        }

        return new TelemetryData(topic, timestamp, deviceId, filteredValues, rawPayload);
    }

    @Override
    public String toString() {
        return "TelemetryData{" +
                "deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", values=" + values +
                '}';
    }
}
