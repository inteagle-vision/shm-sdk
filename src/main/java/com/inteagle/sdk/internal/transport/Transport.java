/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.transport;

import com.inteagle.sdk.exception.SdkException;

import java.io.Closeable;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 传输层抽象接口
 * <p>
 * 提供协议无关的远程调用能力，支持 REST 和未来的 gRPC。
 * <p>
 * 设计要点:
 * <ul>
 *   <li>协议无关: API 实现类只依赖此接口</li>
 *   <li>支持流式: 大数据量场景使用 stream 方法</li>
 *   <li>可替换: 通过 TransportFactory 切换协议</li>
 * </ul>
 *
 * @see TransportFactory
 */
public interface Transport extends Closeable {

    /**
     * 发送 GET 请求
     *
     * @param path         API 路径 (如 "/api/v1/projects")
     * @param queryParams  查询参数 (可为空)
     * @param responseType 响应类型
     * @param <T>          响应类型参数
     * @return 响应对象
     * @throws SdkException 如果调用失败
     */
    <T> T get(String path, Map<String, Object> queryParams, Class<T> responseType)
            throws SdkException;

    /**
     * 发送 POST 请求
     *
     * @param path         API 路径 (如 "/api/v1/telemetry/history")
     * @param body         请求体
     * @param responseType 响应类型
     * @param <T>          响应类型参数
     * @return 响应对象
     * @throws SdkException 如果调用失败
     */
    <T> T post(String path, Object body, Class<T> responseType)
            throws SdkException;

    /**
     * 调用远程方法 (RPC 风格，兼容旧接口)
     *
     * @param service      服务名 (如 "Telemetry", "Device")
     * @param method       方法名 (如 "list", "get", "latest")
     * @param request      请求参数
     * @param responseType 响应类型
     * @param <T>          响应类型参数
     * @return 响应对象
     * @throws SdkException 如果调用失败
     */
    <T> T call(String service, String method, Object request, Class<T> responseType)
            throws SdkException;

    /**
     * 流式调用远程方法
     * <p>
     * 适用于大数据量场景，分批获取数据避免内存溢出。
     *
     * @param service     服务名
     * @param method      方法名
     * @param request     请求参数
     * @param elementType 流元素类型
     * @param <T>         元素类型参数
     * @return 响应流
     */
    <T> Stream<T> stream(String service, String method, Object request, Class<T> elementType);

    /**
     * 检查传输层是否可用
     *
     * @return true 如果连接正常
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * 获取传输协议类型
     *
     * @return 协议类型 (如 "HTTP", "gRPC")
     */
    String getProtocol();

    @Override
    void close();
}
