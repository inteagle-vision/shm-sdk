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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.inteagle.sdk.entity.Entity;
import com.inteagle.sdk.mqtt.data.AlarmData;
import com.inteagle.sdk.mqtt.data.EventData;
import com.inteagle.sdk.mqtt.data.ImageData;
import com.inteagle.sdk.mqtt.data.TelemetryData;

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
    private static final int DEFAULT_QOS_LEVEL = 1;
    private static final int EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final int DEFAULT_RECONNECT_DELAY_MS = 3000;
    
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

    private volatile MqttClient mqttClient;
    private final Map<String, Consumer<BridgeMessage>> subscriptions = new ConcurrentHashMap<>();
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    // 可配置的 QoS 级别
    private int qosLevel = DEFAULT_QOS_LEVEL;

    // 重连计数器
    private final AtomicInteger reconnectCount = new AtomicInteger(0);
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private volatile boolean intentionalDisconnect = false;
    private volatile boolean closed = false;

    // 连接状态监听器
    private Consumer<ConnectionEvent> connectionEventListener;

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
        prepareForConnect();
        connectOnce(false);
    }

    /**
     * Connect to MQTT broker and keep retrying until connected.
     * <p>Use this in long-running applications where the broker or network may
     * be unavailable during process startup. Each attempt creates a fresh MQTT
     * HMAC password, so expired signatures are not reused.
     *
     * @throws MqttException if the thread is interrupted while waiting to retry
     */
    public void connectWithRetry() throws MqttException {
        prepareForConnect();

        while (!closed && !intentionalDisconnect) {
            try {
                connectOnce(false);
                return;
            } catch (MqttException e) {
                closeDisconnectedClient();
                log.warn("Initial MQTT connect failed, retrying in {} ms: {}",
                        DEFAULT_RECONNECT_DELAY_MS, e.getMessage());
                sleepBeforeRetry();
            }
        }
    }

    private void prepareForConnect() {
        closed = false;
        intentionalDisconnect = false;
    }

    private void connectOnce(boolean reconnect) throws MqttException {
        String brokerUrl = brokerUrl();
        String clientId = accessKeyId;

        log.info("Connecting to {} as {}", brokerUrl, clientId);

        if (mqttClient != null && mqttClient.isConnected()) {
            log.info("Already connected to {}", brokerUrl);
            return;
        }

        closeDisconnectedClient();
        mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        mqttClient.setCallback(createCallback());
        mqttClient.connect(createConnectOptions(clientId));

        if (reconnect) {
            int count = reconnectCount.incrementAndGet();
            log.info("Reconnected to {} (count: {}), re-subscribing {} topics...",
                    brokerUrl, count, subscriptions.size());
            resubscribeAll();
            notifyConnectionEvent(ConnectionEvent.reconnected(brokerUrl, count));
        } else {
            log.info("Connected successfully");
            notifyConnectionEvent(ConnectionEvent.connected(brokerUrl));
        }
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
     * Connect to MQTT broker asynchronously and keep retrying until connected.
     *
     * @return CompletableFuture that completes when connection is established
     */
    public CompletableFuture<Void> connectWithRetryAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                connectWithRetry();
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
        intentionalDisconnect = true;
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
        closed = true;
        intentionalDisconnect = true;
        MqttException disconnectException = null;
        try {
            disconnect();
        } catch (MqttException e) {
            disconnectException = e;
        }

        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (disconnectException != null) {
            throw disconnectException;
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
        mqttClient.subscribe(topicPattern, qosLevel);
        log.info("Subscribed to: {} (QoS={})", topicPattern, qosLevel);
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

    // ==================== 类型过滤订阅方法 ====================

    /**
     * 订阅遥测数据，自动解析为 TelemetryData
     * <p>Topic pattern: inteagle/{customerId}/#
     *
     * @param handler 遥测数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeTelemetry(Consumer<TelemetryData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/#";
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.TELEMETRY) {
                TelemetryData data = MessageParser.parseTelemetry(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅告警数据，自动解析为 AlarmData
     * <p>Topic pattern: inteagle/{customerId}/#
     *
     * @param handler 告警数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeAlarm(Consumer<AlarmData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/#";
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.ALARM) {
                AlarmData data = MessageParser.parseAlarm(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅事件数据，自动解析为 EventData
     * <p>Topic pattern: inteagle/{customerId}/#
     *
     * @param handler 事件数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeEvent(Consumer<EventData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/#";
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.EVENT) {
                EventData data = MessageParser.parseEvent(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅图像数据，自动解析为 ImageData
     * <p>Topic pattern: inteagle/{customerId}/#
     *
     * @param handler 图像数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeImage(Consumer<ImageData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/#";
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.IMAGE) {
                ImageData data = MessageParser.parseImage(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅指定设备的遥测数据
     *
     * @param deviceId 设备 ID
     * @param handler 遥测数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeDeviceTelemetry(String deviceId, Consumer<TelemetryData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/d/" + deviceId;
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.TELEMETRY) {
                TelemetryData data = MessageParser.parseTelemetry(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅指定设备的告警数据
     *
     * @param deviceId 设备 ID
     * @param handler 告警数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeDeviceAlarm(String deviceId, Consumer<AlarmData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/d/" + deviceId;
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.ALARM) {
                AlarmData data = MessageParser.parseAlarm(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅项目下所有遥测数据
     *
     * @param projectId 项目 ID
     * @param handler 遥测数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeProjectTelemetry(String projectId, Consumer<TelemetryData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/#";
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.TELEMETRY) {
                TelemetryData data = MessageParser.parseTelemetry(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅项目下所有告警数据
     *
     * @param projectId 项目 ID
     * @param handler 告警数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeProjectAlarm(String projectId, Consumer<AlarmData> handler) throws MqttException {
        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/#";
        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.ALARM) {
                AlarmData data = MessageParser.parseAlarm(msg);
                handler.accept(data);
            }
        });
    }

    /**
     * 订阅所有类型的数据，使用统一回调
     *
     * @param telemetryHandler 遥测数据处理器 (可为 null)
     * @param alarmHandler 告警数据处理器 (可为 null)
     * @param eventHandler 事件数据处理器 (可为 null)
     * @param imageHandler 图像数据处理器 (可为 null)
     * @throws MqttException if subscription fails
     */
    public void subscribeAll(
            Consumer<TelemetryData> telemetryHandler,
            Consumer<AlarmData> alarmHandler,
            Consumer<EventData> eventHandler,
            Consumer<ImageData> imageHandler) throws MqttException {

        String topic = TOPIC_PREFIX + "/" + customerId + "/#";
        subscribe(topic, msg -> {
            switch (msg.getType()) {
                case TELEMETRY:
                    if (telemetryHandler != null) {
                        telemetryHandler.accept(MessageParser.parseTelemetry(msg));
                    }
                    break;
                case ALARM:
                    if (alarmHandler != null) {
                        alarmHandler.accept(MessageParser.parseAlarm(msg));
                    }
                    break;
                case EVENT:
                    if (eventHandler != null) {
                        eventHandler.accept(MessageParser.parseEvent(msg));
                    }
                    break;
                case IMAGE:
                    if (imageHandler != null) {
                        imageHandler.accept(MessageParser.parseImage(msg));
                    }
                    break;
                default:
                    log.debug("Unknown message type: {}", msg.getType());
            }
        });
    }

    private void handleMessage(String topic, MqttMessage mqttMessage) {
        try {
            String payload = new String(mqttMessage.getPayload());
            JsonNode json = MAPPER.readTree(payload);

            // Parse entity field if present
            Entity entity = Entity.from(json.path("entity"));

            BridgeMessage message = BridgeMessage.builder()
                    .topic(topic)
                    .type(MessageType.fromString(json.path("type").asText()))
                    .ts(json.path("ts").asLong())
                    .entity(entity)
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

    // ==================== 重连与 QoS 配置 ====================

    private String brokerUrl() {
        String protocol = useTls ? "ssl" : "tcp";
        return String.format("%s://%s:%d", protocol, host, port);
    }

    private void sleepBeforeRetry() throws MqttException {
        try {
            Thread.sleep(DEFAULT_RECONNECT_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MqttException(e);
        }
    }

    private void closeDisconnectedClient() {
        MqttClient client = mqttClient;
        if (client == null || client.isConnected()) {
            return;
        }

        try {
            client.close();
        } catch (MqttException e) {
            log.debug("Failed to close disconnected MQTT client: {}", e.getMessage());
        } finally {
            if (mqttClient == client) {
                mqttClient = null;
            }
        }
    }

    MqttConnectOptions createConnectOptions(String clientId) {
        long timestamp = System.currentTimeMillis();
        String signString = timestamp + ":" + clientId;
        String signature = hmacSha256(accessKeySecret, signString);
        String password = timestamp + ":" + signature;

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(accessKeyId);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(CONNECTION_TIMEOUT);
        options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        options.setAutomaticReconnect(false);
        options.setCleanSession(true);

        if (useTls) {
            options.setSocketFactory(getDefaultSSLSocketFactory());
        }

        return options;
    }

    private MqttCallbackExtended createCallback() {
        return new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                log.debug("MQTT connect complete: reconnect={}, serverURI={}", reconnect, serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                String reason = cause != null ? cause.getMessage() : "unknown";
                log.warn("Connection lost: {}", reason);
                notifyConnectionEvent(ConnectionEvent.disconnected(reason));

                if (!intentionalDisconnect && !closed) {
                    startReconnectLoop();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                handleMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        };
    }

    private void startReconnectLoop() {
        if (!reconnecting.compareAndSet(false, true)) {
            return;
        }

        asyncExecutor.submit(() -> {
            try {
                while (!closed && !intentionalDisconnect) {
                    try {
                        Thread.sleep(DEFAULT_RECONNECT_DELAY_MS);
                        if (closed || intentionalDisconnect) {
                            return;
                        }

                        MqttClient client = mqttClient;
                        if (client == null || client.isConnected()) {
                            return;
                        }

                        log.info("Reconnecting to {} as {}", brokerUrl(), accessKeyId);
                        connectOnce(true);
                        return;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (MqttException | RuntimeException e) {
                        log.warn("Reconnect failed: {}", e.getMessage());
                    }
                }
            } finally {
                reconnecting.set(false);
                if (!closed && !intentionalDisconnect && mqttClient != null && !mqttClient.isConnected()) {
                    startReconnectLoop();
                }
            }
        });
    }

    /**
     * 重新订阅所有已注册的 topic（重连后调用）
     */
    private void resubscribeAll() {
        for (String topic : subscriptions.keySet()) {
            try {
                mqttClient.subscribe(topic, qosLevel);
                log.debug("Re-subscribed to: {}", topic);
            } catch (MqttException e) {
                log.error("Failed to re-subscribe to {}: {}", topic, e.getMessage());
            }
        }
    }

    /**
     * 通知连接事件监听器
     */
    private void notifyConnectionEvent(ConnectionEvent event) {
        if (connectionEventListener != null) {
            try {
                asyncExecutor.submit(() -> connectionEventListener.accept(event));
            } catch (Exception e) {
                log.error("Error notifying connection event listener: {}", e.getMessage());
            }
        }
    }

    /**
     * 设置连接事件监听器
     * <p>监听连接、断开、重连等事件
     *
     * @param listener 连接事件监听器
     */
    public void setConnectionEventListener(Consumer<ConnectionEvent> listener) {
        this.connectionEventListener = listener;
    }

    /**
     * 设置 QoS 级别
     * <p>必须在订阅之前设置
     *
     * @param qosLevel QoS 级别 (0, 1, 或 2)
     * @throws IllegalArgumentException 如果 QoS 级别无效
     */
    public void setQosLevel(int qosLevel) {
        if (qosLevel < 0 || qosLevel > 2) {
            throw new IllegalArgumentException("QoS level must be 0, 1, or 2");
        }
        this.qosLevel = qosLevel;
    }

    /**
     * 获取当前 QoS 级别
     *
     * @return QoS 级别
     */
    public int getQosLevel() {
        return qosLevel;
    }

    /**
     * 获取重连次数
     *
     * @return 重连次数
     */
    public int getReconnectCount() {
        return reconnectCount.get();
    }

    // ==================== 带过滤器的订阅方法 ====================

    /**
     * 订阅遥测数据，并按 key 过滤
     * <p>只有指定的 key 会被传递给 handler
     *
     * @param keys 需要接收的遥测数据 key 集合
     * @param handler 遥测数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeTelemetryWithFilter(Set<String> keys, Consumer<TelemetryData> handler) throws MqttException {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        String topic = TOPIC_PREFIX + "/" + customerId + "/#";
        Set<String> filterKeys = new HashSet<>(keys); // 复制以避免外部修改

        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.TELEMETRY) {
                TelemetryData data = MessageParser.parseTelemetry(msg);
                // 过滤只保留指定的 key
                TelemetryData filtered = data.filter(filterKeys);
                if (filtered != null) {
                    handler.accept(filtered);
                }
            }
        });
    }

    /**
     * 订阅指定设备的遥测数据，并按 key 过滤
     *
     * @param deviceId 设备 ID
     * @param keys 需要接收的遥测数据 key 集合
     * @param handler 遥测数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeDeviceTelemetryWithFilter(String deviceId, Set<String> keys,
                                                    Consumer<TelemetryData> handler) throws MqttException {
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        String topic = TOPIC_PREFIX + "/" + customerId + "/d/" + deviceId;
        Set<String> filterKeys = new HashSet<>(keys);

        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.TELEMETRY) {
                TelemetryData data = MessageParser.parseTelemetry(msg);
                TelemetryData filtered = data.filter(filterKeys);
                if (filtered != null) {
                    handler.accept(filtered);
                }
            }
        });
    }

    /**
     * 订阅项目下所有遥测数据，并按 key 过滤
     *
     * @param projectId 项目 ID
     * @param keys 需要接收的遥测数据 key 集合
     * @param handler 遥测数据处理器
     * @throws MqttException if subscription fails
     */
    public void subscribeProjectTelemetryWithFilter(String projectId, Set<String> keys,
                                                     Consumer<TelemetryData> handler) throws MqttException {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("projectId cannot be null or empty");
        }
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        String topic = TOPIC_PREFIX + "/" + customerId + "/p/" + projectId + "/#";
        Set<String> filterKeys = new HashSet<>(keys);

        subscribe(topic, msg -> {
            if (msg.getType() == MessageType.TELEMETRY) {
                TelemetryData data = MessageParser.parseTelemetry(msg);
                TelemetryData filtered = data.filter(filterKeys);
                if (filtered != null) {
                    handler.accept(filtered);
                }
            }
        });
    }

    // ==================== 连接事件类 ====================

    /**
     * 连接事件，用于通知连接状态变化
     */
    public static class ConnectionEvent {
        public enum Type {
            CONNECTED,      // 首次连接成功
            DISCONNECTED,   // 断开连接
            RECONNECTED     // 重新连接成功
        }

        private final Type type;
        private final String serverUri;
        private final String message;
        private final int reconnectCount;
        private final long timestamp;

        private ConnectionEvent(Type type, String serverUri, String message, int reconnectCount) {
            this.type = type;
            this.serverUri = serverUri;
            this.message = message;
            this.reconnectCount = reconnectCount;
            this.timestamp = System.currentTimeMillis();
        }

        public static ConnectionEvent connected(String serverUri) {
            return new ConnectionEvent(Type.CONNECTED, serverUri, "Connected", 0);
        }

        public static ConnectionEvent disconnected(String reason) {
            return new ConnectionEvent(Type.DISCONNECTED, null, reason, 0);
        }

        public static ConnectionEvent reconnected(String serverUri, int count) {
            return new ConnectionEvent(Type.RECONNECTED, serverUri, "Reconnected", count);
        }

        public Type getType() { return type; }
        public String getServerUri() { return serverUri; }
        public String getMessage() { return message; }
        public int getReconnectCount() { return reconnectCount; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("ConnectionEvent{type=%s, serverUri='%s', message='%s', reconnectCount=%d}",
                    type, serverUri, message, reconnectCount);
        }
    }
}
