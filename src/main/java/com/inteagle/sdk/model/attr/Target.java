/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model.attr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 测量目标
 * <p>
 * 用于视觉位移计等设备的测量目标定义，包含目标标识、相机参数、ROI区域、参考点等信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Target {

    /** 目标标识，如 "T_01", "T_02" */
    private String targetId;

    /** 目标型号，如 "T100" */
    private String targetModel;

    /** 相机ID */
    private Integer cameraId;

    /** 是否为基准点 */
    private Boolean basePoint;

    /** 是否为基准点2 */
    private Boolean basePoint2;

    /** 实际测量距离 (米) */
    private Double actualMeasureDistance;

    /** 高度差 (米) */
    private Double heightDiff;

    /** 红外 PWM 值 */
    private Integer infraRedPWM;

    /** 初始化时间戳 (毫秒) */
    private Long initTs;

    /** 最后更新时间戳 (毫秒) */
    private Long lastUpdateTs;

    /** 是否自适应 */
    private Boolean selfAdaption;

    /** 是否跳过测量 */
    private Boolean skipMeasurement;

    /** 亮度阈值 */
    private Integer wyBrightnessThr;

    /** 相机曝光模式，如 "Middle", "High", "Low" */
    private String wyCameraExposure;

    /** 感兴趣区域 */
    private Roi roi;

    /** 参考点 */
    private RefPoint refPoint;

    public Target() {
    }

    // ==================== Getters & Setters ====================

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetModel() {
        return targetModel;
    }

    public void setTargetModel(String targetModel) {
        this.targetModel = targetModel;
    }

    public Integer getCameraId() {
        return cameraId;
    }

    public void setCameraId(Integer cameraId) {
        this.cameraId = cameraId;
    }

    public Boolean getBasePoint() {
        return basePoint;
    }

    public void setBasePoint(Boolean basePoint) {
        this.basePoint = basePoint;
    }

    public Boolean getBasePoint2() {
        return basePoint2;
    }

    public void setBasePoint2(Boolean basePoint2) {
        this.basePoint2 = basePoint2;
    }

    public Double getActualMeasureDistance() {
        return actualMeasureDistance;
    }

    public void setActualMeasureDistance(Double actualMeasureDistance) {
        this.actualMeasureDistance = actualMeasureDistance;
    }

    public Double getHeightDiff() {
        return heightDiff;
    }

    public void setHeightDiff(Double heightDiff) {
        this.heightDiff = heightDiff;
    }

    public Integer getInfraRedPWM() {
        return infraRedPWM;
    }

    public void setInfraRedPWM(Integer infraRedPWM) {
        this.infraRedPWM = infraRedPWM;
    }

    public Long getInitTs() {
        return initTs;
    }

    public void setInitTs(Long initTs) {
        this.initTs = initTs;
    }

    public Long getLastUpdateTs() {
        return lastUpdateTs;
    }

    public void setLastUpdateTs(Long lastUpdateTs) {
        this.lastUpdateTs = lastUpdateTs;
    }

    public Boolean getSelfAdaption() {
        return selfAdaption;
    }

    public void setSelfAdaption(Boolean selfAdaption) {
        this.selfAdaption = selfAdaption;
    }

    public Boolean getSkipMeasurement() {
        return skipMeasurement;
    }

    public void setSkipMeasurement(Boolean skipMeasurement) {
        this.skipMeasurement = skipMeasurement;
    }

    public Integer getWyBrightnessThr() {
        return wyBrightnessThr;
    }

    public void setWyBrightnessThr(Integer wyBrightnessThr) {
        this.wyBrightnessThr = wyBrightnessThr;
    }

    public String getWyCameraExposure() {
        return wyCameraExposure;
    }

    public void setWyCameraExposure(String wyCameraExposure) {
        this.wyCameraExposure = wyCameraExposure;
    }

    public Roi getRoi() {
        return roi;
    }

    public void setRoi(Roi roi) {
        this.roi = roi;
    }

    public RefPoint getRefPoint() {
        return refPoint;
    }

    public void setRefPoint(RefPoint refPoint) {
        this.refPoint = refPoint;
    }

    /**
     * 判断是否为基准点（basePoint 或 basePoint2）
     */
    public boolean isAnyBasePoint() {
        return Boolean.TRUE.equals(basePoint) || Boolean.TRUE.equals(basePoint2);
    }

    @Override
    public String toString() {
        return "Target{" +
                "targetId='" + targetId + '\'' +
                ", targetModel='" + targetModel + '\'' +
                ", basePoint=" + basePoint +
                ", roi=" + roi +
                '}';
    }
}
