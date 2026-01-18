/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.exception.SdkException;
import com.inteagle.sdk.model.AlarmRule;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.AlarmRuleQuery;

import java.time.Duration;

/**
 * 告警规则 API 测试
 * <p>
 * 运行方式:
 * <pre>
 * cd inteagle-cloud-sdk
 * mvn exec:java -Dexec.mainClass="com.inteagle.sdk.integration.AlarmRuleApiTest"
 * </pre>
 */
public class AlarmRuleApiTest {

    // 测试配置
    private static final String ENDPOINT = "http://localhost:8099";
    private static final String ACCESS_KEY = "ak_sdk_test_full";
    private static final String SECRET_KEY = "sk_sdk_test_full_secret_key_2024";

    // 测试数据
    private static final String TEST_PROJECT_ID = "07e190a0-f21a-11f0-858a-c903b68a232e";
    private static final String TEST_DEVICE_ID = "081a05c0-f21a-11f0-858a-c903b68a232e";
    private static final String TEST_POINT_ID = "08e6a490-f21a-11f0-858a-c903b68a232e";

    public static void main(String[] args) {
        System.out.println("=== 告警规则 API 测试 ===");
        System.out.println("Endpoint: " + ENDPOINT);
        System.out.println("Project ID: " + TEST_PROJECT_ID);
        System.out.println();

        try (InteagleClient client = InteagleClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .timeout(Duration.ofSeconds(30))
                .build()) {

            // 测试 1: 查询项目所有告警规则
            System.out.println("=== 1. 查询项目所有告警规则 ===");
            testListAllRules(client);

            // 测试 2: 按设备类型过滤
            System.out.println("\n=== 2. 查询设备类型的告警规则 ===");
            testListDeviceRules(client);

            // 测试 3: 按监测点类型过滤
            System.out.println("\n=== 3. 查询监测点类型的告警规则 ===");
            testListPointRules(client);

            // 测试 4: 查询特定设备的告警规则
            System.out.println("\n=== 4. 查询特定设备的告警规则 ===");
            testListDeviceEntityRules(client);

            // 测试 5: 查询特定监测点的告警规则
            System.out.println("\n=== 5. 查询特定监测点的告警规则 ===");
            testListPointEntityRules(client);

            // 测试 6: 查询单个规则详情
            System.out.println("\n=== 6. 查询单个规则详情 ===");
            testGetRule(client);

            // 测试 7: 只查询启用的规则
            System.out.println("\n=== 7. 查询已启用的告警规则 ===");
            testListEnabledRules(client);

            System.out.println("\n=== 所有测试通过 ===");

        } catch (SdkException e) {
            System.err.println("SDK Error: " + e.getMessage());
            System.err.println("  Code: " + e.getStatusCode());
            System.err.println("  Error Code: " + e.getErrorCode());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testListAllRules(InteagleClient client) throws SdkException {
        PageResult<AlarmRule> result = client.alarmRules().list(
                AlarmRuleQuery.ofProject(TEST_PROJECT_ID).pageSize(20).build()
        );
        System.out.println("Total: " + result.getTotal());
        for (AlarmRule rule : result) {
            printRule(rule);
        }
    }

    private static void testListDeviceRules(InteagleClient client) throws SdkException {
        PageResult<AlarmRule> result = client.alarmRules().list(
                AlarmRuleQuery.builder()
                        .projectId(TEST_PROJECT_ID)
                        .entityType("DEVICE")
                        .build()
        );
        System.out.println("Device rules count: " + result.getTotal());
        for (AlarmRule rule : result) {
            printRule(rule);
        }
    }

    private static void testListPointRules(InteagleClient client) throws SdkException {
        PageResult<AlarmRule> result = client.alarmRules().list(
                AlarmRuleQuery.builder()
                        .projectId(TEST_PROJECT_ID)
                        .entityType("POINT")
                        .build()
        );
        System.out.println("Point rules count: " + result.getTotal());
        for (AlarmRule rule : result) {
            printRule(rule);
        }
    }

    private static void testListDeviceEntityRules(InteagleClient client) throws SdkException {
        PageResult<AlarmRule> result = client.alarmRules().list(
                AlarmRuleQuery.ofEntity(TEST_PROJECT_ID, "DEVICE", TEST_DEVICE_ID).build()
        );
        System.out.println("Device " + TEST_DEVICE_ID + " rules count: " + result.getTotal());
        for (AlarmRule rule : result) {
            printRule(rule);
        }
    }

    private static void testListPointEntityRules(InteagleClient client) throws SdkException {
        PageResult<AlarmRule> result = client.alarmRules().list(
                AlarmRuleQuery.ofEntity(TEST_PROJECT_ID, "POINT", TEST_POINT_ID).build()
        );
        System.out.println("Point " + TEST_POINT_ID + " rules count: " + result.getTotal());
        for (AlarmRule rule : result) {
            printRule(rule);
        }
    }

    private static void testGetRule(InteagleClient client) throws SdkException {
        try {
            AlarmRule rule = client.alarmRules().get(TEST_PROJECT_ID, "rule-001");
            System.out.println("Found rule:");
            printRule(rule);
        } catch (SdkException e) {
            if (e.getStatusCode() == 404) {
                System.out.println("Rule not found: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    private static void testListEnabledRules(InteagleClient client) throws SdkException {
        PageResult<AlarmRule> result = client.alarmRules().list(
                AlarmRuleQuery.builder()
                        .projectId(TEST_PROJECT_ID)
                        .enabled(true)
                        .build()
        );
        System.out.println("Enabled rules count: " + result.getTotal());
        for (AlarmRule rule : result) {
            printRule(rule);
        }
    }

    private static void printRule(AlarmRule rule) {
        System.out.println("  - [" + rule.getId() + "] " + rule.getName());
        String scope = rule.isProjectLevel() ? "项目级" : "实体级";
        System.out.println("    Scope: " + scope + ", Type: " + rule.getEntityType());
        if (rule.getEntityId() != null) {
            System.out.println("    EntityId: " + rule.getEntityId());
        }
        System.out.println("    AlarmType: " + rule.getAlarmType() + ", Enabled: " + rule.getEnabled());

        // 显示三级阈值 (3A模式)
        if (rule.getThresholds() != null && !rule.getThresholds().isEmpty()) {
            System.out.println("    Thresholds (3A):");
            for (AlarmRule.ThresholdRule t : rule.getThresholds()) {
                System.out.println("      " + t.toString());
            }
        }
    }
}
