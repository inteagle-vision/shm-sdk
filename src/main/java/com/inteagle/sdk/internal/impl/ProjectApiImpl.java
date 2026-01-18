/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inteagle.sdk.api.ProjectApi;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.internal.transport.Transport;
import com.inteagle.sdk.model.MonitoringProject;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.ProjectQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目 API 实现
 * <p>
 * Nop RPC 接口:
 * <ul>
 *   <li>POST /r/MonitoringProject__list - 项目列表</li>
 *   <li>POST /r/MonitoringProject__get - 项目详情</li>
 * </ul>
 */
public class ProjectApiImpl implements ProjectApi {

    private static final String BIZ_MODEL = "MonitoringProject";

    private final Transport transport;

    public ProjectApiImpl(Transport transport) {
        this.transport = transport;
    }

    @Override
    public PageResult<MonitoringProject> list(ProjectQuery query) throws SdkException {
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
    public List<MonitoringProject> listAll(ProjectQuery query) throws SdkException {
        List<MonitoringProject> all = new ArrayList<>();
        int page = 0;
        boolean hasMore = true;

        while (hasMore) {
            ProjectQuery pageQuery = ProjectQuery.builder()
                    .name(query.getName())
                    .type(query.getType())
                    .active(query.getActive())
                    .region(query.getRegion())
                    .page(page, query.getPageSize())
                    .build();

            PageResult<MonitoringProject> result = list(pageQuery);
            all.addAll(result.getData());
            hasMore = result.hasMore();
            page++;
        }

        return all;
    }

    @Override
    public MonitoringProject get(String projectId) throws SdkException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("projectId", projectId);
        params.put("includeAttributes", true);

        NopResponse response = transport.call(BIZ_MODEL, "get", params, NopResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, response.code != null ? response.code : "API_ERROR",
                    response.msg != null ? response.msg : "Unknown error");
        }

        return response.data;
    }

    /**
     * 构建查询参数 (Nop RPC 格式)
     */
    private Map<String, Object> buildQueryParams(ProjectQuery query) {
        Map<String, Object> params = new LinkedHashMap<>();

        // 分页参数
        params.put("page", query.getPage());
        params.put("pageSize", query.getPageSize());

        if (query.getName() != null) {
            params.put("nameLike", "%" + query.getName() + "%");
        }
        if (query.getActive() != null) {
            params.put("status", query.getActive() ? "inProgress" : "completed");
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
        public List<MonitoringProject> items;
        public Long total;
        public Long offset;
        public Integer limit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NopResponse {
        public int status;
        public String code;
        public String msg;
        public MonitoringProject data;
    }
}
