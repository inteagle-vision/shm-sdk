/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model.attr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 参考点坐标
 * <p>
 * 用于视觉位移计等设备的目标参考点定义
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefPoint {

    /** X 坐标 */
    private Double x;

    /** Y 坐标 */
    private Double y;

    /** 半径/距离 */
    private Double r;

    public RefPoint() {
    }

    public RefPoint(Double x, Double y, Double r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getR() {
        return r;
    }

    public void setR(Double r) {
        this.r = r;
    }

    @Override
    public String toString() {
        return "RefPoint{x=" + x + ", y=" + y + ", r=" + r + '}';
    }
}
