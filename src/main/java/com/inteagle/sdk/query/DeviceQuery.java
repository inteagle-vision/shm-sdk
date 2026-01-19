/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import java.util.Map;

/**
 * 设备查询
 * <p>
 * 用于构建设备列表查询请求。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 查询某项目下的活跃设备
 * DeviceQuery query = DeviceQuery.builder()
 *     .projectId("project-001")
 *     .active(true)
 *     .pageSize(20)
 *     .orderByName(false)
 *     .build();
 *
 * // 快捷方法
 * DeviceQuery query = DeviceQuery.ofProject("project-001");
 * }</pre>
 */
public final class DeviceQuery extends BaseQuery<DeviceQuery> {

    private String projectId;
    private String name;
    private String type;
    private Boolean active;
    private String label;
    private Boolean includeMonitoringPoints;
    private Boolean includeAlarmRules;
    private Boolean includeAttributes;

    private DeviceQuery() {
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建 Builder
     */
    public static DeviceQuery builder() {
        return new DeviceQuery();
    }

    /**
     * 快捷方法 - 查询某项目下的设备
     *
     * @param projectId 项目 ID
     * @return 查询对象
     */
    public static DeviceQuery ofProject(String projectId) {
        return builder().projectId(projectId);
    }

    /**
     * 快捷方法 - 查询某类型的设备
     *
     * @param type 设备类型
     * @return 查询对象
     */
    public static DeviceQuery ofType(String type) {
        return builder().type(type);
    }

    // ==================== 特定过滤条件 ====================

    /**
     * 按项目 ID 过滤
     *
     * @param projectId 项目 ID
     * @return this
     */
    public DeviceQuery projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * 按设备名称模糊搜索
     *
     * @param name 设备名称 (支持模糊搜索)
     * @return this
     */
    public DeviceQuery name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 按设备类型过滤
     *
     * @param type 设备类型
     * @return this
     */
    public DeviceQuery type(String type) {
        this.type = type;
        return this;
    }

    /**
     * 按活跃状态过滤
     *
     * @param active 是否活跃
     * @return this
     */
    public DeviceQuery active(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * 按标签过滤
     *
     * @param label 设备标签
     * @return this
     */
    public DeviceQuery label(String label) {
        this.label = label;
        return this;
    }

    /**
     * 是否包含关联的监测点
     *
     * @param include 是否包含
     * @return this
     */
    public DeviceQuery includeMonitoringPoints(boolean include) {
        this.includeMonitoringPoints = include;
        return this;
    }

    /**
     * 是否包含告警规则
     *
     * @param include 是否包含
     * @return this
     */
    public DeviceQuery includeAlarmRules(boolean include) {
        this.includeAlarmRules = include;
        return this;
    }

    /**
     * 是否包含属性
     *
     * @param include 是否包含
     * @return this
     */
    public DeviceQuery includeAttributes(boolean include) {
        this.includeAttributes = include;
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
        if (active != null) {
            params.put("active", active);
        }
        if (label != null) {
            params.put("label", label);
        }
        if (includeMonitoringPoints != null) {
            params.put("includeMonitoringPoints", includeMonitoringPoints);
        }
        if (includeAlarmRules != null) {
            params.put("includeAlarmRules", includeAlarmRules);
        }
        if (includeAttributes != null) {
            params.put("includeAttributes", includeAttributes);
        }
    }

    /**
     * 构建查询对象 (兼容 Builder 风格)
     */
    public DeviceQuery build() {
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

    public Boolean getActive() {
        return active;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getIncludeMonitoringPoints() {
        return includeMonitoringPoints;
    }

    public Boolean getIncludeAlarmRules() {
        return includeAlarmRules;
    }

    public Boolean getIncludeAttributes() {
        return includeAttributes;
    }
}
