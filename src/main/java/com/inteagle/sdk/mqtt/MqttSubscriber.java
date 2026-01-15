/*
 * Copyright © 2024-2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * MQTT Subscriber for real-time data subscription.
 */
public class MqttSubscriber {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriber.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TOPIC_PREFIX = "inteagle";

    private final String host;
    private final int port;
    private final String customerId;
    private final String accessToken;
    private final boolean useTls;

    private MqttClient mqttClient;
    private final Map<String, Consumer<BridgeMessage>> subscriptions = new ConcurrentHashMap<>();

    public MqttSubscriber(String host, int port, String customerId, String accessToken, boolean useTls) {
        this.host = host;
        this.port = port;
        this.customerId = customerId;
        this.accessToken = accessToken;
        this.useTls = useTls;
    }

    /**
     * Connect to MQTT broker.
     */
    public void connect() throws MqttException {
        String protocol = useTls ? "ssl" : "tcp";
        String brokerUrl = String.format("%s://%s:%d", protocol, host, port);
        String clientId = customerId + "_" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Connecting to {} as {}", brokerUrl, clientId);

        mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(customerId);
        options.setPassword(accessToken.toCharArray());
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        if (useTls) {
            options.setSocketFactory(getDefaultSSLSocketFactory());
        }

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("Connection lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                handleMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        mqttClient.connect(options);
        log.info("Connected successfully");
    }

    /**
     * Disconnect from MQTT broker.
     */
    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            log.info("Disconnected");
        }
    }

    /**
     * Check if connected.
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Subscribe to all data for a project.
     */
    public void subscribeProject(String projectId, Consumer<BridgeMessage> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/#";
        subscribe(topic, handler);
    }

    /**
     * Subscribe to a monitoring point.
     */
    public void subscribePoint(String projectId, String pointId, Consumer<BridgeMessage> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/mp/" + pointId;
        subscribe(topic, handler);
    }

    /**
     * Subscribe to a device.
     */
    public void subscribeDevice(String projectId, String deviceId, Consumer<BridgeMessage> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/d/" + deviceId;
        subscribe(topic, handler);
    }

    /**
     * Subscribe to a custom topic pattern.
     */
    public void subscribe(String topicPattern, Consumer<BridgeMessage> handler) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }

        subscriptions.put(topicPattern, handler);
        mqttClient.subscribe(topicPattern, 1);
        log.info("Subscribed to: {}", topicPattern);
    }

    /**
     * Unsubscribe from a topic.
     */
    public void unsubscribe(String topicPattern) throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.unsubscribe(topicPattern);
            subscriptions.remove(topicPattern);
            log.info("Unsubscribed from: {}", topicPattern);
        }
    }

    private void handleMessage(String topic, MqttMessage mqttMessage) {
        try {
            String payload = new String(mqttMessage.getPayload());
            JsonNode json = MAPPER.readTree(payload);

            BridgeMessage message = BridgeMessage.builder()
                    .topic(topic)
                    .type(MessageType.fromString(json.path("type").asText()))
                    .ts(json.path("ts").asLong())
                    .payload(json.path("payload"))
                    .rawPayload(payload)
                    .build();

            for (Map.Entry<String, Consumer<BridgeMessage>> entry : subscriptions.entrySet()) {
                if (topicMatches(entry.getKey(), topic)) {
                    try {
                        entry.getValue().accept(message);
                    } catch (Exception e) {
                        log.error("Error in message handler: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse message: {}", e.getMessage());
        }
    }

    private boolean topicMatches(String pattern, String topic) {
        if (pattern.equals(topic)) return true;
        if (pattern.endsWith("#")) {
            return topic.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return false;
    }

    private SSLSocketFactory getDefaultSSLSocketFactory() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SSL", e);
        }
    }
}
