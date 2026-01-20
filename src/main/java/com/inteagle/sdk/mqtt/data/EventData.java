/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 事件数据模型
 * <p>
 * 事件数据表示设备上报的离散事件，如开关状态变化、用户操作等。
 *
 * <p>使用示例:
 * <pre>{@code
 * EventData event = EventData.from(message);
 * String eventType = event.getEventType();
 * Map<String, Object> body = event.getBody();
 * }</pre>
 */
public class EventData {

    private final String topic;
    private final long timestamp;
    private final String deviceId;
    private final String eventType;
    private final Map<String, Object> body;
    private final JsonNode rawPayload;

    private EventData(String topic, long timestamp, String deviceId,
                      String eventType, Map<String, Object> body, JsonNode rawPayload) {
        this.topic = topic;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.eventType = eventType;
        this.body = body;
        this.rawPayload = rawPayload;
    }

    /**
     * 从 JsonNode 解析事件数据
     */
    public static EventData from(String topic, long timestamp, JsonNode payload) {
        String deviceId = extractDeviceId(topic);
        String eventType = null;
        Map<String, Object> body = new HashMap<>();

        if (payload != null) {
            if (payload.has("eventType")) {
                eventType = payload.get("eventType").asText();
            }
            if (payload.has("body") && payload.get("body").isObject()) {
                body = parseObject(payload.get("body"));
            } else if (payload.has("data") && payload.get("data").isObject()) {
                body = parseObject(payload.get("data"));
            } else {
                // 整个 payload 作为 body
                body = parseObject(payload);
            }
        }

        return new EventData(topic, timestamp, deviceId, eventType, body, payload);
    }

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

    private static Map<String, Object> parseObject(JsonNode node) {
        Map<String, Object> result = new HashMap<>();
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                result.put(field.getKey(), jsonNodeToObject(field.getValue()));
            }
        }
        return result;
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
    public String getEventType() { return eventType; }
    public Map<String, Object> getBody() { return new HashMap<>(body); }
    public JsonNode getRawPayload() { return rawPayload; }

    /**
     * 获取事件体中的值
     */
    public Object get(String key) {
        return body.get(key);
    }

    /**
     * 获取事件体中的字符串值
     */
    public String getString(String key) {
        Object value = body.get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public String toString() {
        return "EventData{" +
                "deviceId='" + deviceId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", body=" + body +
                '}';
    }
}
