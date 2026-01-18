/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.api;

import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.TelemetryValue;
import com.inteagle.sdk.query.TelemetryQuery;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 遥测数据 API 接口
 * <p>
 * 提供时序数据的查询功能，包括：
 * <ul>
 *   <li>获取最新遥测值</li>
 *   <li>查询历史遥测数据</li>
 *   <li>流式查询大量数据</li>
 * </ul>
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 获取最新温度和湿度
 * Map<String, List<TelemetryValue>> latest = client.telemetry()
 *     .getLatest("device-001", "temperature", "humidity");
 *
 * // 查询最近 1 小时的数据
 * Stream<TelemetryValue> data = client.telemetry().query(
 *     TelemetryQuery.builder()
 *         .entityId("device-001")
 *         .keys("temperature")
 *         .lastHours(1)
 *         .build()
 * );
 * }</pre>
 */
public interface TelemetryApi {

    /**
     * 获取最新遥测值
     * <p>
     * 从 ts_kv_latest 表查询，性能更优
     *
     * @param entityId 实体 ID (设备或资产)
     * @param keys     遥测键名
     * @return 最新遥测值，按键名分组
     * @throws SdkException 如果查询失败
     */
    Map<String, List<TelemetryValue>> getLatest(String entityId, String... keys) throws SdkException;

    /**
     * 使用查询条件获取最新遥测值
     *
     * @param query 查询条件
     * @return 最新遥测值
     * @throws SdkException 如果查询失败
     */
    Map<String, List<TelemetryValue>> getLatest(TelemetryQuery query) throws SdkException;

    /**
     * 流式查询遥测数据
     * <p>
     * 使用游标分页，适合大数据量场景，避免内存溢出
     *
     * @param query 查询条件
     * @return 遥测值流
     */
    Stream<TelemetryValue> query(TelemetryQuery query);

    /**
     * 批量查询遥测数据
     * <p>
     * 单次查询，适合小数据量场景
     *
     * @param query 查询条件
     * @return 遥测值，按键名分组
     * @throws SdkException 如果查询失败
     */
    Map<String, List<TelemetryValue>> list(TelemetryQuery query) throws SdkException;
}
