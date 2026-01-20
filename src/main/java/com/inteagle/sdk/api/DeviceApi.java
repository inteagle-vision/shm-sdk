/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.api;

import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.Device;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.DeviceQuery;

/**
 * 设备 API 接口
 * <p>
 * 提供设备信息的查询功能。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 分页查询设备
 * PageResult<Device> devices = client.devices().list(
 *     DeviceQuery.builder()
 *         .projectId("project-001")
 *         .active(true)
 *         .pageSize(20)
 *         .build()
 * );
 *
 * // 获取单个设备
 * Device device = client.devices().get("device-001");
 * }</pre>
 */
public interface DeviceApi {

    /**
     * 分页查询设备
     *
     * @param query 查询条件
     * @return 分页结果
     * @throws SdkException 如果查询失败
     */
    PageResult<Device> list(DeviceQuery query) throws SdkException;

    /**
     * 获取单个设备
     *
     * @param deviceId 设备 ID
     * @return 设备信息
     * @throws com.inteagle.sdk.exception.NotFoundException 如果设备不存在
     * @throws SdkException                                  如果查询失败
     */
    Device get(String deviceId) throws SdkException;
}
