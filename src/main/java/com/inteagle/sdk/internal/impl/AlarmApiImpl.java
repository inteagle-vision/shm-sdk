/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inteagle.sdk.api.AlarmApi;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.internal.transport.Transport;
import com.inteagle.sdk.model.Alarm;
import com.inteagle.sdk.model.AlarmStatistics;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.AlarmQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警 API 实现
 * <p>
 * Nop RPC 接口:
 * <ul>
 *   <li>POST /r/AlarmBizModel__list - 告警列表</li>
 *   <li>POST /r/AlarmBizModel__get - 告警详情</li>
 *   <li>POST /r/AlarmBizModel__statistics - 告警统计</li>
 * </ul>
 */
public class AlarmApiImpl implements AlarmApi {

    private static final String BIZ_MODEL = "Alarm";

    private final Transport transport;

    public AlarmApiImpl(Transport transport) {
        this.transport = transport;
    }

    @Override
    public PageResult<Alarm> list(AlarmQuery query) throws SdkException {
        Map<String, Object> params = buildQueryParams(query);
        NopPageResponse response = transport.call(BIZ_MODEL, "list", params, NopPageResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, response.code != null ? response.code : "API_ERROR",
                    response.msg != null ? response.msg : "Unknown error");
        }

        NopPageData data = response.data;
        if (data == null || data.items == null) {
            return PageResult.empty(query.getPage(), query.getPageSize());
        }

        int page = data.offset != null ? (int) (data.offset / query.getPageSize()) : query.getPage();
        return PageResult.of(
                data.items,
                data.total != null ? data.total : 0,
                page,
                query.getPageSize()
        );
    }

    @Override
    public List<Alarm> listAll(AlarmQuery query) throws SdkException {
        List<Alarm> all = new ArrayList<>();
        int page = 0;
        boolean hasMore = true;

        while (hasMore) {
            AlarmQuery pageQuery = AlarmQuery.builder()
                    .projectId(query.getProjectId())
                    .deviceId(query.getDeviceId())
                    .severity(query.getSeverity())
                    .type(query.getType())
                    .active(query.getActive())
                    .acknowledged(query.getAcknowledged())
                    .page(page, query.getPageSize())
                    .build();

            PageResult<Alarm> result = list(pageQuery);
            all.addAll(result.getData());
            hasMore = result.hasMore();
            page++;
        }

        return all;
    }

    @Override
    public Alarm get(String alarmId) throws SdkException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("alarmId", alarmId);

        NopResponse response = transport.call(BIZ_MODEL, "get", params, NopResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, response.code != null ? response.code : "API_ERROR",
                    response.msg != null ? response.msg : "Unknown error");
        }

        return response.data;
    }

    @Override
    public AlarmStatistics getStatistics() throws SdkException {
        Map<String, Object> params = new LinkedHashMap<>();
        NopStatsResponse response = transport.call(BIZ_MODEL, "statistics", params, NopStatsResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, response.code != null ? response.code : "API_ERROR",
                    response.msg != null ? response.msg : "Unknown error");
        }

        return response.data;
    }

    @Override
    public List<Alarm> getActiveUnacknowledged() throws SdkException {
        return listAll(AlarmQuery.activeUnacknowledged());
    }

    @Override
    public List<Alarm> getByDevice(String deviceId) throws SdkException {
        return listAll(AlarmQuery.ofDevice(deviceId));
    }

    /**
     * 构建查询参数 (Nop RPC 格式)
     */
    private Map<String, Object> buildQueryParams(AlarmQuery query) {
        Map<String, Object> params = new LinkedHashMap<>();

        // 分页参数
        params.put("page", query.getPage());
        params.put("pageSize", query.getPageSize());

        if (query.getProjectId() != null) {
            params.put("projectId", query.getProjectId());
        }
        if (query.getDeviceId() != null) {
            params.put("originatorId", query.getDeviceId());
        }
        if (query.getSeverity() != null) {
            params.put("severity", query.getSeverity());
        }
        if (query.getType() != null) {
            params.put("type", query.getType());
        }
        if (query.getActive() != null) {
            // active=true 表示未清除的告警
            params.put("cleared", !query.getActive());
        }
        if (query.getAcknowledged() != null) {
            params.put("acknowledged", query.getAcknowledged());
        }
        if (query.getStartTs() != null) {
            params.put("startTs", query.getStartTs());
        }
        if (query.getEndTs() != null) {
            params.put("endTs", query.getEndTs());
        }

        return params;
    }

    // ==================== Nop 响应 DTO ====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NopPageResponse {
        public int status;
        public String code;
        public String msg;
        public NopPageData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NopPageData {
        public List<Alarm> items;
        public Long total;
        public Long offset;
        public Integer limit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NopResponse {
        public int status;
        public String code;
        public String msg;
        public Alarm data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NopStatsResponse {
        public int status;
        public String code;
        public String msg;
        public AlarmStatistics data;
    }
}
