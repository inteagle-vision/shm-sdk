/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.api;

import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.MonitoringProject;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.ProjectQuery;

import java.util.List;

/**
 * 项目 API 接口
 * <p>
 * 提供项目信息的查询功能。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 分页查询项目
 * PageResult<MonitoringProject> projects = client.projects().list(
 *     ProjectQuery.builder()
 *         .pageSize(20)
 *         .build()
 * );
 *
 * // 获取单个项目
 * MonitoringProject project = client.projects().get("project-001");
 * }</pre>
 */
public interface ProjectApi {

    /**
     * 分页查询项目
     *
     * @param query 查询条件
     * @return 分页结果
     * @throws SdkException 如果查询失败
     */
    PageResult<MonitoringProject> list(ProjectQuery query) throws SdkException;

    /**
     * 查询所有项目 (不分页)
     *
     * @param query 查询条件
     * @return 项目列表
     * @throws SdkException 如果查询失败
     */
    List<MonitoringProject> listAll(ProjectQuery query) throws SdkException;

    /**
     * 获取单个项目
     *
     * @param projectId 项目 ID
     * @return 项目信息
     * @throws com.inteagle.sdk.exception.NotFoundException 如果项目不存在
     * @throws SdkException                                  如果查询失败
     */
    MonitoringProject get(String projectId) throws SdkException;
}
