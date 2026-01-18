/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.api;

import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.MonitoringPoint;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.PointQuery;

import java.util.List;

/**
 * 监测点 API 接口
 * <p>
 * 提供监测点信息的查询功能。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 分页查询监测点
 * PageResult<MonitoringPoint> points = client.points().list(
 *     PointQuery.builder()
 *         .projectId("project-001")
 *         .pageSize(20)
 *         .build()
 * );
 *
 * // 获取单个监测点
 * MonitoringPoint point = client.points().get("point-001");
 * }</pre>
 */
public interface PointApi {

    /**
     * 分页查询监测点
     *
     * @param query 查询条件
     * @return 分页结果
     * @throws SdkException 如果查询失败
     */
    PageResult<MonitoringPoint> list(PointQuery query) throws SdkException;

    /**
     * 查询所有监测点 (不分页)
     *
     * @param query 查询条件
     * @return 监测点列表
     * @throws SdkException 如果查询失败
     */
    List<MonitoringPoint> listAll(PointQuery query) throws SdkException;

    /**
     * 获取单个监测点
     *
     * @param pointId 监测点 ID
     * @return 监测点信息
     * @throws com.inteagle.sdk.exception.NotFoundException 如果监测点不存在
     * @throws SdkException                                  如果查询失败
     */
    MonitoringPoint get(String pointId) throws SdkException;
}
