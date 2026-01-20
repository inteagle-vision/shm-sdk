/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt.data;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Base64;

/**
 * 图像数据模型
 * <p>
 * 图像数据包含设备上传的图片信息，支持 URL 或 Base64 编码。
 *
 * <p>使用示例:
 * <pre>{@code
 * ImageData image = ImageData.from(message);
 * if (image.hasUrl()) {
 *     String url = image.getImageUrl();
 * } else if (image.hasBase64()) {
 *     byte[] bytes = image.getImageBytes();
 * }
 * }</pre>
 */
public class ImageData {

    private final String topic;
    private final long timestamp;
    private final String deviceId;
    private final String imageUrl;
    private final String base64Data;
    private final String mimeType;
    private final String fileName;
    private final Long fileSize;
    private final JsonNode rawPayload;

    private ImageData(Builder builder) {
        this.topic = builder.topic;
        this.timestamp = builder.timestamp;
        this.deviceId = builder.deviceId;
        this.imageUrl = builder.imageUrl;
        this.base64Data = builder.base64Data;
        this.mimeType = builder.mimeType;
        this.fileName = builder.fileName;
        this.fileSize = builder.fileSize;
        this.rawPayload = builder.rawPayload;
    }

    /**
     * 从 JsonNode 解析图像数据
     */
    public static ImageData from(String topic, long timestamp, JsonNode payload) {
        Builder builder = new Builder()
                .topic(topic)
                .timestamp(timestamp)
                .deviceId(extractDeviceId(topic))
                .rawPayload(payload);

        if (payload != null) {
            // 尝试不同的字段名
            builder.imageUrl(getTextValue(payload, "imageUrl", "url", "uri"))
                    .base64Data(getTextValue(payload, "base64", "imageData", "data"))
                    .mimeType(getTextValue(payload, "mimeType", "contentType", "type"))
                    .fileName(getTextValue(payload, "fileName", "name"))
                    .fileSize(getLongValue(payload, "fileSize", "size"));
        }

        return builder.build();
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

    private static String getTextValue(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.path(key);
            if (!value.isMissingNode() && !value.isNull()) {
                return value.asText();
            }
        }
        return null;
    }

    private static Long getLongValue(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.path(key);
            if (!value.isMissingNode() && value.isNumber()) {
                return value.asLong();
            }
        }
        return null;
    }

    // ==================== Getters ====================

    public String getTopic() { return topic; }
    public long getTimestamp() { return timestamp; }
    public String getDeviceId() { return deviceId; }
    public String getImageUrl() { return imageUrl; }
    public String getBase64Data() { return base64Data; }
    public String getMimeType() { return mimeType; }
    public String getFileName() { return fileName; }
    public Long getFileSize() { return fileSize; }
    public JsonNode getRawPayload() { return rawPayload; }

    /**
     * 是否包含图片 URL
     */
    public boolean hasUrl() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    /**
     * 是否包含 Base64 数据
     */
    public boolean hasBase64() {
        return base64Data != null && !base64Data.isEmpty();
    }

    /**
     * 获取图片字节数组 (从 Base64 解码)
     */
    public byte[] getImageBytes() {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ImageData{" +
                "deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", hasUrl=" + hasUrl() +
                ", hasBase64=" + hasBase64() +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }

    // ==================== Builder ====================

    public static class Builder {
        private String topic;
        private long timestamp;
        private String deviceId;
        private String imageUrl;
        private String base64Data;
        private String mimeType;
        private String fileName;
        private Long fileSize;
        private JsonNode rawPayload;

        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }
        public Builder deviceId(String deviceId) { this.deviceId = deviceId; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder base64Data(String base64Data) { this.base64Data = base64Data; return this; }
        public Builder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public Builder rawPayload(JsonNode rawPayload) { this.rawPayload = rawPayload; return this; }
        public ImageData build() { return new ImageData(this); }
    }
}
