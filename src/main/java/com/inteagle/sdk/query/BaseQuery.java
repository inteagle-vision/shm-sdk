/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 查询基类
 * <p>
 * 提供分页、排序、过滤等通用功能，消除子类重复代码。
 * <p>
 * 使用 CRTP (Curiously Recurring Template Pattern) 实现流式 API。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * DeviceQuery query = DeviceQuery.builder()
 *     .projectId("project-001")
 *     .page(0, 20)
 *     .orderBy("createdAt", true)
 *     .build();
 * }</pre>
 *
 * @param <T> 子类类型 (用于流式 API 返回正确类型)
 */
public abstract class BaseQuery<T extends BaseQuery<T>> {

    protected int page = 0;
    protected int pageSize = 20;
    protected String orderBy;
    protected Boolean orderDesc;
    protected Map<String, Object> extraFilters;

    // ==================== 分页方法 ====================

    /**
     * 设置页码 (从 0 开始)
     *
     * @param page 页码
     * @return this
     */
    public T page(int page) {
        this.page = page;
        return self();
    }

    /**
     * 设置每页大小
     *
     * @param pageSize 每页大小
     * @return this
     */
    public T pageSize(int pageSize) {
        this.pageSize = pageSize;
        return self();
    }

    /**
     * 同时设置页码和每页大小
     *
     * @param page     页码 (从 0 开始)
     * @param pageSize 每页大小
     * @return this
     */
    public T page(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
        return self();
    }

    // ==================== 排序方法 ====================

    /**
     * 设置排序字段
     *
     * @param field 排序字段
     * @param desc  是否降序
     * @return this
     */
    public T orderBy(String field, boolean desc) {
        this.orderBy = field;
        this.orderDesc = desc;
        return self();
    }

    /**
     * 按创建时间排序
     *
     * @param desc 是否降序 (true = 最新的在前)
     * @return this
     */
    public T orderByCreatedAt(boolean desc) {
        return orderBy("createdAt", desc);
    }

    /**
     * 按名称排序
     *
     * @param desc 是否降序
     * @return this
     */
    public T orderByName(boolean desc) {
        return orderBy("name", desc);
    }

    /**
     * 按更新时间排序
     *
     * @param desc 是否降序
     * @return this
     */
    public T orderByUpdatedAt(boolean desc) {
        return orderBy("updatedAt", desc);
    }

    // ==================== 过滤器方法 ====================

    /**
     * 添加自定义过滤条件
     *
     * @param key   过滤键
     * @param value 过滤值
     * @return this
     */
    public T filter(String key, Object value) {
        if (extraFilters == null) {
            extraFilters = new LinkedHashMap<>();
        }
        extraFilters.put(key, value);
        return self();
    }

    // ==================== 转换方法 ====================

    /**
     * 转换为 Map (用于 API 调用)
     * <p>
     * 模板方法模式: 先添加分页/排序参数，再调用子类添加特定参数
     *
     * @return 参数 Map
     */
    public final Map<String, Object> toMap() {
        Map<String, Object> params = new LinkedHashMap<>();
        addPaginationParams(params);
        addOrderingParams(params);
        addSpecificParams(params);  // 子类实现
        if (extraFilters != null) {
            params.putAll(extraFilters);
        }
        return params;
    }

    // ==================== 内部方法 ====================

    /**
     * 添加分页参数
     */
    protected void addPaginationParams(Map<String, Object> params) {
        params.put("page", page);
        params.put("pageSize", pageSize);
    }

    /**
     * 添加排序参数
     */
    protected void addOrderingParams(Map<String, Object> params) {
        if (orderBy != null) {
            params.put("orderBy", orderBy);
        }
        if (orderDesc != null) {
            params.put("orderDesc", orderDesc);
        }
    }

    /**
     * 添加子类特定参数 (模板方法)
     *
     * @param params 参数 Map
     */
    protected abstract void addSpecificParams(Map<String, Object> params);

    /**
     * 返回 this 的正确类型 (CRTP 辅助方法)
     */
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    // ==================== Getters ====================

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public Boolean getOrderDesc() {
        return orderDesc;
    }
}
