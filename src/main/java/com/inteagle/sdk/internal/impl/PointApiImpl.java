/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inteagle.sdk.api.PointApi;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.internal.transport.Transport;
import com.inteagle.sdk.model.MonitoringPoint;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.PointQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 监测点 API 实现
 * <p>
 * REST 接口:
 * <ul>
 *   <li>GET /api/v1/projects/{projectId}/points - 项目下监测点列表</li>
 *   <li>GET /api/v1/points/{pointId} - 监测点详情</li>
 * </ul>
 */
public class PointApiImpl implements PointApi {

    private static final String POINTS_PATH = "/api/v1/points";

    private final Transport transport;

    public PointApiImpl(Transport transport) {
        this.transport = transport;
    }

    @Override
    public PageResult<MonitoringPoint> list(PointQuery query) throws SdkException {
        // 统一使用 Nop REST API: POST /r/MonitoringPoint__list
        String path = "/r/MonitoringPoint__list";
        Map<String, Object> params = buildNopQueryParams(query);
        PointListResponse response = transport.post(path, params, PointListResponse.class);

        if (response.getCode() != 0) {
            throw new SdkException(response.getCode(), "API_ERROR",
                    response.getMessage() != null ? response.getMessage() : "Unknown error");
        }

        PagedData data = response.data;
        if (data == null || data.items == null) {
            return PageResult.empty(query.getPage(), query.getPageSize());
        }

        return PageResult.of(
                data.items,
                data.total,
                data.getPage(),
                data.getPageSize()
        );
    }

    @Override
    public List<MonitoringPoint> listAll(PointQuery query) throws SdkException {
        List<MonitoringPoint> all = new ArrayList<>();
        int page = 0;
        boolean hasMore = true;

        while (hasMore) {
            PointQuery pageQuery = PointQuery.builder()
                    .projectId(query.getProjectId())
                    .name(query.getName())
                    .type(query.getType())
                    .deviceId(query.getDeviceId())
                    .active(query.getActive())
                    .page(page, query.getPageSize())
                    .build();

            PageResult<MonitoringPoint> result = list(pageQuery);
            all.addAll(result.getData());
            hasMore = result.hasMore();
            page++;
        }

        return all;
    }

    @Override
    public MonitoringPoint get(String pointId) throws SdkException {
        // 使用 Nop REST API: POST /r/MonitoringPoint__get
        String path = "/r/MonitoringPoint__get";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("pointId", pointId);
        params.put("includeAttributes", true);
        params.put("includeAlarmRules", true);

        PointResponse response = transport.post(path, params, PointResponse.class);

        if (response.getCode() != 0) {
            throw new SdkException(response.getCode(), "API_ERROR",
                    response.getMessage() != null ? response.getMessage() : "Unknown error");
        }

        return response.data;
    }

    /**
     * 构建查询参数（用于项目监测点接口）
     */
    private Map<String, Object> buildQueryParams(PointQuery query) {
        Map<String, Object> params = new LinkedHashMap<>();

        // 分页参数 (BaseQuery 默认值: page=0, pageSize=20)
        params.put("page", query.getPage());
        params.put("pageSize", query.getPageSize());

        if (query.getName() != null) {
            params.put("name", query.getName());
        }
        if (query.getType() != null) {
            params.put("type", query.getType());
        }
        if (query.getDeviceId() != null) {
            params.put("deviceId", query.getDeviceId());
        }
        if (query.getIncludeAttributes() != null) {
            params.put("includeAttributes", query.getIncludeAttributes());
        }
        if (query.getIncludeAlarmRules() != null) {
            params.put("includeAlarmRules", query.getIncludeAlarmRules());
        }

        return params;
    }

    /**
     * 构建 Nop REST API 查询参数
     */
    private Map<String, Object> buildNopQueryParams(PointQuery query) {
        Map<String, Object> params = new LinkedHashMap<>();

        // Nop REST API 使用 page (0-based) 和 pageSize
        params.put("page", query.getPage());
        params.put("pageSize", query.getPageSize());

        if (query.getProjectId() != null) {
            params.put("projectId", query.getProjectId());
        }
        if (query.getDeviceId() != null) {
            params.put("deviceId", query.getDeviceId());
        }
        if (query.getName() != null) {
            params.put("nameLike", "%" + query.getName() + "%");
        }
        if (query.getType() != null) {
            params.put("pointType", query.getType());
        }
        if (query.getIncludeAttributes() != null) {
            params.put("includeAttributes", query.getIncludeAttributes());
        }
        if (query.getIncludeAlarmRules() != null) {
            params.put("includeAlarmRules", query.getIncludeAlarmRules());
        }

        return params;
    }

    // ==================== 响应 DTO ====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PointListResponse {
        public int code;          // 自定义 API 格式
        public Integer status;    // Nop REST 格式
        public String message;
        public String msg;        // Nop 错误格式
        public PagedData data;

        public int getCode() {
            // Nop 返回 status，自定义 API 返回 code
            return status != null ? status : code;
        }

        public String getMessage() {
            return message != null ? message : msg;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagedData {
        public List<MonitoringPoint> items;
        public long total;
        public int page;
        public int pageSize;
        public boolean hasNext;
        // Nop 格式
        public Long offset;
        public Integer limit;

        public int getPage() {
            if (page > 0) return page;
            if (offset != null && limit != null && limit > 0) {
                return (int) (offset / limit);
            }
            return 0;
        }

        public int getPageSize() {
            return limit != null ? limit : pageSize;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PointResponse {
        public int code;          // 自定义 API 格式
        public Integer status;    // Nop REST 格式
        public String message;
        public String msg;        // Nop 错误格式
        public MonitoringPoint data;

        public int getCode() {
            return status != null ? status : code;
        }

        public String getMessage() {
            return message != null ? message : msg;
        }
    }
}
