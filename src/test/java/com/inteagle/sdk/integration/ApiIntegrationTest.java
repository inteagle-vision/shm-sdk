/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.exception.AuthenticationException;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.Device;
import com.inteagle.sdk.model.MonitoringProject;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.model.TelemetryValue;
import com.inteagle.sdk.query.DeviceQuery;
import com.inteagle.sdk.query.ProjectQuery;
import com.inteagle.sdk.query.TelemetryQuery;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * REST API 集成测试
 * <p>
 * 测试 SDK 与真实 API 的连接。
 * <p>
 * 运行方式:
 * <pre>
 * # 设置环境变量
 * export INTEAGLE_API_ENDPOINT=https://api.shm.inteagle.com
 * export INTEAGLE_ACCESS_KEY=ak_xxx
 * export INTEAGLE_SECRET_KEY=sk_xxx
 *
 * # 运行测试
 * mvn test -Dtest=ApiIntegrationTest
 * </pre>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ApiIntegrationTest.class);

    // 从环境变量读取配置
    private static final String ENDPOINT = System.getenv().getOrDefault(
            "INTEAGLE_API_ENDPOINT", "https://api.shm.inteagle.com");
    private static final String ACCESS_KEY = System.getenv("INTEAGLE_ACCESS_KEY");
    private static final String SECRET_KEY = System.getenv("INTEAGLE_SECRET_KEY");

    private InteagleClient client;

    @BeforeAll
    void setup() {
        log.info("========================================");
        log.info("  Inteagle SDK Integration Test");
        log.info("========================================");
        log.info("Endpoint: {}", ENDPOINT);
        log.info("Access Key: {}", ACCESS_KEY != null ? ACCESS_KEY.substring(0, 8) + "..." : "NOT SET");

        // 跳过测试如果没有凭证
        assumeTrue(ACCESS_KEY != null && SECRET_KEY != null,
                "Skipping: Set INTEAGLE_ACCESS_KEY and INTEAGLE_SECRET_KEY env vars");

        client = InteagleClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .timeout(Duration.ofSeconds(30))
                .build();

        log.info("✓ Client created, protocol: {}", client.getProtocol());
    }

    @AfterAll
    void cleanup() {
        if (client != null) {
            client.close();
            log.info("✓ Client closed");
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. 测试项目列表查询")
    void testListProjects() throws SdkException {
        log.info("--- Testing project list ---");

        PageResult<MonitoringProject> result = client.projects().list(
                ProjectQuery.builder()
                        .pageSize(10)
                        .build()
        );

        log.info("✓ Projects found: {} (total: {})", result.size(), result.getTotal());

        for (MonitoringProject project : result) {
            log.info("  - {} ({})", project.getName(), project.getId());
        }

        assertNotNull(result);
    }

    @Test
    @Order(2)
    @DisplayName("2. 测试设备列表查询")
    void testListDevices() throws SdkException {
        log.info("--- Testing device list ---");

        PageResult<Device> result = client.devices().list(
                DeviceQuery.builder()
                        .pageSize(10)
                        .build()
        );

        log.info("✓ Devices found: {} (total: {})", result.size(), result.getTotal());

        for (Device device : result) {
            log.info("  - {} [{}] status={}", device.getName(), device.getType(), device.getStatus());
        }

        assertNotNull(result);
    }

    @Test
    @Order(3)
    @DisplayName("3. 测试遥测数据查询")
    void testQueryTelemetry() throws SdkException {
        log.info("--- Testing telemetry query ---");

        // 先获取一个设备
        PageResult<Device> devices = client.devices().list(
                DeviceQuery.builder().pageSize(1).build()
        );

        if (devices.isEmpty()) {
            log.info("ℹ No devices found, skipping telemetry test");
            return;
        }

        Device device = devices.getData().get(0);
        log.info("Testing telemetry for device: {}", device.getName());

        // 查询最近 1 小时的温度数据
        Map<String, List<TelemetryValue>> telemetry = client.telemetry().list(
                TelemetryQuery.builder()
                        .entityId(device.getDeviceId())
                        .keys("temperature")
                        .lastHours(1)
                        .limit(10)
                        .build()
        );

        log.info("✓ Telemetry keys: {}", telemetry.keySet());

        for (Map.Entry<String, List<TelemetryValue>> entry : telemetry.entrySet()) {
            log.info("  - {}: {} values", entry.getKey(), entry.getValue().size());
            if (!entry.getValue().isEmpty()) {
                TelemetryValue latest = entry.getValue().get(0);
                log.info("    Latest: {} @ {}", latest.getValue(), latest.getTs());
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("4. 测试告警统计")
    void testAlarmStatistics() throws SdkException {
        log.info("--- Testing alarm statistics ---");

        var stats = client.alarms().getStatistics();

        log.info("✓ Alarm statistics:");
        log.info("  - Total: {}", stats.getTotal());
        log.info("  - Active: {}", stats.getActive());
        log.info("  - Critical: {}", stats.getCritical());
        log.info("  - Major: {}", stats.getMajor());

        assertNotNull(stats);
    }

    @Test
    @Order(5)
    @DisplayName("5. 测试认证失败")
    void testAuthenticationFailure() {
        log.info("--- Testing authentication failure ---");

        InteagleClient badClient = InteagleClient.builder()
                .endpoint(ENDPOINT)
                .credentials("invalid_ak", "invalid_sk")
                .build();

        try {
            assertThrows(AuthenticationException.class, () -> {
                badClient.projects().list(ProjectQuery.builder().build());
            });
            log.info("✓ AuthenticationException thrown as expected");
        } finally {
            badClient.close();
        }
    }

    @Test
    @Order(99)
    @DisplayName("测试摘要")
    void testSummary() {
        log.info("========================================");
        log.info("  Integration Test Complete");
        log.info("========================================");
        log.info("Endpoint: {}", ENDPOINT);
        log.info("Protocol: {}", client != null ? client.getProtocol() : "N/A");
        log.info("========================================");
    }
}
