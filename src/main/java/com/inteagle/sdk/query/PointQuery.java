/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import java.util.Map;

/**
 * 监测点查询
 * <p>
 * 用于构建监测点列表查询请求。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 查询某项目下的监测点
 * PointQuery query = PointQuery.builder()
 *     .projectId("project-001")
 *     .pageSize(20)
 *     .orderByName(false)
 *     .build();
 *
 * // 快捷方法
 * PointQuery query = PointQuery.ofProject("project-001");
 * }</pre>
 */
public final class PointQuery extends BaseQuery<PointQuery> {

    private String projectId;
    private String name;
    private String type;
    private String deviceId;
    private Boolean active;
    private Boolean includeAttributes;
    private Boolean includeAlarmRules;

    private PointQuery() {
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建 Builder
     */
    public static PointQuery builder() {
        return new PointQuery();
    }

    /**
     * 快捷方法 - 查询某项目下的监测点
     *
     * @param projectId 项目 ID
     */
    public static PointQuery ofProject(String projectId) {
        return builder().projectId(projectId);
    }

    /**
     * 快捷方法 - 查询某设备关联的监测点
     *
     * @param deviceId 设备 ID
     */
    public static PointQuery ofDevice(String deviceId) {
        return builder().deviceId(deviceId);
    }

    // ==================== 特定过滤条件 ====================

    /**
     * 按项目 ID 过滤
     */
    public PointQuery projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * 按监测点名称模糊搜索
     */
    public PointQuery name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 按监测点类型过滤
     */
    public PointQuery type(String type) {
        this.type = type;
        return this;
    }

    /**
     * 按关联设备 ID 过滤
     */
    public PointQuery deviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * 按活跃状态过滤
     */
    public PointQuery active(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * 是否包含完整属性
     */
    public PointQuery includeAttributes(boolean include) {
        this.includeAttributes = include;
        return this;
    }

    /**
     * 是否包含告警规则
     */
    public PointQuery includeAlarmRules(boolean include) {
        this.includeAlarmRules = include;
        return this;
    }

    // ==================== 实现父类抽象方法 ====================

    @Override
    protected void addSpecificParams(Map<String, Object> params) {
        if (projectId != null) {
            params.put("projectId", projectId);
        }
        if (name != null) {
            params.put("name", name);
        }
        if (type != null) {
            params.put("type", type);
        }
        if (deviceId != null) {
            params.put("deviceId", deviceId);
        }
        if (active != null) {
            params.put("active", active);
        }
        if (includeAttributes != null) {
            params.put("includeAttributes", includeAttributes);
        }
        if (includeAlarmRules != null) {
            params.put("includeAlarmRules", includeAlarmRules);
        }
    }

    /**
     * 构建查询对象
     */
    public PointQuery build() {
        return this;
    }

    // ==================== Getters ====================

    public String getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getIncludeAttributes() {
        return includeAttributes;
    }

    public Boolean getIncludeAlarmRules() {
        return includeAlarmRules;
    }
}
