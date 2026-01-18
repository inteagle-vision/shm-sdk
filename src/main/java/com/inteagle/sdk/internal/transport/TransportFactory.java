/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.transport;

import com.inteagle.sdk.internal.transport.http.HttpTransport;

import java.time.Duration;

/**
 * 传输实例工厂
 * <p>
 * 根据配置创建合适的 Transport 实例。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 创建 HTTP 传输
 * Transport transport = TransportFactory.createHttp(
 *     "https://api.example.com",
 *     "ak_xxx",
 *     "sk_xxx",
 *     Duration.ofSeconds(30)
 * );
 *
 * // 未来: 创建 gRPC 传输
 * // Transport transport = TransportFactory.createGrpc(...);
 * }</pre>
 */
public final class TransportFactory {

    private TransportFactory() {
        // 工具类，不可实例化
    }

    /**
     * 创建 HTTP 传输实例
     *
     * @param endpoint  API 端点 (如 "https://api.example.com")
     * @param accessKey Access Key
     * @param secretKey Secret Key
     * @param timeout   请求超时时间
     * @return HTTP 传输实例
     */
    public static Transport createHttp(String endpoint, String accessKey, String secretKey, Duration timeout) {
        return new HttpTransport(endpoint, accessKey, secretKey, timeout);
    }

    /**
     * 创建 HTTP 传输实例 (默认超时)
     *
     * @param endpoint  API 端点
     * @param accessKey Access Key
     * @param secretKey Secret Key
     * @return HTTP 传输实例
     */
    public static Transport createHttp(String endpoint, String accessKey, String secretKey) {
        return createHttp(endpoint, accessKey, secretKey, Duration.ofSeconds(30));
    }

    // 未来 gRPC 支持
    // public static Transport createGrpc(String endpoint, String accessKey, String secretKey) {
    //     return new GrpcTransport(endpoint, accessKey, secretKey);
    // }

    /**
     * 传输协议枚举
     */
    public enum Protocol {
        /**
         * HTTP/HTTPS 协议
         */
        HTTP,

        /**
         * gRPC 协议 (未来支持)
         */
        GRPC
    }
}
