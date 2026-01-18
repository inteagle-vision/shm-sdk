/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 分页结果
 *
 * @param <T> 元素类型
 */
public final class PageResult<T> implements Iterable<T> {

    private final List<T> data;
    private final long total;
    private final int page;
    private final int pageSize;
    private final boolean hasMore;

    private PageResult(List<T> data, long total, int page, int pageSize) {
        this.data = data != null ? data : Collections.emptyList();
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.hasMore = (page + 1) * pageSize < total;
    }

    /**
     * 创建分页结果
     *
     * @param data     数据列表
     * @param total    总数
     * @param page     当前页
     * @param pageSize 每页大小
     * @param <T>      元素类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> data, long total, int page, int pageSize) {
        return new PageResult<>(data, total, page, pageSize);
    }

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0, 0, 20);
    }

    /**
     * 创建空分页结果（指定分页参数）
     */
    public static <T> PageResult<T> empty(int page, int pageSize) {
        return new PageResult<>(Collections.emptyList(), 0, page, pageSize);
    }

    /**
     * 获取数据列表
     */
    public List<T> getData() {
        return data;
    }

    /**
     * 获取总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 获取当前页码
     */
    public int getPage() {
        return page;
    }

    /**
     * 获取每页大小
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 是否有下一页
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * 获取总页数
     */
    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * 获取当前页元素数量
     */
    public int size() {
        return data.size();
    }

    /**
     * 流式访问
     */
    public Stream<T> stream() {
        return data.stream();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "total=" + total +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", hasMore=" + hasMore +
                ", size=" + data.size() +
                '}';
    }
}
