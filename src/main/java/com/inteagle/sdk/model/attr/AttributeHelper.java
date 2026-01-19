/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model.attr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 属性访问工具类
 * <p>
 * 提供类型安全的属性访问方法，用于 Device、MonitoringProject、MonitoringPoint 等实体
 */
public final class AttributeHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AttributeHelper() {
        // 工具类，禁止实例化
    }

    /**
     * 获取指定类型的属性值
     *
     * @param attributes 属性 Map
     * @param key        属性键名
     * @param type       目标类型
     * @param <T>        返回类型
     * @return 转换后的属性值，如果不存在或转换失败返回 null
     */
    public static <T> T getAttribute(Map<String, Object> attributes, String key, Class<T> type) {
        if (attributes == null || !attributes.containsKey(key)) {
            return null;
        }
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        try {
            if (type.isInstance(value)) {
                return type.cast(value);
            }
            return MAPPER.convertValue(value, type);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取复杂类型的属性值（如 List、泛型等）
     *
     * @param attributes 属性 Map
     * @param key        属性键名
     * @param typeRef    类型引用
     * @param <T>        返回类型
     * @return 转换后的属性值，如果不存在或转换失败返回 null
     */
    public static <T> T getAttribute(Map<String, Object> attributes, String key, TypeReference<T> typeRef) {
        if (attributes == null || !attributes.containsKey(key)) {
            return null;
        }
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        try {
            // 如果是 JSON 字符串，先解析
            if (value instanceof String) {
                String str = ((String) value).trim();
                if (str.startsWith("[") || str.startsWith("{")) {
                    return MAPPER.readValue(str, typeRef);
                }
            }
            return MAPPER.convertValue(value, typeRef);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取字符串属性
     */
    public static String getString(Map<String, Object> attributes, String key) {
        return getAttribute(attributes, key, String.class);
    }

    /**
     * 获取整数属性
     */
    public static Integer getInt(Map<String, Object> attributes, String key) {
        return getAttribute(attributes, key, Integer.class);
    }

    /**
     * 获取长整数属性
     */
    public static Long getLong(Map<String, Object> attributes, String key) {
        return getAttribute(attributes, key, Long.class);
    }

    /**
     * 获取浮点数属性
     */
    public static Double getDouble(Map<String, Object> attributes, String key) {
        return getAttribute(attributes, key, Double.class);
    }

    /**
     * 获取布尔属性
     */
    public static Boolean getBool(Map<String, Object> attributes, String key) {
        return getAttribute(attributes, key, Boolean.class);
    }

    /**
     * 检查是否存在指定属性
     */
    public static boolean hasAttribute(Map<String, Object> attributes, String key) {
        return attributes != null && attributes.containsKey(key);
    }
}
