package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Stream;

/**
 * 遥测查询结果
 * <p>
 * 包装按 key 分组的遥测数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelemetryResult {

    /**
     * 实体 ID
     */
    private String entityId;

    /**
     * 遥测数据 - key -> values
     */
    @Builder.Default
    private Map<String, List<TelemetryValue>> values = new HashMap<>();

    /**
     * 获取指定 key 的所有值
     *
     * @param key 遥测键名
     * @return 值列表，如果不存在返回空列表
     */
    public List<TelemetryValue> get(String key) {
        return values.getOrDefault(key, Collections.emptyList());
    }

    /**
     * 获取指定 key 的最新值
     *
     * @param key 遥测键名
     * @return 最新值，如果不存在返回 null
     */
    public TelemetryValue getLatest(String key) {
        List<TelemetryValue> list = get(key);
        if (list.isEmpty()) return null;

        // 找到时间戳最大的值
        return list.stream()
                .max(Comparator.comparingLong(TelemetryValue::getTs))
                .orElse(null);
    }

    /**
     * 获取指定 key 的第一个值
     *
     * @param key 遥测键名
     * @return 第一个值，如果不存在返回 null
     */
    public TelemetryValue getFirst(String key) {
        List<TelemetryValue> list = get(key);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 获取所有 key
     */
    public Set<String> getKeys() {
        return values.keySet();
    }

    /**
     * 检查是否包含指定 key
     */
    public boolean hasKey(String key) {
        return values.containsKey(key) && !values.get(key).isEmpty();
    }

    /**
     * 获取指定 key 的值流
     *
     * @param key 遥测键名
     * @return 值流
     */
    public Stream<TelemetryValue> stream(String key) {
        return get(key).stream();
    }

    /**
     * 获取所有值的流 (扁平化)
     */
    public Stream<TelemetryValue> streamAll() {
        return values.values().stream().flatMap(List::stream);
    }

    /**
     * 获取指定 key 的数值流
     *
     * @param key 遥测键名
     * @return 数值流 (double)
     */
    public Stream<Double> streamDoubles(String key) {
        return stream(key)
                .map(TelemetryValue::getAsDouble)
                .filter(d -> !Double.isNaN(d));
    }

    /**
     * 计算指定 key 的平均值
     */
    public OptionalDouble average(String key) {
        return streamDoubles(key).mapToDouble(Double::doubleValue).average();
    }

    /**
     * 计算指定 key 的最大值
     */
    public OptionalDouble max(String key) {
        return streamDoubles(key).mapToDouble(Double::doubleValue).max();
    }

    /**
     * 计算指定 key 的最小值
     */
    public OptionalDouble min(String key) {
        return streamDoubles(key).mapToDouble(Double::doubleValue).min();
    }

    /**
     * 计算指定 key 的总和
     */
    public double sum(String key) {
        return streamDoubles(key).mapToDouble(Double::doubleValue).sum();
    }

    /**
     * 获取指定 key 的数据点数量
     */
    public int count(String key) {
        return get(key).size();
    }

    /**
     * 添加遥测值
     */
    public void addValue(String key, TelemetryValue value) {
        values.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    /**
     * 合并另一个结果
     */
    public void merge(TelemetryResult other) {
        if (other == null) return;
        for (Map.Entry<String, List<TelemetryValue>> entry : other.values.entrySet()) {
            values.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
        }
    }

    /**
     * 检查结果是否为空
     */
    public boolean isEmpty() {
        return values.isEmpty() || values.values().stream().allMatch(List::isEmpty);
    }

    /**
     * 获取总数据点数量
     */
    public int totalCount() {
        return values.values().stream().mapToInt(List::size).sum();
    }
}
