/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model.attr;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * 常用属性类型定义
 * <p>
 * 提供预定义的 TypeReference 常量，用于 Device.getAttribute() 方法
 * <p>
 * 使用示例:
 * <pre>
 * List&lt;Target&gt; targets = device.getAttribute("targets", AttrTypes.TARGET_LIST);
 * </pre>
 */
public final class AttrTypes {

    private AttrTypes() {
        // 工具类，禁止实例化
    }

    // ==================== 视觉位移计相关 ====================

    /**
     * 目标列表类型 - List&lt;Target&gt;
     */
    public static final TypeReference<List<Target>> TARGET_LIST = new TypeReference<>() {};

    /**
     * 单个目标类型 - Target
     */
    public static final TypeReference<Target> TARGET = new TypeReference<>() {};

    /**
     * ROI 区域类型 - Roi
     */
    public static final TypeReference<Roi> ROI = new TypeReference<>() {};

    /**
     * 参考点类型 - RefPoint
     */
    public static final TypeReference<RefPoint> REF_POINT = new TypeReference<>() {};

    // ==================== 通用类型 ====================

    /**
     * 字符串列表类型 - List&lt;String&gt;
     */
    public static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    /**
     * 整数列表类型 - List&lt;Integer&gt;
     */
    public static final TypeReference<List<Integer>> INT_LIST = new TypeReference<>() {};

    /**
     * 浮点数列表类型 - List&lt;Double&gt;
     */
    public static final TypeReference<List<Double>> DOUBLE_LIST = new TypeReference<>() {};

    /**
     * 通用 Map 类型 - Map&lt;String, Object&gt;
     */
    public static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};

    /**
     * 通用 List&lt;Map&gt; 类型
     */
    public static final TypeReference<List<Map<String, Object>>> MAP_LIST = new TypeReference<>() {};
}
