/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inteagle.sdk.api.DeviceApi;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.internal.transport.Transport;
import com.inteagle.sdk.model.Device;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.DeviceQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备 API 实现
 * <p>
 * REST 接口 (Nop Platform):
 * <ul>
 *   <li>POST /r/Device__list - 设备列表</li>
 *   <li>POST /r/Device__get - 设备详情</li>
 * </ul>
 */
public class DeviceApiImpl implements DeviceApi {

    private static final String LIST_PATH = "/r/Device__list";
    private static final String GET_PATH = "/r/Device__get";

    private final Transport transport;

    public DeviceApiImpl(Transport transport) {
        this.transport = transport;
    }

    @Override
    public PageResult<Device> list(DeviceQuery query) throws SdkException {
        Map<String, Object> params = buildQueryParams(query);
        params.put("includeAttributes", true);

        DeviceListResponse response = transport.post(LIST_PATH, params, DeviceListResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, "API_ERROR",
                    response.message != null ? response.message : "Unknown error");
        }

        PagedData data = response.data;
        if (data == null || data.items == null) {
            return PageResult.empty(query.getPage(), query.getPageSize());
        }

        return PageResult.of(
                data.items,
                data.total,
                data.page != null ? data.page : 0,
                data.limit != null ? data.limit : query.getPageSize()
        );
    }

    @Override
    public List<Device> listAll(DeviceQuery query) throws SdkException {
        List<Device> all = new ArrayList<>();
        int page = 0;
        boolean hasMore = true;

        while (hasMore) {
            DeviceQuery pageQuery = DeviceQuery.builder()
                    .projectId(query.getProjectId())
                    .name(query.getName())
                    .type(query.getType())
                    .active(query.getActive())
                    .label(query.getLabel())
                    .page(page, query.getPageSize())
                    .build();

            PageResult<Device> result = list(pageQuery);
            all.addAll(result.getData());
            hasMore = result.hasMore();
            page++;
        }

        return all;
    }

    @Override
    public Device get(String deviceId) throws SdkException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("deviceId", deviceId);
        params.put("includeAttributes", true);

        DeviceResponse response = transport.post(GET_PATH, params, DeviceResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, "API_ERROR",
                    response.message != null ? response.message : "Unknown error");
        }

        return response.data;
    }

    /**
     * 构建查询参数
     */
    private Map<String, Object> buildQueryParams(DeviceQuery query) {
        Map<String, Object> params = new LinkedHashMap<>();

        // 分页参数 (BaseQuery 默认值: page=0, pageSize=20)
        params.put("page", query.getPage());
        params.put("pageSize", query.getPageSize());

        // 过滤条件
        if (query.getProjectId() != null) {
            params.put("projectId", query.getProjectId());
        }
        if (query.getName() != null) {
            params.put("nameLike", "%" + query.getName() + "%");
        }
        if (query.getType() != null) {
            params.put("type", query.getType());
        }
        if (query.getLabel() != null) {
            params.put("nameLike", "%" + query.getLabel() + "%");
        }

        // 包含选项
        if (query.getIncludeMonitoringPoints() != null) {
            params.put("includeMonitoringPoints", query.getIncludeMonitoringPoints());
        }
        if (query.getIncludeAlarmRules() != null) {
            params.put("includeAlarmRules", query.getIncludeAlarmRules());
        }

        return params;
    }

    // ==================== 响应 DTO ====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeviceListResponse {
        public int status;
        public String message;
        public PagedData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PagedData {
        public List<Device> items;
        public long total;
        public Integer page;
        public Integer limit;
        public Long offset;
        public boolean hasNext;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class DeviceResponse {
        public int status;
        public String message;
        public Device data;
    }
}
