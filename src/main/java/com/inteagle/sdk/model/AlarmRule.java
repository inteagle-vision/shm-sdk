/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 告警规则配置
 * <p>
 * 使用三级阈值模式 (3A)：每个监测指标可配置预警/报警/紧急三个阈值
 * <p>
 * 规则作用范围由 entityType + entityId 决定：
 * <ul>
 *   <li>entityId 为空：项目级规则，应用于该类型的所有实体</li>
 *   <li>entityId 有值：实体级规则，仅应用于特定设备/监测点</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AlarmRule {

    /** 规则唯一标识 */
    private String id;
    /** 所属项目ID */
    private String projectId;
    /** 规则名称 */
    private String name;
    /** 规则描述 */
    private String description;
    /** 是否启用 */
    private Boolean enabled;
    /** 实体类型: DEVICE, POINT */
    private String entityType;
    /** 关联的实体ID（为空表示应用于所有同类型实体） */
    private String entityId;
    /** 告警类型标识，如 TEMPERATURE, VIBRATION, PRESSURE */
    private String alarmType;
    /** 三级阈值配置 (3A模式) */
    private List<ThresholdRule> thresholds;
    /** 创建时间（毫秒时间戳） */
    private Long createTime;
    /** 更新时间（毫秒时间戳） */
    private Long updateTime;

    public AlarmRule() {
    }

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public List<ThresholdRule> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<ThresholdRule> thresholds) {
        this.thresholds = thresholds;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 判断是否为项目级规则（应用于所有同类型实体）
     */
    public boolean isProjectLevel() {
        return entityId == null || entityId.isEmpty();
    }

    // ==================== 内部类 ====================

    /**
     * 三级阈值规则 (3A模式)
     * <p>
     * 定义同一监测指标的三个告警级别阈值，并映射到 ThingsBoard 的告警严重级别
     * <p>
     * 默认映射：
     * <ul>
     *   <li>alert (预警) → WARNING</li>
     *   <li>alarm (报警) → MAJOR</li>
     *   <li>action (紧急) → CRITICAL</li>
     * </ul>
     * <p>
     * 运行时根据当前值与阈值比较确定告警严重程度
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThresholdRule {
        /** 监测指标键名，如 temperature, vibration, pressure */
        private String key;
        /** 指标显示名称 */
        private String keyName;
        /** 比较操作符: GT, GTE, LT, LTE, EQ, NEQ */
        private String operator;
        /** 预警阈值 (一级) */
        private Double alert;
        /** 报警阈值 (二级) */
        private Double alarm;
        /** 紧急阈值 (三级) */
        private Double action;
        /** 单位，如 ℃, mm/s, MPa */
        private String unit;

        // ===== TB 严重级别映射 =====
        /** 预警对应的 TB 严重级别 (默认: WARNING) */
        private String alertSeverity;
        /** 报警对应的 TB 严重级别 (默认: MAJOR) */
        private String alarmSeverity;
        /** 紧急对应的 TB 严重级别 (默认: CRITICAL) */
        private String actionSeverity;

        // ===== 默认值常量 =====
        public static final String DEFAULT_ALERT_SEVERITY = "WARNING";
        public static final String DEFAULT_ALARM_SEVERITY = "MAJOR";
        public static final String DEFAULT_ACTION_SEVERITY = "CRITICAL";

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Double getAlert() {
            return alert;
        }

        public void setAlert(Double alert) {
            this.alert = alert;
        }

        public Double getAlarm() {
            return alarm;
        }

        public void setAlarm(Double alarm) {
            this.alarm = alarm;
        }

        public Double getAction() {
            return action;
        }

        public void setAction(Double action) {
            this.action = action;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getAlertSeverity() {
            return alertSeverity;
        }

        public void setAlertSeverity(String alertSeverity) {
            this.alertSeverity = alertSeverity;
        }

        public String getAlarmSeverity() {
            return alarmSeverity;
        }

        public void setAlarmSeverity(String alarmSeverity) {
            this.alarmSeverity = alarmSeverity;
        }

        public String getActionSeverity() {
            return actionSeverity;
        }

        public void setActionSeverity(String actionSeverity) {
            this.actionSeverity = actionSeverity;
        }

        /** 获取预警的有效 TB 严重级别 */
        public String getEffectiveAlertSeverity() {
            return alertSeverity != null ? alertSeverity : DEFAULT_ALERT_SEVERITY;
        }

        /** 获取报警的有效 TB 严重级别 */
        public String getEffectiveAlarmSeverity() {
            return alarmSeverity != null ? alarmSeverity : DEFAULT_ALARM_SEVERITY;
        }

        /** 获取紧急的有效 TB 严重级别 */
        public String getEffectiveActionSeverity() {
            return actionSeverity != null ? actionSeverity : DEFAULT_ACTION_SEVERITY;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(keyName != null ? keyName : key);
            if (operator != null) sb.append(" ").append(operator);
            sb.append(" [");
            if (alert != null) sb.append("预警:").append(alert).append("→").append(getEffectiveAlertSeverity());
            if (alarm != null) sb.append(" 报警:").append(alarm).append("→").append(getEffectiveAlarmSeverity());
            if (action != null) sb.append(" 紧急:").append(action).append("→").append(getEffectiveActionSeverity());
            sb.append("]");
            if (unit != null) sb.append(" ").append(unit);
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        return "AlarmRule{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", entityType='" + entityType + '\'' +
                ", alarmType='" + alarmType + '\'' +
                ", enabled=" + enabled +
                ", projectLevel=" + isProjectLevel() +
                '}';
    }
}
