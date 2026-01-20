/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inteagle.sdk.api.AlarmRuleApi;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.internal.transport.Transport;
import com.inteagle.sdk.model.AlarmRule;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.AlarmRuleQuery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警规则 API 实现
 * <p>
 * REST 接口 (Nop REST API):
 * <ul>
 *   <li>POST /r/AlarmRule__list - 告警规则列表</li>
 *   <li>POST /r/AlarmRule__get - 告警规则详情</li>
 * </ul>
 */
public class AlarmRuleApiImpl implements AlarmRuleApi {

    private final Transport transport;

    public AlarmRuleApiImpl(Transport transport) {
        this.transport = transport;
    }

    @Override
    public PageResult<AlarmRule> list(AlarmRuleQuery query) throws SdkException {
        String path = "/r/AlarmRule__list";
        Map<String, Object> params = buildNopQueryParams(query);
        AlarmRuleListResponse response = transport.post(path, params, AlarmRuleListResponse.class);

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
    public AlarmRule get(String projectId, String ruleId) throws SdkException {
        String path = "/r/AlarmRule__get";
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("projectId", projectId);
        params.put("ruleId", ruleId);

        AlarmRuleResponse response = transport.post(path, params, AlarmRuleResponse.class);

        if (response.getCode() != 0) {
            throw new SdkException(response.getCode(), "API_ERROR",
                    response.getMessage() != null ? response.getMessage() : "Unknown error");
        }

        return response.data;
    }

    /**
     * 构建 Nop REST API 查询参数
     */
    private Map<String, Object> buildNopQueryParams(AlarmRuleQuery query) {
        Map<String, Object> params = new LinkedHashMap<>();

        // Nop REST API 使用 page (0-based) 和 pageSize
        params.put("page", query.getPage());
        params.put("pageSize", query.getPageSize());

        if (query.getProjectId() != null) {
            params.put("projectId", query.getProjectId());
        }
        if (query.getEntityType() != null) {
            params.put("entityType", query.getEntityType());
        }
        if (query.getEntityId() != null) {
            params.put("entityId", query.getEntityId());
        }
        if (query.getEnabled() != null) {
            params.put("enabled", query.getEnabled());
        }

        return params;
    }

    // ==================== 响应 DTO ====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AlarmRuleListResponse {
        public int code;
        public Integer status;
        public String message;
        public String msg;
        public PagedData data;

        public int getCode() {
            return status != null ? status : code;
        }

        public String getMessage() {
            return message != null ? message : msg;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class PagedData {
        public List<AlarmRule> items;
        public long total;
        public int page;
        public int pageSize;
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
    private static class AlarmRuleResponse {
        public int code;
        public Integer status;
        public String message;
        public String msg;
        public AlarmRule data;

        public int getCode() {
            return status != null ? status : code;
        }

        public String getMessage() {
            return message != null ? message : msg;
        }
    }
}
