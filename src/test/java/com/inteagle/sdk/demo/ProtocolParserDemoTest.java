/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inteagle.sdk.codec.JsonCodec;
import com.inteagle.sdk.entity.Entity;
import com.inteagle.sdk.model.VdmTelemetry;
import com.inteagle.sdk.mqtt.BridgeMessage;
import com.inteagle.sdk.mqtt.MessageParser;
import com.inteagle.sdk.mqtt.MessageType;
import com.inteagle.sdk.parser.ParserRegistry;
import com.inteagle.sdk.parser.VdmParser;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Protocol Parser Demo Test
 * <p>
 * 演示协议解析器的完整使用流程，不依赖外部服务。
 * 展示如何在 MQTT、HTTP 或其他协议中使用相同的解析器。
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProtocolParserDemoTest {

    private static final Logger log = LoggerFactory.getLogger(ProtocolParserDemoTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void setup() {
        log.info("╔════════════════════════════════════════╗");
        log.info("║   Protocol Parser Demo                ║");
        log.info("╚════════════════════════════════════════╝");

        // 注册协议解析器（应用启动时执行一次）
        ParserRegistry.getInstance().clear();
        ParserRegistry.getInstance().register(new VdmParser());

        log.info("✓ Registered VDM parser");
    }

    @Test
    @Order(1)
    @DisplayName("演示 1: 基础编解码")
    void demo1_BasicCodec() throws Exception {
        log.info("\n📖 Demo 1: 基础编解码 (Protocol-Agnostic)");
        log.info("──────────────────────────────────────");

        // 创建 JSON 编解码器（可用于任何协议）
        JsonCodec codec = new JsonCodec();

        // 编码
        String json = "{\"dx\":12.5,\"dy\":-3.2,\"dT\":25.0,\"bat\":85}";
        JsonNode payload = codec.decode(json);

        log.info("JSON Input: {}", json);
        log.info("✓ Decoded to JsonNode");

        // 再次编码
        String encoded = codec.encode(payload);
        log.info("✓ Encoded back: {}", encoded);

        assertNotNull(payload);
        assertEquals(12.5, payload.get("dx").asDouble());
    }

    @Test
    @Order(2)
    @DisplayName("演示 2: VDM 协议解析")
    void demo2_VdmParsing() throws Exception {
        log.info("\n📖 Demo 2: VDM 协议解析 (Protocol-Agnostic)");
        log.info("──────────────────────────────────────");

        // 模拟 VDM 设备数据
        String json = """
                {
                    "dx": 12.5,
                    "dy": -3.2,
                    "dz": 0.8,
                    "dT": 25.5,
                    "bat": 85,
                    "signal": -55
                }
                """;

        JsonNode payload = mapper.readTree(json);

        // 使用 VDM 解析器（可在任何协议中使用）
        VdmParser parser = new VdmParser();
        VdmTelemetry telemetry = parser.parse("VDM-V1", payload);

        log.info("Device Profile: VDM-V1");
        log.info("Parsed telemetry:");
        log.info("  • dx = {} mm", telemetry.getDx());
        log.info("  • dy = {} mm", telemetry.getDy());
        log.info("  • dz = {} mm", telemetry.getDz());
        log.info("  • temperature = {} °C", telemetry.getTemperature());
        log.info("  • battery = {} %", telemetry.getBattery());
        log.info("  • signal = {} dBm", telemetry.getSignal());

        // 验证
        assertEquals(12.5, telemetry.getDx());
        assertEquals(-3.2, telemetry.getDy());
        assertEquals(25.5, telemetry.getTemperature());
        assertEquals(85, telemetry.getBattery());

        log.info("✓ VDM data parsed successfully");
    }

    @Test
    @Order(3)
    @DisplayName("演示 3: 解析器注册中心")
    void demo3_ParserRegistry() {
        log.info("\n📖 Demo 3: 解析器注册中心 (Protocol-Agnostic)");
        log.info("──────────────────────────────────────");

        ParserRegistry registry = ParserRegistry.getInstance();

        log.info("Registered profiles:");
        for (String profile : registry.getRegisteredProfiles()) {
            log.info("  • {}", profile);
        }

        // 检查特定 profile
        boolean hasVdmV1 = registry.hasParser("VDM-V1");
        boolean hasVdmV2W = registry.hasParser("VDM-V2W");
        boolean hasUnknown = registry.hasParser("UNKNOWN-DEVICE");

        log.info("\nProfile availability:");
        log.info("  • VDM-V1: {}", hasVdmV1 ? "✓" : "✗");
        log.info("  • VDM-V2W: {}", hasVdmV2W ? "✓" : "✗");
        log.info("  • UNKNOWN-DEVICE: {}", hasUnknown ? "✓" : "✗");

        assertTrue(hasVdmV1);
        assertTrue(hasVdmV2W);
        assertFalse(hasUnknown);

        log.info("✓ Parser registry working correctly");
    }

    @Test
    @Order(4)
    @DisplayName("演示 4: MQTT 场景 - 带 Entity 的消息")
    void demo4_MqttScenario() throws Exception {
        log.info("\n📖 Demo 4: MQTT 使用场景");
        log.info("──────────────────────────────────────");

        // 模拟 MQTT 消息（包含 entity 字段）
        String mqttPayload = """
                {
                    "type": "telemetry",
                    "ts": 1737381600000,
                    "entity": {
                        "type": "DEVICE",
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "profile": "VDM-V1"
                    },
                    "payload": {
                        "dx": 10.0,
                        "dy": 20.0,
                        "dT": 22.5,
                        "bat": 75
                    }
                }
                """;

        JsonNode json = mapper.readTree(mqttPayload);

        // 解析 entity
        Entity entity = Entity.from(json.get("entity"));

        log.info("MQTT Message:");
        log.info("  • Type: {}", json.get("type").asText());
        log.info("  • Entity Type: {}", entity.getType());
        log.info("  • Entity ID: {}", entity.getId());
        log.info("  • Device Profile: {}", entity.getProfile());

        // 构建 BridgeMessage
        BridgeMessage message = BridgeMessage.builder()
                .topic("inteagle/customer1/d/device123")
                .type(MessageType.TELEMETRY)
                .ts(json.get("ts").asLong())
                .entity(entity)
                .payload(json.get("payload"))
                .rawPayload(mqttPayload)
                .build();

        // 使用 MessageParser 自动选择解析器
        if (MessageParser.hasProtocolParser(message)) {
            VdmTelemetry vdm = MessageParser.parseVdm(message);

            log.info("\nParsed VDM data:");
            log.info("  • dx = {} mm", vdm.getDx());
            log.info("  • dy = {} mm", vdm.getDy());
            log.info("  • temperature = {} °C", vdm.getTemperature());
            log.info("  • battery = {} %", vdm.getBattery());

            assertEquals(10.0, vdm.getDx());
            assertEquals(20.0, vdm.getDy());
        }

        log.info("✓ MQTT scenario completed");
    }

    @Test
    @Order(5)
    @DisplayName("演示 5: HTTP API 场景 - 直接使用解析器")
    void demo5_HttpApiScenario() throws Exception {
        log.info("\n📖 Demo 5: HTTP API 使用场景");
        log.info("──────────────────────────────────────");

        // 模拟 HTTP API 请求
        log.info("Simulating HTTP POST /api/telemetry/parse");

        String requestBody = """
                {
                    "deviceProfile": "VDM-X1",
                    "payload": {
                        "dx": 15.5,
                        "dy": -8.2,
                        "dA": 45.0,
                        "status": 1
                    }
                }
                """;

        JsonNode request = mapper.readTree(requestBody);
        String profile = request.get("deviceProfile").asText();
        JsonNode payload = request.get("payload");

        log.info("Request:");
        log.info("  • Profile: {}", profile);
        log.info("  • Payload: {}", payload);

        // HTTP handler 直接使用相同的解析器
        ParserRegistry registry = ParserRegistry.getInstance();
        if (registry.hasParser(profile)) {
            VdmParser parser = (VdmParser) registry.getParser(profile);
            VdmTelemetry telemetry = parser.parse(profile, payload);

            log.info("\nHTTP Response:");
            log.info("  • dx = {} mm", telemetry.getDx());
            log.info("  • dy = {} mm", telemetry.getDy());
            log.info("  • angle = {} °", telemetry.getAngle());
            log.info("  • status = {}", telemetry.getStatus());

            assertEquals(15.5, telemetry.getDx());
            assertEquals(45.0, telemetry.getAngle());

            log.info("✓ HTTP API scenario completed");
        }
    }

    @Test
    @Order(6)
    @DisplayName("演示 6: 多种设备类型")
    void demo6_MultipleDeviceTypes() throws Exception {
        log.info("\n📖 Demo 6: 多种设备类型支持");
        log.info("──────────────────────────────────────");

        String[] profiles = {"VDM-V1", "VDM-V2W", "VDM-X1", "VDM-X2W", "VDM-V2K"};

        for (String profile : profiles) {
            log.info("Testing profile: {}", profile);

            String json = "{\"dx\":5.0,\"dy\":3.0,\"dT\":20.0}";
            JsonNode payload = mapper.readTree(json);

            VdmParser parser = new VdmParser();
            VdmTelemetry telemetry = parser.parse(profile, payload);

            log.info("  ✓ Successfully parsed {} data", profile);

            assertNotNull(telemetry);
            assertEquals(5.0, telemetry.getDx());
        }

        log.info("✓ All device profiles supported");
    }

    @Test
    @Order(99)
    @DisplayName("测试总结")
    void testSummary() {
        log.info("\n╔════════════════════════════════════════╗");
        log.info("║   Demo Complete                       ║");
        log.info("╚════════════════════════════════════════╝");
        log.info("\n✅ 协议解析器特性：");
        log.info("  • Protocol-Agnostic: 可用于 MQTT, HTTP, gRPC");
        log.info("  • Type-Safe: 类型安全的数据模型");
        log.info("  • Extensible: 易于添加新设备类型");
        log.info("  • Reusable: 零代码重复");
        log.info("\n📦 Package Structure:");
        log.info("  • com.inteagle.sdk.entity - Entity 模型");
        log.info("  • com.inteagle.sdk.codec - 编解码器");
        log.info("  • com.inteagle.sdk.parser - 协议解析器");
        log.info("  • com.inteagle.sdk.model - 数据模型");
    }
}
