/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * 告警统计信息
 * <p>
 * 四态统计:
 * - activeUnack: 活跃未确认
 * - activeAck: 活跃已确认
 * - clearedUnack: 已清除未确认
 * - clearedAck: 已清除已确认
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AlarmStatistics {

    private long total;

    // 四态统计 (云端返回格式)
    private long activeUnack;
    private long activeAck;
    private long clearedUnack;
    private long clearedAck;

    // 按严重程度统计 (ALERT/ALARM/ACTION)
    private Map<String, Integer> bySeverity;

    public AlarmStatistics() {
    }

    // ==================== Getters & Setters ====================

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getActiveUnack() {
        return activeUnack;
    }

    public void setActiveUnack(long activeUnack) {
        this.activeUnack = activeUnack;
    }

    public long getActiveAck() {
        return activeAck;
    }

    public void setActiveAck(long activeAck) {
        this.activeAck = activeAck;
    }

    public long getClearedUnack() {
        return clearedUnack;
    }

    public void setClearedUnack(long clearedUnack) {
        this.clearedUnack = clearedUnack;
    }

    public long getClearedAck() {
        return clearedAck;
    }

    public void setClearedAck(long clearedAck) {
        this.clearedAck = clearedAck;
    }

    public Map<String, Integer> getBySeverity() {
        return bySeverity;
    }

    public void setBySeverity(Map<String, Integer> bySeverity) {
        this.bySeverity = bySeverity;
    }

    // ==================== 计算属性 (兼容旧接口) ====================

    /**
     * 获取活跃告警数 (未清除)
     */
    public long getActive() {
        return activeUnack + activeAck;
    }

    /**
     * 获取已确认告警数
     */
    public long getAcknowledged() {
        return activeAck + clearedAck;
    }

    /**
     * 获取已清除告警数
     */
    public long getCleared() {
        return clearedUnack + clearedAck;
    }

    /**
     * 获取严重级别告警数 (ACTION)
     */
    public long getCritical() {
        return bySeverity != null ? bySeverity.getOrDefault("ACTION", 0) : 0;
    }

    /**
     * 获取重要级别告警数 (ALARM)
     */
    public long getMajor() {
        return bySeverity != null ? bySeverity.getOrDefault("ALARM", 0) : 0;
    }

    /**
     * 获取次要级别告警数
     */
    public long getMinor() {
        return 0; // 云端未区分 minor
    }

    /**
     * 获取警告级别告警数 (ALERT)
     */
    public long getWarning() {
        return bySeverity != null ? bySeverity.getOrDefault("ALERT", 0) : 0;
    }

    @Override
    public String toString() {
        return "AlarmStatistics{" +
                "total=" + total +
                ", activeUnack=" + activeUnack +
                ", activeAck=" + activeAck +
                ", clearedUnack=" + clearedUnack +
                ", clearedAck=" + clearedAck +
                '}';
    }
}
