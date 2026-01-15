/*
 * Copyright © 2024-2026 Inteagle Inc.
 * Inteagle Cloud SDK - Unified client for REST API and MQTT subscription
 */
package com.inteagle.sdk;

import com.inteagle.sdk.mqtt.MqttSubscriber;
import com.inteagle.sdk.rest.RestClient;

/**
 * Unified client for Inteagle Cloud Platform.
 *
 * <p>Provides both REST API (fast queries) and MQTT (real-time data) capabilities.
 *
 * <p>Usage:
 * <pre>{@code
 * InteagleClient client = InteagleClient.builder()
 *     .apiEndpoint("https://api.inteagle.com")
 *     .mqttEndpoint("mqtt.inteagle.com", 8883)
 *     .credentials(customerId, accessToken)
 *     .build();
 *
 * // REST API - Fast queries
 * List<Device> devices = client.rest().getDevices("project-id");
 * Telemetry data = client.rest().getLatestTelemetry("device-id");
 *
 * // MQTT - Real-time subscription
 * client.mqtt().subscribeProject("project-id", message -> {
 *     System.out.println("Real-time data: " + message);
 * });
 * }</pre>
 */
public class InteagleClient {

    private final RestClient restClient;
    private final MqttSubscriber mqttSubscriber;

    private InteagleClient(Builder builder) {
        this.restClient = new RestClient(builder.apiEndpoint, builder.customerId, builder.accessToken);
        this.mqttSubscriber = new MqttSubscriber(
                builder.mqttHost,
                builder.mqttPort,
                builder.customerId,
                builder.accessToken,
                builder.useTls
        );
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
        private String customerId;
        private String accessToken;
        private boolean useTls = true;

        public Builder apiEndpoint(String endpoint) {
            this.apiEndpoint = endpoint;
            return this;
        }

        public Builder mqttEndpoint(String host, int port) {
            this.mqttHost = host;
            this.mqttPort = port;
            return this;
        }

        public Builder credentials(String customerId, String accessToken) {
            this.customerId = customerId;
            this.accessToken = accessToken;
            return this;
        }

        public Builder useTls(boolean useTls) {
            this.useTls = useTls;
            return this;
        }

        public InteagleClient build() {
            if (customerId == null || accessToken == null) {
                throw new IllegalArgumentException("customerId and accessToken are required");
            }
            return new InteagleClient(this);
        }
    }
}
