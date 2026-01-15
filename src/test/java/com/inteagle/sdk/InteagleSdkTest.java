/*
 * Copyright © 2024-2026 Inteagle Inc.
 * Comprehensive SDK Test Suite for Regression Testing
 */
package com.inteagle.sdk;

import com.inteagle.sdk.mqtt.BridgeMessage;
import com.inteagle.sdk.mqtt.MessageType;
import com.inteagle.sdk.mqtt.MqttSubscriber;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Inteagle Cloud SDK.
 *
 * <p>Run with: mvn test
 *
 * <p>For integration tests, set environment variables:
 * - INTEAGLE_API_ENDPOINT
 * - INTEAGLE_MQTT_HOST
 * - INTEAGLE_MQTT_PORT
 * - INTEAGLE_CUSTOMER_ID
 * - INTEAGLE_ACCESS_TOKEN
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InteagleSdkTest {

    // Test configuration
    private static final String TEST_CUSTOMER_ID = System.getenv().getOrDefault("INTEAGLE_CUSTOMER_ID", "test-customer");
    private static final String TEST_ACCESS_TOKEN = System.getenv().getOrDefault("INTEAGLE_ACCESS_TOKEN", "test-token");
    private static final String MQTT_HOST = System.getenv().getOrDefault("INTEAGLE_MQTT_HOST", "localhost");
    private static final int MQTT_PORT = Integer.parseInt(System.getenv().getOrDefault("INTEAGLE_MQTT_PORT", "1884"));

    // ========== Unit Tests ==========

    @Test
    @Order(1)
    @DisplayName("1. InteagleClient builder should create client with defaults")
    void testClientBuilderDefaults() {
        InteagleClient client = InteagleClient.builder()
                .credentials(TEST_CUSTOMER_ID, TEST_ACCESS_TOKEN)
                .build();

        assertNotNull(client);
        assertNotNull(client.rest());
        assertNotNull(client.mqtt());
    }

    @Test
    @Order(2)
    @DisplayName("2. Builder should require credentials")
    void testBuilderRequiresCredentials() {
        assertThrows(IllegalArgumentException.class, () -> {
            InteagleClient.builder().build();
        });
    }

    @Test
    @Order(3)
    @DisplayName("3. MessageType enum should parse correctly")
    void testMessageTypeParsing() {
        assertEquals(MessageType.TELEMETRY, MessageType.fromString("telemetry"));
        assertEquals(MessageType.ALARM, MessageType.fromString("3A"));
        assertEquals(MessageType.EVENT, MessageType.fromString("event"));
        assertEquals(MessageType.IMAGE, MessageType.fromString("image"));
        assertEquals(MessageType.UNKNOWN, MessageType.fromString("invalid"));
        assertEquals(MessageType.UNKNOWN, MessageType.fromString(null));
    }

    @Test
    @Order(4)
    @DisplayName("4. BridgeMessage builder should work correctly")
    void testBridgeMessageBuilder() {
        long ts = System.currentTimeMillis();
        BridgeMessage message = BridgeMessage.builder()
                .topic("inteagle/customer1/p/proj1/d/dev1")
                .type(MessageType.TELEMETRY)
                .ts(ts)
                .rawPayload("{\"deviceId\":\"dev1\"}")
                .build();

        assertNotNull(message);
        assertEquals(MessageType.TELEMETRY, message.getType());
        assertEquals(ts, message.getTs());
        assertTrue(message.getTopic().contains("customer1"));
        assertNotNull(message.toString());
    }

    @Test
    @Order(5)
    @DisplayName("5. MqttSubscriber should be created without connection")
    void testMqttSubscriberCreation() {
        MqttSubscriber subscriber = new MqttSubscriber(
                "localhost", 1883, TEST_CUSTOMER_ID, TEST_ACCESS_TOKEN, false);

        assertNotNull(subscriber);
        assertFalse(subscriber.isConnected());
    }

    // ========== Integration Tests ==========

    @Test
    @Order(10)
    @DisplayName("10. [Integration] MQTT connection test")
    void testMqttConnection() throws Exception {
        // Skip if EMQX not available
        if (!isEmqxAvailable()) {
            System.out.println("EMQX not available at " + MQTT_HOST + ":" + MQTT_PORT + ", skipping");
            return;
        }

        MqttSubscriber subscriber = new MqttSubscriber(
                MQTT_HOST, MQTT_PORT, TEST_CUSTOMER_ID, TEST_ACCESS_TOKEN, false);

        try {
            subscriber.connect();
            assertTrue(subscriber.isConnected(), "Should be connected");

            // Subscribe to test topic
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<BridgeMessage> received = new AtomicReference<>();

            subscriber.subscribeProject("test-project", message -> {
                received.set(message);
                latch.countDown();
            });

            // Wait a bit for subscription
            Thread.sleep(500);
            assertTrue(subscriber.isConnected());

        } finally {
            subscriber.disconnect();
        }

        assertFalse(subscriber.isConnected(), "Should be disconnected");
    }

    @Test
    @Order(11)
    @DisplayName("11. [Integration] Full InteagleClient test")
    void testFullClient() throws Exception {
        if (!isEmqxAvailable()) {
            System.out.println("EMQX not available, skipping full client test");
            return;
        }

        InteagleClient client = InteagleClient.builder()
                .apiEndpoint("http://localhost:8081")
                .mqttEndpoint(MQTT_HOST, MQTT_PORT)
                .credentials(TEST_CUSTOMER_ID, TEST_ACCESS_TOKEN)
                .useTls(false)
                .build();

        try {
            // Test MQTT connection
            client.mqtt().connect();
            assertTrue(client.mqtt().isConnected());

            // Subscribe to data
            client.mqtt().subscribeProject("test-project", msg -> {
                System.out.println("Received: " + msg);
            });

            Thread.sleep(500);

        } finally {
            client.close();
        }
    }

    // ========== Helper Methods ==========

    private boolean isEmqxAvailable() {
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(MQTT_HOST, MQTT_PORT), 2000);
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
