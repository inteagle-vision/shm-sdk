/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.response;

import java.util.List;

/**
 * 分页数据
 * <p>
 * 服务端响应格式:
 * <pre>
 * {
 *   "items": [...],
 *   "total": 100,
 *   "page": 0,
 *   "pageSize": 20,
 *   "hasNext": true
 * }
 * </pre>
 *
 * @param <T> 数据项类型
 */
public class PagedData<T> {

    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码（从 0 开始）
     */
    private int page;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    public PagedData() {
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
