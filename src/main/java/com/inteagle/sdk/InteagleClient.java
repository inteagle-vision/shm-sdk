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


public class InteagleClient implements AutoCloseable {

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
     * Get the customer ID associated with this client.
     * 
     * @return the customer ID used for MQTT topics and API requests
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Get REST API client for fast queries.
     * <p>Use this client to query projects, devices, telemetry data, and alarms.
     * 
     * @return the REST client instance
     */
    public RestClient rest() {
        return restClient;
    }

    /**
     * Get MQTT subscriber for real-time data.
     * <p>Use this subscriber to connect and subscribe to real-time data streams.
     * 
     * @return the MQTT subscriber instance
     */
    public MqttSubscriber mqtt() {
        return mqttSubscriber;
    }

    /**
     * Close all connections and release resources.
     * <p>This method closes both REST client and MQTT subscriber resources.
     * <p>After calling this method, the client should not be used again.
     * 
     * @throws Exception if an error occurs while closing resources
     */
    @Override
    public void close() throws Exception {
        if (mqttSubscriber != null) {
            mqttSubscriber.close();
        }
        if (restClient != null) {
            restClient.close();
        }
    }

    /**
     * Create a new builder instance for constructing an InteagleClient.
     * 
     * @return a new builder instance
     */
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
         * Set API endpoint (must be HTTPS for security).
         * 
         * @param endpoint the HTTPS API endpoint URL
         * @return this builder instance
         * @throws IllegalArgumentException if endpoint is null, empty, or not HTTPS
         */
        public Builder apiEndpoint(String endpoint) {
            if (endpoint == null || endpoint.isEmpty()) {
                throw new IllegalArgumentException("API endpoint cannot be null or empty");
            }
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
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("MQTT host cannot be null or empty");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("MQTT port must be between 1 and 65535");
            }
            this.mqttHost = host;
            this.mqttPort = port;
            // 根据端口自动判断 TLS
            this.mqttUseTls = (port != 1883);
            return this;
        }

        /**
         * Explicitly set whether to use TLS for MQTT connection.
         * <p>This overrides the automatic port-based detection.
         * <p>Use TLS (true) for production environments to ensure secure communication.
         *
         * @param useTls true to use TLS (ssl://), false for plain TCP (tcp://)
         * @return this builder instance
         */
        public Builder mqttUseTls(boolean useTls) {
            this.mqttUseTls = useTls;
            return this;
        }

        /**
         * Set AK/SK credentials for authentication.
         * <p>The customerId will be fetched automatically from API if not provided via {@link #customerId(String)}.
         * <p>These credentials should be securely stored and never hardcoded in production.
         * 
         * @param accessKeyId The Access Key ID (typically starts with ak_)
         * @param accessKeySecret The Access Key Secret (typically starts with sk_)
         * @return this builder instance
         * @throws IllegalArgumentException if accessKeyId or accessKeySecret is null or empty
         */
        public Builder credentials(String accessKeyId, String accessKeySecret) {
            if (accessKeyId == null || accessKeyId.isEmpty()) {
                throw new IllegalArgumentException("accessKeyId cannot be null or empty");
            }
            if (accessKeySecret == null || accessKeySecret.isEmpty()) {
                throw new IllegalArgumentException("accessKeySecret cannot be null or empty");
            }
            this.accessKeyId = accessKeyId;
            this.accessKeySecret = accessKeySecret;
            return this;
        }

        /**
         * Optionally set the customerId explicitly.
         * <p>If not set, it will be fetched automatically from API using the credentials.
         * <p>Setting this explicitly can avoid an extra API call during client initialization.
         * 
         * @param customerId The customer ID
         * @return this builder instance
         */
        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        /**
         * Build the InteagleClient instance with configured settings.
         * <p>This method validates that required credentials are provided and optionally
         * fetches the customerId from the API if not explicitly set.
         * 
         * @return a configured InteagleClient instance
         * @throws IllegalArgumentException if required fields are missing
         */
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
