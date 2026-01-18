/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk;

import com.inteagle.sdk.api.*;
import com.inteagle.sdk.internal.impl.*;
import com.inteagle.sdk.internal.serialization.JsonSerializer;
import com.inteagle.sdk.internal.transport.Transport;
import com.inteagle.sdk.internal.transport.TransportFactory;
import com.inteagle.sdk.mqtt.MqttSubscriber;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;

/**
 * Inteagle Cloud SDK 客户端
 * <p>
 * SDK 的主入口，提供对所有 API 的访问。
 *
 * <h3>使用示例:</h3>
 * <pre>{@code
 * // 创建客户端
 * InteagleClient client = InteagleClient.builder()
 *     .endpoint("https://api.example.com")
 *     .credentials("ak_xxx", "sk_xxx")
 *     .timeout(Duration.ofSeconds(30))
 *     .build();
 *
 * // 查询设备
 * PageResult<Device> devices = client.devices().list(
 *     DeviceQuery.builder()
 *         .projectId("project-001")
 *         .active(true)
 *         .build()
 * );
 *
 * // 查询遥测数据
 * Stream<TelemetryValue> telemetry = client.telemetry().query(
 *     TelemetryQuery.builder()
 *         .entityId("device-001")
 *         .keys("temperature")
 *         .lastHours(1)
 *         .build()
 * );
 *
 * // 关闭客户端
 * client.close();
 * }</pre>
 *
 * <h3>异常处理:</h3>
 * <pre>{@code
 * try {
 *     client.devices().get("non-existent");
 * } catch (NotFoundException e) {
 *     // 404 - 资源不存在
 * } catch (AuthenticationException e) {
 *     // 401 - 认证失败
 * } catch (RateLimitException e) {
 *     // 429 - 请求过多
 *     Thread.sleep(e.getRetryAfterMs());
 * } catch (SdkException e) {
 *     // 其他错误
 * }
 * }</pre>
 */
public final class InteagleClient implements Closeable {

    private final Transport transport;
    private final TelemetryApi telemetry;
    private final DeviceApi devices;
    private final AlarmApi alarms;
    private final AlarmRuleApi alarmRules;
    private final PointApi points;
    private final ProjectApi projects;
    private final MqttSubscriber mqttSubscriber;

    private InteagleClient(Builder builder) {
        this.transport = TransportFactory.createHttp(
                builder.endpoint,
                builder.accessKey,
                builder.secretKey,
                builder.timeout
        );

        JsonSerializer serializer = new JsonSerializer();

        this.telemetry = new TelemetryApiImpl(transport, serializer);
        this.devices = new DeviceApiImpl(transport);
        this.alarms = new AlarmApiImpl(transport);
        this.alarmRules = new AlarmRuleApiImpl(transport);
        this.points = new PointApiImpl(transport);
        this.projects = new ProjectApiImpl(transport);

        // 初始化 MQTT（如果配置了）
        if (builder.mqttHost != null && builder.customerId != null) {
            this.mqttSubscriber = new MqttSubscriber(
                    builder.mqttHost,
                    builder.mqttPort,
                    builder.accessKey,
                    builder.secretKey,
                    builder.customerId,
                    builder.mqttUseTls
            );
        } else {
            this.mqttSubscriber = null;
        }
    }

    // ==================== API 访问器 ====================

    /**
     * 获取遥测数据 API
     */
    public TelemetryApi telemetry() {
        return telemetry;
    }

    /**
     * 获取设备 API
     */
    public DeviceApi devices() {
        return devices;
    }

    /**
     * 获取告警 API
     */
    public AlarmApi alarms() {
        return alarms;
    }

    /**
     * 获取告警规则 API
     */
    public AlarmRuleApi alarmRules() {
        return alarmRules;
    }

    /**
     * 获取监测点 API
     */
    public PointApi points() {
        return points;
    }

    /**
     * 获取项目 API
     */
    public ProjectApi projects() {
        return projects;
    }

    /**
     * 获取 MQTT 订阅器（用于实时数据订阅）
     *
     * @return MqttSubscriber 实例，如果未配置 MQTT 则返回 null
     */
    public MqttSubscriber mqtt() {
        return mqttSubscriber;
    }

