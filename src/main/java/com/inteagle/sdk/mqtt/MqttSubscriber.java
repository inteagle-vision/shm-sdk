/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * MQTT Subscriber for real-time data subscription.
 * <p>This class manages MQTT connections and message subscriptions.
 * <p>Implements AutoCloseable to properly release MQTT client and executor resources.
 */
public class MqttSubscriber implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriber.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TOPIC_PREFIX = "inteagle";
    
    // MQTT connection constants
    private static final int CONNECTION_TIMEOUT = 30;
    private static final int KEEP_ALIVE_INTERVAL = 60;
    private static final int QOS_LEVEL = 1;
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5;
    
    // HMAC algorithm
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    
    // TLS constants
    private static final String TLS_VERSION = "TLSv1.2";

    private final String host;
    private final int port;
    private final String accessKeyId;    // 用于 MQTT 认证
    private final String accessKeySecret; // 用于 MQTT 认证
    private final String customerId;      // 用于 Topic 前缀
    private final boolean useTls;

    private MqttClient mqttClient;
    private final Map<String, Consumer<BridgeMessage>> subscriptions = new ConcurrentHashMap<>();
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    /**
     * 创建 MQTT 订阅者
     * @param host MQTT broker 地址
     * @param port MQTT broker 端口
     * @param accessKeyId Access Key ID (用于认证)
     * @param accessKeySecret Access Key Secret (用于签名，不会明文传输)
     * @param customerId 客户 ID (用于 Topic)
     * @param useTls 是否使用 TLS
     */
    public MqttSubscriber(String host, int port, String accessKeyId, String accessKeySecret,
                          String customerId, boolean useTls) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("host cannot be null or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            throw new IllegalArgumentException("accessKeyId cannot be null or empty");
        }
        if (accessKeySecret == null || accessKeySecret.isEmpty()) {
            throw new IllegalArgumentException("accessKeySecret cannot be null or empty");
        }
        if (customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("customerId cannot be null or empty");
        }
        
        this.host = host;
        this.port = port;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.customerId = customerId;
        this.useTls = useTls;
    }

    /**
     * Connect to MQTT broker (blocking).
     * <p>Uses HMAC-SHA256 signature authentication (SK is never transmitted in plaintext).
     * <p>Password format: {timestamp}:{signature}
     * <p>Signature: HMAC-SHA256(accessKeySecret, timestamp:clientId)
     * 
     * @throws MqttException if connection fails
     */
    public void connect() throws MqttException {
        String protocol = useTls ? "ssl" : "tcp";
        String brokerUrl = String.format("%s://%s:%d", protocol, host, port);
        String clientId = accessKeyId + "_" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Connecting to {} as {}", brokerUrl, clientId);

        mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        // HMAC 签名认证 (SK 不会明文传输)
        long timestamp = System.currentTimeMillis();
        String signString = timestamp + ":" + clientId;
        String signature = hmacSha256(accessKeySecret, signString);
        String password = timestamp + ":" + signature;

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(accessKeyId);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(CONNECTION_TIMEOUT);
        options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
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
     * Connect to MQTT broker (async).
     * <p>Returns a CompletableFuture that completes when connection is established.
     * 
     * @return CompletableFuture that completes when connected
     */
    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                connect();
            } catch (MqttException e) {
                throw new RuntimeException("Failed to connect", e);
            }
        }, asyncExecutor);
    }

    /**
     * Compute HMAC-SHA256 signature (hex encoded).
     */
    private String hmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Hex encode (same as Rust mock service)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC signature", e);
        }
    }

    /**
     * Disconnect from MQTT broker.
     * <p>This only disconnects the MQTT client but does not shut down the executor service.
     * <p>Use {@link #close()} to release all resources.
     * 
     * @throws MqttException if disconnection fails
     */
    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            log.info("Disconnected");
        }
    }

    /**
     * Close all resources including MQTT client and executor service.
     * This method should be called when the subscriber is no longer needed.
     */
    @Override
    public void close() throws Exception {
        disconnect();
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Check if connected to MQTT broker.
     * 
     * @return true if currently connected, false otherwise
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    /**
     * Get the customer ID for topic building.
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Create a topic builder for this customer.
     * Use this to build custom topic patterns.
     *
     * @return TopicBuilder instance
     */
    public TopicBuilder buildTopic() {
        return TopicBuilder.forCustomer(customerId);
    }

    /**
     * Subscribe to all data for a project.
     * <p>Topic pattern: inteagle/{customerId}/p/{projectId}/#
     * 
     * @param projectId the unique identifier of the project
     * @param handler callback function to handle received messages
     * @throws MqttException if subscription fails
     * @throws IllegalArgumentException if projectId or handler is null/empty
     */
    public void subscribeProject(String projectId, Consumer<BridgeMessage> handler) throws MqttException {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("projectId cannot be null or empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/#";
        subscribe(topic, handler);
    }

    /**
     * Subscribe to a monitoring point.
     * <p>Topic pattern: inteagle/{customerId}/p/{projectId}/mp/{pointId}
     * 
     * @param projectId the unique identifier of the project
     * @param pointId the unique identifier of the monitoring point
     * @param handler callback function to handle received messages
     * @throws MqttException if subscription fails
     * @throws IllegalArgumentException if any parameter is null/empty
     */
    public void subscribePoint(String projectId, String pointId, Consumer<BridgeMessage> handler) throws MqttException {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("projectId cannot be null or empty");
        }
        if (pointId == null || pointId.isEmpty()) {
            throw new IllegalArgumentException("pointId cannot be null or empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/mp/" + pointId;
        subscribe(topic, handler);
    }

    /**
     * Subscribe to a device.
     * <p>Topic pattern: inteagle/{customerId}/p/{projectId}/d/{deviceId}
     * 
     * @param projectId the unique identifier of the project
     * @param deviceId the unique identifier of the device
     * @param handler callback function to handle received messages
     * @throws MqttException if subscription fails
     * @throws IllegalArgumentException if any parameter is null/empty
     */
    public void subscribeDevice(String projectId, String deviceId, Consumer<BridgeMessage> handler) throws MqttException {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("projectId cannot be null or empty");
        }
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/d/" + deviceId;
        subscribe(topic, handler);
    }

    /**
     * Subscribe to a custom topic pattern.
     * <p>Supports MQTT wildcards: # (multi-level) and + (single-level)
     * 
     * @param topicPattern the MQTT topic pattern to subscribe to
     * @param handler callback function to handle received messages
     * @throws MqttException if subscription fails or client is not connected
     * @throws IllegalArgumentException if topicPattern or handler is null/empty
     */
    public void subscribe(String topicPattern, Consumer<BridgeMessage> handler) throws MqttException {
        if (topicPattern == null || topicPattern.isEmpty()) {
            throw new IllegalArgumentException("topicPattern cannot be null or empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }

        subscriptions.put(topicPattern, handler);
        mqttClient.subscribe(topicPattern, QOS_LEVEL);
        log.info("Subscribed to: {}", topicPattern);
    }

    /**
     * Unsubscribe from a topic.
     * <p>Removes the subscription and stops receiving messages for the topic pattern.
     * 
     * @param topicPattern the MQTT topic pattern to unsubscribe from
     * @throws MqttException if unsubscription fails
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
            SSLContext sslContext = SSLContext.getInstance(TLS_VERSION);
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SSL", e);
        }
    }
}
