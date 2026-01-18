/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import java.util.*;

/**
 * 遥测数据查询
 * <p>
 * 用于构建时序数据查询请求。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 查询最近 1 小时的数据
 * TelemetryQuery query = TelemetryQuery.builder()
 *     .entityId("device-001")
 *     .keys("temperature", "humidity")
 *     .lastHours(1)
 *     .build();
 *
 * // 查询聚合数据
 * TelemetryQuery aggQuery = TelemetryQuery.builder()
 *     .entityId("device-001")
 *     .keys("temperature")
 *     .lastDays(7)
 *     .avgPerHour()
 *     .build();
 * }</pre>
 */
public final class TelemetryQuery {

    private final String entityId;
    private final List<String> keys;
    private final Long startTs;
    private final Long endTs;
    private final Integer limit;
    private final Boolean orderDesc;
    private final Aggregation aggregation;
    private final Long intervalMs;

    private TelemetryQuery(Builder builder) {
        this.entityId = builder.entityId;
        this.keys = builder.keys != null ? new ArrayList<>(builder.keys) : Collections.emptyList();
        this.startTs = builder.startTs;
        this.endTs = builder.endTs;
        this.limit = builder.limit;
        this.orderDesc = builder.orderDesc;
        this.aggregation = builder.aggregation;
        this.intervalMs = builder.intervalMs;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 快捷方法 - 查询最新值
     *
     * @param entityId 实体 ID
     * @param keys     遥测键名
     * @return 查询对象
     */
    public static TelemetryQuery latest(String entityId, String... keys) {
        return builder()
                .entityId(entityId)
                .keys(keys)
                .limit(1)
                .orderDesc(true)
                .build();
    }

    /**
     * 快捷方法 - 查询时间范围内的数据
     *
     * @param entityId 实体 ID
     * @param startTs  开始时间戳 (毫秒)
     * @param endTs    结束时间戳 (毫秒)
     * @param keys     遥测键名
     * @return 查询对象
     */
    public static TelemetryQuery range(String entityId, long startTs, long endTs, String... keys) {
        return builder()
                .entityId(entityId)
                .keys(keys)
                .timeRange(startTs, endTs)
                .build();
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为 Map (用于 API 调用)
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("entityId", entityId);
        if (!keys.isEmpty()) {
            map.put("keys", keys);
        }
        if (startTs != null) {
            map.put("startTs", startTs);
        }
        if (endTs != null) {
            map.put("endTs", endTs);
        }
        if (limit != null) {
            map.put("limit", limit);
        }
        if (orderDesc != null) {
            map.put("orderDesc", orderDesc);
        }
        if (aggregation != null && aggregation != Aggregation.NONE) {
            map.put("aggregation", aggregation.name());
            if (intervalMs != null) {
                map.put("intervalMs", intervalMs);
            }
        }
        return map;
    }

    // ==================== Getters ====================

    public String getEntityId() {
        return entityId;
    }

    public List<String> getKeys() {
        return keys;
    }

    public Long getStartTs() {
        return startTs;
    }

    public Long getEndTs() {
        return endTs;
    }

    public Integer getLimit() {
        return limit;
    }

    public Boolean getOrderDesc() {
        return orderDesc;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public Long getIntervalMs() {
        return intervalMs;
    }

    // ==================== 聚合类型 ====================

    /**
     * 聚合类型
     */
    public enum Aggregation {
        /** 不聚合，返回原始数据 */
        NONE,
        /** 平均值 */
        AVG,
        /** 最小值 */
        MIN,
        /** 最大值 */
        MAX,
        /** 总和 */
        SUM,
        /** 计数 */
        COUNT
    }

    // ==================== Builder ====================

    /**
     * 查询构建器
     */
    public static final class Builder {
        private String entityId;
        private List<String> keys;
        private Long startTs;
        private Long endTs;
        private Integer limit;
        private Boolean orderDesc;
        private Aggregation aggregation;
        private Long intervalMs;

        private Builder() {
        }

        /**
         * 设置实体 ID (设备或资产)
         */
        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        /**
         * 设置遥测键名列表
         */
        public Builder keys(String... keys) {
            this.keys = Arrays.asList(keys);
            return this;
        }

        /**
         * 设置遥测键名列表
         */
        public Builder keys(List<String> keys) {
            this.keys = new ArrayList<>(keys);
            return this;
        }

        /**
         * 设置时间范围
         *
         * @param startTs 开始时间戳 (毫秒)
         * @param endTs   结束时间戳 (毫秒)
         */
        public Builder timeRange(long startTs, long endTs) {
            this.startTs = startTs;
            this.endTs = endTs;
            return this;
        }

        /**
         * 查询最近 N 分钟的数据
         */
        public Builder lastMinutes(int minutes) {
            long now = System.currentTimeMillis();
            this.endTs = now;
            this.startTs = now - minutes * 60L * 1000L;
            return this;
        }

        /**
         * 查询最近 N 小时的数据
         */
        public Builder lastHours(int hours) {
            long now = System.currentTimeMillis();
            this.endTs = now;
            this.startTs = now - hours * 60L * 60L * 1000L;
            return this;
        }

        /**
         * 查询最近 N 天的数据
         */
        public Builder lastDays(int days) {
            long now = System.currentTimeMillis();
            this.endTs = now;
            this.startTs = now - days * 24L * 60L * 60L * 1000L;
            return this;
        }

        /**
         * 设置每个 key 的最大返回条数
         */
        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        /**
         * 设置时间排序方向
         *
         * @param orderDesc true = 降序 (最新的在前)
         */
        public Builder orderDesc(boolean orderDesc) {
            this.orderDesc = orderDesc;
            return this;
        }

        /**
         * 设置聚合函数
         *
         * @param aggregation 聚合类型
         * @param intervalMs  聚合时间间隔 (毫秒)
         */
        public Builder aggregate(Aggregation aggregation, long intervalMs) {
            this.aggregation = aggregation;
            this.intervalMs = intervalMs;
            return this;
        }

        /**
         * 快捷方法 - 每分钟平均值
         */
        public Builder avgPerMinute() {
            return aggregate(Aggregation.AVG, 60 * 1000L);
        }

        /**
         * 快捷方法 - 每5分钟平均值
         */
        public Builder avgPer5Minutes() {
            return aggregate(Aggregation.AVG, 5 * 60 * 1000L);
        }

        /**
         * 快捷方法 - 每小时平均值
         */
        public Builder avgPerHour() {
            return aggregate(Aggregation.AVG, 60 * 60 * 1000L);
        }

        /**
         * 快捷方法 - 每天平均值
         */
        public Builder avgPerDay() {
            return aggregate(Aggregation.AVG, 24 * 60 * 60 * 1000L);
        }

        /**
         * 构建查询对象
         *
         * @throws IllegalArgumentException 如果参数无效
         */
        public TelemetryQuery build() {
            if (entityId == null || entityId.isBlank()) {
                throw new IllegalArgumentException("entityId is required");
            }
            if (keys == null || keys.isEmpty()) {
                throw new IllegalArgumentException("keys is required");
            }
            return new TelemetryQuery(this);
        }
    }
}
