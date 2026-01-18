/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import java.util.Map;

/**
 * 告警查询
 * <p>
 * 用于构建告警列表查询请求。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 查询活跃的严重告警
 * AlarmQuery query = AlarmQuery.builder()
 *     .active(true)
 *     .severity("CRITICAL")
 *     .pageSize(20)
 *     .orderByCreatedAt(true)
 *     .build();
 *
 * // 快捷方法
 * AlarmQuery query = AlarmQuery.active();
 * }</pre>
 */
public final class AlarmQuery extends BaseQuery<AlarmQuery> {

    private String projectId;
    private String deviceId;
    private String severity;
    private String type;
    private Boolean active;
    private Boolean acknowledged;
    private Long startTs;
    private Long endTs;

    private AlarmQuery() {
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建 Builder
     */
    public static AlarmQuery builder() {
        return new AlarmQuery();
    }

    /**
     * 快捷方法 - 查询活跃告警
     */
    public static AlarmQuery active() {
        return builder().active(true);
    }

    /**
     * 快捷方法 - 查询活跃未确认告警
     */
    public static AlarmQuery activeUnacknowledged() {
        return builder().active(true).acknowledged(false);
    }

    /**
     * 快捷方法 - 查询某设备的告警
     *
     * @param deviceId 设备 ID
     */
    public static AlarmQuery ofDevice(String deviceId) {
        return builder().deviceId(deviceId);
    }

    // ==================== 特定过滤条件 ====================

    /**
     * 按项目 ID 过滤
     */
    public AlarmQuery projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * 按设备 ID 过滤
     */
    public AlarmQuery deviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * 按严重程度过滤
     *
     * @param severity 严重程度 (CRITICAL, MAJOR, MINOR, WARNING, INDETERMINATE)
     */
    public AlarmQuery severity(String severity) {
        this.severity = severity;
        return this;
    }

    /**
     * 按告警类型过滤
     */
    public AlarmQuery type(String type) {
        this.type = type;
        return this;
    }

    /**
     * 按活跃状态过滤
     */
    public AlarmQuery active(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * 按确认状态过滤
     */
    public AlarmQuery acknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
        return this;
    }

    /**
     * 设置时间范围
     *
     * @param startTs 开始时间戳 (毫秒)
     * @param endTs   结束时间戳 (毫秒)
     */
    public AlarmQuery timeRange(long startTs, long endTs) {
        this.startTs = startTs;
        this.endTs = endTs;
        return this;
    }

    /**
     * 查询最近 N 小时的告警
     */
    public AlarmQuery lastHours(int hours) {
        long now = System.currentTimeMillis();
        this.endTs = now;
        this.startTs = now - hours * 60L * 60L * 1000L;
        return this;
    }

    /**
     * 查询最近 N 天的告警
     */
    public AlarmQuery lastDays(int days) {
        long now = System.currentTimeMillis();
        this.endTs = now;
        this.startTs = now - days * 24L * 60L * 60L * 1000L;
        return this;
    }

    // ==================== 实现父类抽象方法 ====================

    @Override
    protected void addSpecificParams(Map<String, Object> params) {
        if (projectId != null) {
            params.put("projectId", projectId);
        }
        if (deviceId != null) {
            params.put("deviceId", deviceId);
        }
        if (severity != null) {
            params.put("severity", severity);
        }
        if (type != null) {
            params.put("type", type);
        }
        if (active != null) {
            params.put("active", active);
        }
        if (acknowledged != null) {
            params.put("acknowledged", acknowledged);
        }
        if (startTs != null) {
            params.put("startTs", startTs);
        }
        if (endTs != null) {
            params.put("endTs", endTs);
        }
    }

    /**
     * 构建查询对象
     */
    public AlarmQuery build() {
        return this;
    }

    // ==================== Getters ====================

    public String getProjectId() {
        return projectId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSeverity() {
        return severity;
    }

    public String getType() {
        return type;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public Long getStartTs() {
        return startTs;
    }

    public Long getEndTs() {
        return endTs;
    }
}
