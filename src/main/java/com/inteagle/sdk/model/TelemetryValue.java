package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 遥测值 - 单个时间点的遥测数据
 * <p>
 * 支持多种数据类型：布尔、字符串、长整型、双精度、JSON
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelemetryValue {

    /**
     * 时间戳 (毫秒)
     */
    private long ts;

    /**
     * 通用值 - 自动类型推断
     */
    private Object value;

    /**
     * 字符串值
     */
    private String strValue;

    /**
     * 双精度值
     */
    private Double dblValue;

    /**
     * 长整型值
     */
    private Long longValue;

    /**
     * 布尔值
     */
    private Boolean boolValue;

    /**
     * JSON 值
     */
    private String jsonValue;

    /**
     * 从原始值构造
     */
    public static TelemetryValue of(long ts, Object value) {
        TelemetryValue tv = new TelemetryValue();
        tv.ts = ts;
        tv.value = value;

        if (value instanceof Boolean) {
            tv.boolValue = (Boolean) value;
        } else if (value instanceof String) {
            tv.strValue = (String) value;
        } else if (value instanceof Long) {
            tv.longValue = (Long) value;
        } else if (value instanceof Integer) {
            tv.longValue = ((Integer) value).longValue();
        } else if (value instanceof Double) {
            tv.dblValue = (Double) value;
        } else if (value instanceof Float) {
            tv.dblValue = ((Float) value).doubleValue();
        } else if (value instanceof Number) {
            tv.dblValue = ((Number) value).doubleValue();
        }

        return tv;
    }

    /**
     * 获取值 - 优先返回非空的具体类型值
     */
    public Object getValue() {
        if (value != null) return value;
        if (boolValue != null) return boolValue;
        if (longValue != null) return longValue;
        if (dblValue != null) return dblValue;
        if (strValue != null) return strValue;
        if (jsonValue != null) return jsonValue;
        return null;
    }

    /**
     * 获取数值 - 返回 double 类型
     *
     * @return 数值，如果不是数值类型返回 NaN
     */
    public double getAsDouble() {
        if (dblValue != null) return dblValue;
        if (longValue != null) return longValue.doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.NaN;
    }

    /**
     * 获取整数值 - 返回 long 类型
     *
     * @return 整数值，如果不是整数类型返回 0
     */
    public long getAsLong() {
        if (longValue != null) return longValue;
        if (dblValue != null) return dblValue.longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }

    /**
     * 获取字符串值
     */
    public String getAsString() {
        if (strValue != null) return strValue;
        if (jsonValue != null) return jsonValue;
        Object v = getValue();
        return v != null ? v.toString() : null;
    }

    /**
     * 获取布尔值
     */
    public boolean getAsBoolean() {
        if (boolValue != null) return boolValue;
        if (value instanceof Boolean) return (Boolean) value;
        return false;
    }
}