    /**
     * 检查是否配置了 MQTT
     */
    public boolean hasMqtt() {
        return mqttSubscriber != null;
    }

    // ==================== 生命周期 ====================

    /**
     * 关闭客户端，释放资源
     */
    @Override
    public void close() {
        transport.close();
        if (mqttSubscriber != null) {
            try {
                mqttSubscriber.close();
            } catch (Exception e) {
                // 忽略关闭时的异常
            }
        }
    }

    /**
     * 获取传输协议类型
     *
     * @return 协议名称 (如 "HTTP", "gRPC")
     */
    public String getProtocol() {
        return transport.getProtocol();
    }

    // ==================== 静态工厂 ====================

    /**
     * 创建客户端 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 快捷方法 - 创建客户端
     *
     * @param endpoint  API 端点
     * @param accessKey Access Key
     * @param secretKey Secret Key
     * @return 客户端实例
     */
    public static InteagleClient create(String endpoint, String accessKey, String secretKey) {
        return builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    // ==================== Builder ====================

    /**
     * 客户端构建器
     */
    public static final class Builder {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private Duration timeout = Duration.ofSeconds(30);
        private Protocol protocol = Protocol.HTTP;

        // MQTT 配置
        private String mqttHost;
        private int mqttPort = 8883;
        private String customerId;
        private boolean mqttUseTls = true;

        private Builder() {
        }

        /**
         * 设置 API 端点
         *
         * @param endpoint API 端点 (如 "https://api.example.com")
         * @return this
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * 设置认证凭证
         *
         * @param accessKey Access Key
         * @param secretKey Secret Key
         * @return this
         */
        public Builder credentials(String accessKey, String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            return this;
        }

        /**
         * 设置 Access Key
         */
        public Builder accessKey(String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        /**
         * 设置 Secret Key
         */
        public Builder secretKey(String secretKey) {
            this.secretKey = secretKey;
            return this;
        }

        /**
         * 设置请求超时时间
         *
         * @param timeout 超时时间
         * @return this
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * 使用 HTTP 协议 (默认)
         */
        public Builder useHttp() {
            this.protocol = Protocol.HTTP;
            return this;
        }

        /**
         * 使用 gRPC 协议 (未来支持)
         */
        public Builder useGrpc() {
            this.protocol = Protocol.GRPC;
            return this;
        }

        /**
         * 配置 MQTT 连接
         *
         * @param host       MQTT broker 地址
         * @param port       MQTT broker 端口
         * @param customerId 客户 ID（用于 Topic 前缀）
         * @return this
         */
        public Builder mqtt(String host, int port, String customerId) {
            this.mqttHost = host;
            this.mqttPort = port;
            this.customerId = customerId;
            return this;
        }

        /**
         * 配置 MQTT 连接（使用默认端口 8883）
         *
         * @param host       MQTT broker 地址
         * @param customerId 客户 ID
         * @return this
         */
        public Builder mqtt(String host, String customerId) {
            return mqtt(host, 8883, customerId);
        }

        /**
         * 设置 MQTT 是否使用 TLS
         *
         * @param useTls true 使用 TLS，false 使用明文
         * @return this
         */
        public Builder mqttUseTls(boolean useTls) {
            this.mqttUseTls = useTls;
            return this;
        }

        /**
         * 设置客户 ID（用于 MQTT Topic）
         *
         * @param customerId 客户 ID
         * @return this
         */
        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        /**
         * 构建客户端
         *
         * @return InteagleClient 实例
         * @throws IllegalArgumentException 如果参数无效
         * @throws UnsupportedOperationException 如果协议不支持
         */
        public InteagleClient build() {
            Objects.requireNonNull(endpoint, "endpoint is required");
            Objects.requireNonNull(accessKey, "accessKey is required");
            Objects.requireNonNull(secretKey, "secretKey is required");

            if (protocol == Protocol.GRPC) {
                throw new UnsupportedOperationException("gRPC protocol is not yet supported");
            }

            return new InteagleClient(this);
        }
    }

    /**
     * 传输协议
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
