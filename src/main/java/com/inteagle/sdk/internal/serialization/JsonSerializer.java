/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON 序列化器实现
 * <p>
 * 使用 Jackson 进行 JSON 序列化/反序列化。
 */
public class JsonSerializer implements Serializer {

    private static final String FORMAT = "JSON";

    private final ObjectMapper objectMapper;

    /**
     * 创建 JSON 序列化器 (默认配置)
     */
    public JsonSerializer() {
        this.objectMapper = createDefaultObjectMapper();
    }

    /**
     * 创建 JSON 序列化器 (自定义 ObjectMapper)
     *
     * @param objectMapper 自定义的 ObjectMapper
     */
    public JsonSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object to JSON", e);
        }
    }

    @Override
    public byte[] serializeToBytes(Object object) {
        if (object == null) {
            return "null".getBytes();
        }
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object to JSON bytes", e);
        }
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) {
        if (data == null || data.isEmpty() || "null".equals(data)) {
            return null;
        }
        try {
            return objectMapper.readValue(data, type);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to deserialize JSON to " + type.getName(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(data, type);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize JSON bytes to " + type.getName(), e);
        }
    }

    @Override
    public <T> List<T> deserializeList(String data, Class<T> elementType) {
        if (data == null || data.isEmpty() || "null".equals(data)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(data,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to deserialize JSON to List<" + elementType.getName() + ">", e);
        }
    }

    @Override
    public <K, V> Map<K, V> deserializeMap(String data, Class<K> keyType, Class<V> valueType) {
        if (data == null || data.isEmpty() || "null".equals(data)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(data,
                    objectMapper.getTypeFactory().constructMapType(Map.class, keyType, valueType));
        } catch (JsonProcessingException e) {
            throw new SerializationException(
                    "Failed to deserialize JSON to Map<" + keyType.getName() + ", " + valueType.getName() + ">", e);
        }
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    /**
     * 解析 JSON 为 JsonNode (用于复杂响应解析)
     *
     * @param data JSON 字符串
     * @return JsonNode
     */
    public JsonNode parseTree(String data) {
        if (data == null || data.isEmpty()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to parse JSON tree", e);
        }
    }

    /**
     * 获取底层 ObjectMapper
     *
     * @return ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 创建默认配置的 ObjectMapper
     */
    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 不序列化 null 值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 空对象不报错
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    /**
     * 序列化异常
     */
    public static class SerializationException extends RuntimeException {
        public SerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
