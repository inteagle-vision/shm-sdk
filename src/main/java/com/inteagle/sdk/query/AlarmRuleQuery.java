/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import java.util.Map;

/**
 * 告警规则查询
 * <p>
 * 用于构建告警规则列表查询请求。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 查询某项目的所有告警规则
 * AlarmRuleQuery query = AlarmRuleQuery.builder()
 *     .projectId("project-001")
 *     .pageSize(20)
 *     .build();
 *
 * // 查询某设备的告警规则
 * AlarmRuleQuery query = AlarmRuleQuery.ofEntity("project-001", "DEVICE", "device-001");
 * }</pre>
 */
public final class AlarmRuleQuery extends BaseQuery<AlarmRuleQuery> {

    private String projectId;
    private String entityType;
    private String entityId;
    private Boolean enabled;

    private AlarmRuleQuery() {
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建 Builder
     */
    public static AlarmRuleQuery builder() {
        return new AlarmRuleQuery();
    }

    /**
     * 快捷方法 - 查询某项目的所有告警规则
     *
     * @param projectId 项目 ID
     */
    public static AlarmRuleQuery ofProject(String projectId) {
        return builder().projectId(projectId);
    }

    /**
     * 快捷方法 - 查询某实体的告警规则
     *
     * @param projectId  项目 ID
     * @param entityType 实体类型 (POINT, DEVICE)
     * @param entityId   实体 ID
     */
    public static AlarmRuleQuery ofEntity(String projectId, String entityType, String entityId) {
        return builder().projectId(projectId).entityType(entityType).entityId(entityId);
    }

    // ==================== 特定过滤条件 ====================

    /**
     * 按项目 ID 过滤 (必填)
     */
    public AlarmRuleQuery projectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * 按实体类型过滤 (POINT, DEVICE)
     */
    public AlarmRuleQuery entityType(String entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * 按实体 ID 过滤
     */
    public AlarmRuleQuery entityId(String entityId) {
        this.entityId = entityId;
        return this;
    }

    /**
     * 按启用状态过滤
     */
    public AlarmRuleQuery enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    // ==================== 实现父类抽象方法 ====================

    @Override
    protected void addSpecificParams(Map<String, Object> params) {
        if (projectId != null) {
            params.put("projectId", projectId);
        }
        if (entityType != null) {
            params.put("entityType", entityType);
        }
        if (entityId != null) {
            params.put("entityId", entityId);
        }
        if (enabled != null) {
            params.put("enabled", enabled);
        }
    }

    /**
     * 构建查询对象
     */
    public AlarmRuleQuery build() {
        return this;
    }

    // ==================== Getters ====================

    public String getProjectId() {
        return projectId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
