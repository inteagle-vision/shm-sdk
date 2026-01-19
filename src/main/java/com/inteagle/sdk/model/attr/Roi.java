/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model.attr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 感兴趣区域 (Region of Interest)
 * <p>
 * 定义图像中的矩形区域，用于视觉位移计等设备的目标检测区域
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Roi {

    /** 左上角 X 坐标 */
    private Integer x;

    /** 左上角 Y 坐标 */
    private Integer y;

    /** 区域宽度 */
    private Integer width;

    /** 区域高度 */
    private Integer height;

    /** 图像宽度 */
    private Integer imageWidth;

    /** 图像高度 */
    private Integer imageHeight;

    public Roi() {
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    @Override
    public String toString() {
        return "Roi{x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + '}';
    }
}
