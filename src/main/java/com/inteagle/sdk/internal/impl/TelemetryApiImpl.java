/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.inteagle.sdk.api.TelemetryApi;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.internal.serialization.JsonSerializer;
import com.inteagle.sdk.internal.transport.Transport;
import com.inteagle.sdk.model.TelemetryValue;
import com.inteagle.sdk.query.TelemetryQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * 遥测 API 实现
 * <p>
 * Nop RPC 接口:
 * <ul>
 *   <li>POST /r/TelemetryBizModel__list - 历史遥测查询</li>
 *   <li>POST /r/TelemetryBizModel__latest - 最新遥测批量查询</li>
 * </ul>
 */
public class TelemetryApiImpl implements TelemetryApi {

    private static final Logger log = LoggerFactory.getLogger(TelemetryApiImpl.class);
    private static final String BIZ_MODEL = "Telemetry";
    private static final int STREAM_BATCH_SIZE = 1000;

    private final Transport transport;
    private final JsonSerializer serializer;

    public TelemetryApiImpl(Transport transport, JsonSerializer serializer) {
        this.transport = transport;
        this.serializer = serializer;
    }

    @Override
    public Map<String, List<TelemetryValue>> getLatest(String entityId, String... keys) throws SdkException {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("entityId", entityId);
        request.put("keys", Arrays.asList(keys));

        TelemetryResponse response = transport.call(BIZ_MODEL, "latest", request, TelemetryResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, response.code != null ? response.code : "API_ERROR",
                    response.msg != null ? response.msg : "Unknown error");
        }

