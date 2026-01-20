/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.api;

import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.Alarm;
import com.inteagle.sdk.model.AlarmStatistics;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.AlarmQuery;

/**
 * 告警 API 接口
 * <p>
 * 提供告警信息的查询功能。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 查询活跃告警
 * PageResult<Alarm> alarms = client.alarms().list(
 *     AlarmQuery.builder()
 *         .active(true)
 *         .severity("CRITICAL")
 *         .pageSize(20)
 *         .build()
 * );
 *
 * // 获取告警统计
 * AlarmStatistics stats = client.alarms().getStatistics();
 * }</pre>
 */
public interface AlarmApi {

    /**
     * 分页查询告警
     *
     * @param query 查询条件
     * @return 分页结果
     * @throws SdkException 如果查询失败
     */
    PageResult<Alarm> list(AlarmQuery query) throws SdkException;

    /**
     * 获取单个告警
     *
     * @param alarmId 告警 ID
     * @return 告警信息
     * @throws com.inteagle.sdk.exception.NotFoundException 如果告警不存在
     * @throws SdkException                                  如果查询失败
     */
    Alarm get(String alarmId) throws SdkException;

    /**
     * 获取告警统计
     *
     * @return 告警统计信息
     * @throws SdkException 如果查询失败
     */
    AlarmStatistics getStatistics() throws SdkException;
}
