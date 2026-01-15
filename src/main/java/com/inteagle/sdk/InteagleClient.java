/*
 * Copyright © 2026 Inteagle Inc.
 * Inteagle Cloud SDK - Unified client for REST API and MQTT subscription
 */
package com.inteagle.sdk;

import com.inteagle.sdk.model.CredentialInfo;
import com.inteagle.sdk.mqtt.MqttSubscriber;
import com.inteagle.sdk.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InteagleClient {

    private static final Logger log = LoggerFactory.getLogger(InteagleClient.class);

    private final RestClient restClient;
    private final MqttSubscriber mqttSubscriber;
    private final String customerId;

    private InteagleClient(Builder builder, String customerId) {
        this.customerId = customerId;
        this.restClient = new RestClient(builder.apiEndpoint, builder.accessKeyId, builder.accessKeySecret);
        this.mqttSubscriber = new MqttSubscriber(
                builder.mqttHost,
                builder.mqttPort,
                builder.accessKeyId,
                builder.accessKeySecret,
                customerId,
                builder.mqttUseTls
        );
    }

    /**
     * Get the customer ID.
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Get REST API client for fast queries.
     */
    public RestClient rest() {
        return restClient;
    }

    /**
     * Get MQTT subscriber for real-time data.
     */
    public MqttSubscriber mqtt() {
        return mqttSubscriber;
    }

    /**
     * Close all connections.
     */
    public void close() throws Exception {
        mqttSubscriber.disconnect();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiEndpoint = "https://api.shm.inteagle.com";
        private String mqttHost = "broker.shm.inteagle.com";
        private int mqttPort = 8883;
        private String accessKeyId;
        private String accessKeySecret;
        private String customerId;  // 可选，如果不提供会通过 API 获取
        private boolean mqttUseTls = true;  // MQTT TLS 可选

        /**
         * Set API endpoint (must be HTTPS).
         */
        public Builder apiEndpoint(String endpoint) {
            if (!endpoint.startsWith("https://")) {
                throw new IllegalArgumentException("API endpoint must use HTTPS");
            }
            this.apiEndpoint = endpoint;
            return this;
        }

        /**
         * Set MQTT broker endpoint.
         * TLS is automatically determined by port:
         * - Port 8883: TLS enabled
         * - Port 1883: TLS disabled
         * - Other ports: TLS enabled by default
         *
         * Use {@link #mqttUseTls(boolean)} to override automatic detection.
         *
         * @param host MQTT broker host
         * @param port MQTT broker port
         */
        public Builder mqttEndpoint(String host, int port) {
            this.mqttHost = host;
            this.mqttPort = port;
            // 根据端口自动判断 TLS
            this.mqttUseTls = (port != 1883);
            return this;
        }

        /**
         * Explicitly set whether to use TLS for MQTT connection.
         * This overrides the automatic port-based detection.
         *
         * @param useTls true to use TLS (ssl://), false for plain TCP (tcp://)
         */
        public Builder mqttUseTls(boolean useTls) {
            this.mqttUseTls = useTls;
            return this;
        }

        /**
         * Set AK/AS credentials for authentication.
         * The customerId will be fetched automatically from API if not provided.
         * @param accessKeyId The Access Key ID (ak_xxx format)
         * @param accessKeySecret The Access Key Secret (sk_xxx format)
         */
        public Builder credentials(String accessKeyId, String accessKeySecret) {
            this.accessKeyId = accessKeyId;
            this.accessKeySecret = accessKeySecret;
            return this;
        }

        /**
         * Optionally set the customerId explicitly.
         * If not set, it will be fetched from API using the credentials.
         * @param customerId The customer ID
         */
        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public InteagleClient build() {
            if (accessKeyId == null || accessKeySecret == null) {
                throw new IllegalArgumentException("accessKeyId and accessKeySecret are required");
            }

            String resolvedCustomerId = customerId;

            // 如果没有提供 customerId，从 API 自动获取
            if (resolvedCustomerId == null) {
                try {
                    RestClient tempClient = new RestClient(apiEndpoint, accessKeyId, accessKeySecret);
                    CredentialInfo credInfo = tempClient.getCredentialInfo(accessKeyId);
                    resolvedCustomerId = credInfo.getCustomerId();
                    log.info("Auto-resolved customerId: {}", resolvedCustomerId);
                } catch (Exception e) {
                    log.warn("Failed to fetch customerId from API, using accessKeyId as fallback: {}", e.getMessage());
                    resolvedCustomerId = accessKeyId;
                }
            }

            return new InteagleClient(this, resolvedCustomerId);
        }
    }
}