        return parseTelemetryData(response.data);
    }

    @Override
    public Map<String, List<TelemetryValue>> getLatest(TelemetryQuery query) throws SdkException {
        Map<String, Object> request = buildLatestRequest(query);
        TelemetryResponse response = transport.call(BIZ_MODEL, "latest", request, TelemetryResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, response.code != null ? response.code : "API_ERROR",
                    response.msg != null ? response.msg : "Unknown error");
        }

        return parseTelemetryData(response.data);
    }

    @Override
    public Stream<TelemetryValue> query(TelemetryQuery query) {
        return Stream.generate(new TelemetryStreamSupplier(query))
                .takeWhile(Objects::nonNull)
                .flatMap(map -> map.values().stream().flatMap(List::stream));
    }

    @Override
    public Map<String, List<TelemetryValue>> list(TelemetryQuery query) throws SdkException {
        Map<String, Object> request = buildHistoryRequest(query);
        TelemetryResponse response = transport.call(BIZ_MODEL, "list", request, TelemetryResponse.class);

        if (response.status != 0) {
            throw new SdkException(response.status, response.code != null ? response.code : "API_ERROR",
                    response.msg != null ? response.msg : "Unknown error");
        }

        return parseTelemetryData(response.data);
    }

    /**
     * 构建最新遥测请求 (Nop RPC 格式)
     */
    private Map<String, Object> buildLatestRequest(TelemetryQuery query) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("entityId", query.getEntityId());
        if (query.getKeys() != null) {
            request.put("keys", query.getKeys());
        }
        return request;
    }

    /**
     * 构建历史遥测请求 (Nop RPC 格式)
     */
    private Map<String, Object> buildHistoryRequest(TelemetryQuery query) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("entityId", query.getEntityId());
        if (query.getKeys() != null) {
            request.put("keys", query.getKeys());
        }
        if (query.getStartTs() != null) {
            request.put("startTs", query.getStartTs());
        }
        if (query.getEndTs() != null) {
            request.put("endTs", query.getEndTs());
        }
        if (query.getLimit() != null) {
            request.put("limit", query.getLimit());
        }
        return request;
    }

    /**
     * 解析遥测数据响应
     */
    private Map<String, List<TelemetryValue>> parseTelemetryData(Object data) {
        if (data == null) {
            return new LinkedHashMap<>();
        }
        // 直接序列化和解析
        String json = serializer.serialize(data);
        return parseTelemetryResponse(json);
    }

    // ==================== Nop 响应 DTO ====================

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TelemetryResponse {
        public int status;
        public String code;
        public String msg;
        public Object data;
    }

    // ==================== 内部方法 ====================

    /**
     * 解析遥测响应
     */
    private Map<String, List<TelemetryValue>> parseTelemetryResponse(String response) {
        Map<String, List<TelemetryValue>> result = new LinkedHashMap<>();

        if (response == null || response.isEmpty() || "{}".equals(response)) {
            return result;
        }

        JsonNode root = serializer.parseTree(response);
        if (!root.isObject()) {
            return result;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode values = entry.getValue();

            List<TelemetryValue> valueList = new ArrayList<>();
            if (values.isArray()) {
                for (JsonNode v : values) {
                    TelemetryValue tv = parseTelemetryValue(v);
                    if (tv != null) {
                        valueList.add(tv);
                    }
                }
            }
            result.put(key, valueList);
        }

        return result;
    }

    /**
     * 解析单个遥测值
     */
    private TelemetryValue parseTelemetryValue(JsonNode node) {
        if (!node.isObject()) return null;

        long ts = node.has("ts") ? node.get("ts").asLong() : 0;

        TelemetryValue.TelemetryValueBuilder builder = TelemetryValue.builder().ts(ts);

        if (node.has("value")) {
            JsonNode valueNode = node.get("value");
            parseValue(builder, valueNode);
        }

        // 支持直接的类型字段
        if (node.has("boolV") && !node.get("boolV").isNull()) {
            builder.boolValue(node.get("boolV").asBoolean());
        }
        if (node.has("strV") && !node.get("strV").isNull()) {
            builder.strValue(node.get("strV").asText());
        }
        if (node.has("longV") && !node.get("longV").isNull()) {
            builder.longValue(node.get("longV").asLong());
        }
        if (node.has("dblV") && !node.get("dblV").isNull()) {
            builder.dblValue(node.get("dblV").asDouble());
        }
        if (node.has("jsonV") && !node.get("jsonV").isNull()) {
            builder.jsonValue(node.get("jsonV").asText());
        }

        return builder.build();
    }

    /**
     * 解析值字段
     */
    private void parseValue(TelemetryValue.TelemetryValueBuilder builder, JsonNode valueNode) {
        if (valueNode.isBoolean()) {
            builder.boolValue(valueNode.asBoolean());
            builder.value(valueNode.asBoolean());
        } else if (valueNode.isLong() || valueNode.isInt()) {
            builder.longValue(valueNode.asLong());
            builder.value(valueNode.asLong());
        } else if (valueNode.isDouble() || valueNode.isFloat()) {
            builder.dblValue(valueNode.asDouble());
            builder.value(valueNode.asDouble());
        } else if (valueNode.isTextual()) {
            builder.strValue(valueNode.asText());
            builder.value(valueNode.asText());
        } else if (valueNode.isObject() || valueNode.isArray()) {
            String json = valueNode.toString();
            builder.jsonValue(json);
            builder.value(json);
        }
    }

    // ==================== 流式查询支持 ====================

    /**
     * 流式查询 Supplier
     */
    private class TelemetryStreamSupplier implements java.util.function.Supplier<Map<String, List<TelemetryValue>>> {
        private final TelemetryQuery baseQuery;
        private Long currentEndTs;
        private boolean hasMore = true;

        TelemetryStreamSupplier(TelemetryQuery query) {
            this.baseQuery = query;
            this.currentEndTs = query.getEndTs() != null ? query.getEndTs() : System.currentTimeMillis();
        }

        @Override
        public Map<String, List<TelemetryValue>> get() {
            if (!hasMore) return null;

            try {
                Long startTs = baseQuery.getStartTs();

                TelemetryQuery batchQuery = TelemetryQuery.builder()
                        .entityId(baseQuery.getEntityId())
                        .keys(baseQuery.getKeys())
                        .timeRange(startTs != null ? startTs : 0, currentEndTs)
                        .limit(STREAM_BATCH_SIZE)
                        .orderDesc(true)
                        .build();

                Map<String, List<TelemetryValue>> result = list(batchQuery);

                int totalCount = result.values().stream().mapToInt(List::size).sum();
                if (result.isEmpty() || totalCount < STREAM_BATCH_SIZE) {
                    hasMore = false;
                } else {
                    // 更新下一批查询的结束时间
                    long minTs = result.values().stream()
                            .flatMap(List::stream)
                            .mapToLong(TelemetryValue::getTs)
                            .min()
                            .orElse(0);

                    if (minTs <= (startTs != null ? startTs : 0)) {
                        hasMore = false;
                    } else {
                        currentEndTs = minTs - 1;
                    }
                }

                return result;

            } catch (SdkException e) {
                log.error("Failed to fetch telemetry batch", e);
                hasMore = false;
                return null;
            }
        }
    }
}
