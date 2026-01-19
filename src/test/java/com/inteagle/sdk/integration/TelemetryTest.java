/*
 * 遥测数据查询测试
 */
package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.model.TelemetryValue;
import com.inteagle.sdk.query.TelemetryQuery;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class TelemetryTest {

    public static void main(String[] args) {
        String endpoint = "http://localhost:8095";
        String accessKey = "ak_sdk_test_full";
        String secretKey = "sk_sdk_test_full_secret_key_2024";
        String entityId = "ddbd2770-f508-11f0-8ccb-0d404d4711dc";
        String[] keys = {"T_01.dx", "T_01.dy"};

        System.out.println("========================================");
        System.out.println("  遥测数据查询测试");
        System.out.println("========================================");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Entity ID: " + entityId);
        System.out.println("Keys: " + String.join(", ", keys));
        System.out.println();

        try (InteagleClient client = InteagleClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .timeout(Duration.ofSeconds(30))
                .build()) {

            System.out.println("✓ 客户端创建成功");

            // 查询最近 24 小时的数据
            System.out.println("\n查询遥测数据 (最近 24 小时)...");

            Map<String, List<TelemetryValue>> result = client.telemetry().list(
                    TelemetryQuery.builder()
                            .entityId(entityId)
                            .keys(keys)
                            .lastHours(24)
                            .limit(10)
                            .orderDesc(true)
                            .build()
            );

            System.out.println("\n========================================");
            System.out.println("  查询结果");
            System.out.println("========================================");

            if (result.isEmpty()) {
                System.out.println("⚠ 未查询到数据");
            } else {
                for (Map.Entry<String, List<TelemetryValue>> entry : result.entrySet()) {
                    String key = entry.getKey();
                    List<TelemetryValue> values = entry.getValue();

                    System.out.println("\n" + key + " (" + values.size() + " 条):");
                    for (TelemetryValue v : values) {
                        System.out.printf("  ts=%d, value=%s%n", v.getTs(), v.getValue());
                    }
                }
            }

            System.out.println("\n✓ 测试完成");

        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
