/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.query;

import java.util.Map;

/**
 * 项目查询
 * <p>
 * 用于构建项目列表查询请求。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 分页查询项目
 * ProjectQuery query = ProjectQuery.builder()
 *     .pageSize(20)
 *     .orderByName(false)
 *     .build();
 *
 * // 按名称搜索
 * ProjectQuery query = ProjectQuery.builder()
 *     .name("桥梁")
 *     .build();
 * }</pre>
 */
public final class ProjectQuery extends BaseQuery<ProjectQuery> {

    private String name;
    private String type;
    private Boolean active;
    private String region;

    private ProjectQuery() {
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建 Builder
     */
    public static ProjectQuery builder() {
        return new ProjectQuery();
    }

    /**
     * 查询所有项目 (默认分页)
     */
    public static ProjectQuery all() {
        return builder();
    }

    /**
     * 快捷方法 - 按类型查询
     *
     * @param type 项目类型
     */
    public static ProjectQuery ofType(String type) {
        return builder().type(type);
    }

    // ==================== 特定过滤条件 ====================

    /**
     * 按项目名称模糊搜索
     */
    public ProjectQuery name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 按项目类型过滤
     */
    public ProjectQuery type(String type) {
        this.type = type;
        return this;
    }

    /**
     * 按活跃状态过滤
     */
    public ProjectQuery active(Boolean active) {
        this.active = active;
        return this;
    }

    /**
     * 按区域过滤
     */
    public ProjectQuery region(String region) {
        this.region = region;
        return this;
    }

    // ==================== 实现父类抽象方法 ====================

    @Override
    protected void addSpecificParams(Map<String, Object> params) {
        if (name != null) {
            params.put("name", name);
        }
        if (type != null) {
            params.put("type", type);
        }
        if (active != null) {
            params.put("active", active);
        }
        if (region != null) {
            params.put("region", region);
        }
    }

    /**
     * 构建查询对象
     */
    public ProjectQuery build() {
        return this;
    }

    // ==================== Getters ====================

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Boolean getActive() {
        return active;
    }

    public String getRegion() {
        return region;
    }
}
