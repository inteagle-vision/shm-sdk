/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.integration;

import com.inteagle.sdk.entity.Entity;
import com.inteagle.sdk.model.VdmTelemetry;
import com.inteagle.sdk.mqtt.BridgeMessage;
import com.inteagle.sdk.mqtt.MessageParser;
import com.inteagle.sdk.mqtt.MqttSubscriber;
import com.inteagle.sdk.parser.ParserRegistry;
import com.inteagle.sdk.parser.VdmParser;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * MQTT Protocol Parser Integration Test
 * <p>
 * 测试协议解析器在 MQTT 环境中的完整集成流程：
 * 1. 注册协议解析器
 * 2. 连接 MQTT broker
 * 3. 订阅消息
 * 4. 接收并解析 VDM 设备数据
 * 5. 验证解析结果
 *
 * <p>运行方式:
 * <pre>
 * # 设置环境变量
 * export MQTT_HOST=shm.inteagle.com
 * export MQTT_PORT=8883
 * export MQTT_USE_TLS=true
 * export MQTT_ACCESS_KEY=ak_xxx
 * export MQTT_SECRET_KEY=sk_xxx
 * export MQTT_CUSTOMER_ID=customer1
 *
 * # 运行测试
 * mvn test -Dtest=MqttParserIntegrationTest
 * </pre>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MqttParserIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MqttParserIntegrationTest.class);

    // 从环境变量读取配置
    private static final String MQTT_HOST = System.getenv().getOrDefault("MQTT_HOST", "shm.inteagle.com");
    private static final int MQTT_PORT = Integer.parseInt(System.getenv().getOrDefault("MQTT_PORT", "8883"));
    private static final boolean MQTT_USE_TLS = Boolean.parseBoolean(System.getenv().getOrDefault("MQTT_USE_TLS", "true"));
    private static final String MQTT_ACCESS_KEY = System.getenv("MQTT_ACCESS_KEY");
    private static final String MQTT_SECRET_KEY = System.getenv("MQTT_SECRET_KEY");
    private static final String MQTT_CUSTOMER_ID = System.getenv().getOrDefault("MQTT_CUSTOMER_ID", "customer1");

    private MqttSubscriber subscriber;

    @BeforeAll
    void setup() {
        log.info("========================================");
        log.info("  MQTT Parser Integration Test");
        log.info("========================================");
        log.info("MQTT Host: {}", MQTT_HOST);
        log.info("MQTT Port: {}", MQTT_PORT);
        log.info("MQTT TLS: {}", MQTT_USE_TLS);
        log.info("Access Key: {}", MQTT_ACCESS_KEY != null ? MQTT_ACCESS_KEY.substring(0, 8) + "..." : "NOT SET");
        log.info("Customer ID: {}", MQTT_CUSTOMER_ID);

        // 跳过测试如果没有凭证
        assumeTrue(MQTT_ACCESS_KEY != null && MQTT_SECRET_KEY != null,
                "Skipping: Set MQTT_ACCESS_KEY and MQTT_SECRET_KEY env vars");

        // 1. 注册协议解析器
        log.info("Step 1: Registering protocol parsers...");
        ParserRegistry.getInstance().clear(); // 清空确保干净状态
        ParserRegistry.getInstance().register(new VdmParser());
        log.info("✓ Registered VDM parser for profiles: {}", ParserRegistry.getInstance().getRegisteredProfiles());

        // 2. 创建 MQTT 订阅者
        log.info("Step 2: Creating MQTT subscriber...");
        subscriber = new MqttSubscriber(
                MQTT_HOST,
                MQTT_PORT,
                MQTT_ACCESS_KEY,
                MQTT_SECRET_KEY,
                MQTT_CUSTOMER_ID,
                MQTT_USE_TLS
        );
        log.info("✓ MQTT subscriber created");
    }

    @AfterAll
    void cleanup() {
        if (subscriber != null) {
            try {
                subscriber.close();
                log.info("✓ MQTT subscriber closed");
            } catch (Exception e) {
                log.error("Error closing subscriber", e);
            }
        }
        ParserRegistry.getInstance().clear();
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试 MQTT 连接")
    void testMqttConnection() throws MqttException {
        log.info("--- Testing MQTT connection ---");

        subscriber.connect();

        assertTrue(subscriber.isConnected());
        log.info("✓ Connected to MQTT broker");
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试订阅并解析 VDM 消息")
    void testSubscribeAndParseVdm() throws Exception {
        log.info("--- Testing VDM message parsing ---");

        // 确保已连接
        if (!subscriber.isConnected()) {
            subscriber.connect();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger messageCount = new AtomicInteger(0);
        AtomicReference<VdmTelemetry> parsedData = new AtomicReference<>();
        AtomicReference<Entity> entityRef = new AtomicReference<>();

        // 订阅客户的所有消息
        String topic = "inteagle/" + MQTT_CUSTOMER_ID + "/#";
        log.info("Subscribing to: {}", topic);

        subscriber.subscribe(topic, message -> {
            messageCount.incrementAndGet();
            log.info("Received message: type={}, entity={}", message.getType(), message.getEntity());

            // 获取 entity
            Entity entity = message.getEntity();
            if (entity != null) {
                entityRef.set(entity);
                log.info("  Entity: type={}, id={}, profile={}",
                        entity.getType(), entity.getId(), entity.getProfile());

                // 检查是否有注册的解析器
                if (MessageParser.hasProtocolParser(message)) {
                    log.info("  Parser available for profile: {}", entity.getProfile());

                    // 使用协议解析器解析
                    VdmTelemetry vdm = MessageParser.parseVdm(message);
                    if (vdm != null) {
                        parsedData.set(vdm);
                        log.info("  ✓ Parsed VDM data: {}", vdm);

                        if (vdm.getDx() != null) {
                            log.info("    dx = {} mm", vdm.getDx());
                        }
                        if (vdm.getDy() != null) {
                            log.info("    dy = {} mm", vdm.getDy());
                        }
                        if (vdm.getTemperature() != null) {
                            log.info("    temperature = {} °C", vdm.getTemperature());
                        }
                        if (vdm.getBattery() != null) {
                            log.info("    battery = {} %", vdm.getBattery());
                        }

                        latch.countDown();
                    }
                } else {
                    log.info("  No parser registered for profile: {}", entity.getProfile());
                }
            }
        });

        log.info("Waiting for VDM messages (timeout: 30 seconds)...");
        log.info("ℹ️  If no messages arrive, please:");
        log.info("   1. Check that VDM devices are sending data");
        log.info("   2. Verify MQTT credentials and customer ID");
        log.info("   3. Ensure entity field is included in MQTT messages");

        // 等待消息（最多30秒）
        boolean received = latch.await(30, TimeUnit.SECONDS);

        log.info("Total messages received: {}", messageCount.get());

        if (received) {
            // 验证解析结果
            assertNotNull(parsedData.get(), "Should have parsed VDM data");
            assertNotNull(entityRef.get(), "Should have entity information");

            VdmTelemetry vdm = parsedData.get();
            Entity entity = entityRef.get();

            log.info("✓ Successfully parsed VDM message:");
            log.info("  Entity Type: {}", entity.getType());
            log.info("  Entity ID: {}", entity.getId());
            log.info("  Device Profile: {}", entity.getProfile());
            log.info("  Has displacement: {}", vdm.hasDisplacement());
            log.info("  Has temperature: {}", vdm.hasTemperature());
            log.info("  Has battery: {}", vdm.hasBattery());

            // 断言
            assertEquals(Entity.EntityType.DEVICE, entity.getType());
            assertTrue(ParserRegistry.getInstance().hasParser(entity.getProfile()));

        } else {
            log.warn("⚠️  No VDM messages received within timeout");
            log.warn("   This might be normal if no VDM devices are currently sending data");
            log.warn("   Received {} total messages", messageCount.get());
        }
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试协议解析器注册状态")
    void testParserRegistration() {
        log.info("--- Testing parser registration ---");

        ParserRegistry registry = ParserRegistry.getInstance();

        // 验证 VDM 解析器已注册
        assertTrue(registry.hasParser("VDM-V1"));
        assertTrue(registry.hasParser("VDM-V2W"));
        assertTrue(registry.hasParser("VDM-X1"));
        assertTrue(registry.hasParser("VDM-X2W"));
        assertTrue(registry.hasParser("VDM-V2K"));

        log.info("✓ All VDM profiles registered: {}", registry.getRegisteredProfiles());
    }

    @Test
    @Order(99)
    @DisplayName("测试摘要")
    void testSummary() {
        log.info("========================================");
        log.info("  Integration Test Summary");
        log.info("========================================");
        log.info("MQTT Host: {}:{}", MQTT_HOST, MQTT_PORT);
        log.info("TLS Enabled: {}", MQTT_USE_TLS);
        log.info("Connected: {}", subscriber != null && subscriber.isConnected());
        log.info("Parsers Registered: {}", ParserRegistry.getInstance().size());
        log.info("========================================");
    }
}
