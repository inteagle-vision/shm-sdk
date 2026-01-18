/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

/**
 * 资源不存在异常 (HTTP 404)
 * <p>
 * 当请求的资源不存在时抛出，例如:
 * <ul>
 *   <li>设备 ID 不存在</li>
 *   <li>项目 ID 不存在</li>
 *   <li>告警 ID 不存在</li>
 * </ul>
 *
 * <h3>处理建议:</h3>
 * <ul>
 *   <li>检查资源 ID 是否正确</li>
 *   <li>确认资源是否已被删除</li>
 *   <li>确认是否有权限访问该资源</li>
 * </ul>
 */
public class NotFoundException extends SdkException {

    private static final long serialVersionUID = 1L;
    private static final int STATUS_CODE = 404;
    private static final String ERROR_CODE = "NOT_FOUND";

    /**
     * 资源类型 (如 "Device", "Project", "Alarm")
     */
    private final String resourceType;

    /**
     * 资源 ID
     */
    private final String resourceId;

    public NotFoundException(String message) {
        super(STATUS_CODE, ERROR_CODE, message);
        this.resourceType = null;
        this.resourceId = null;
    }

    public NotFoundException(String resourceType, String resourceId) {
        super(STATUS_CODE, ERROR_CODE,
                String.format("%s not found: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * 获取资源类型
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * 获取资源 ID
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * 创建设备不存在异常
     */
    public static NotFoundException device(String deviceId) {
        return new NotFoundException("Device", deviceId);
    }

    /**
     * 创建项目不存在异常
     */
    public static NotFoundException project(String projectId) {
        return new NotFoundException("Project", projectId);
    }

    /**
     * 创建告警不存在异常
     */
    public static NotFoundException alarm(String alarmId) {
        return new NotFoundException("Alarm", alarmId);
    }

    /**
     * 创建监测点不存在异常
     */
    public static NotFoundException point(String pointId) {
        return new NotFoundException("MonitoringPoint", pointId);
    }
}
