package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.model.AlarmRule;
import com.inteagle.sdk.model.Device;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.AlarmRuleQuery;
import com.inteagle.sdk.query.DeviceQuery;
import java.time.Duration;

public class RemoteApiTest {
    // 使用互联网地址
    private static final String ENDPOINT = "https://api.shm.inteagle.com";
    private static final String ACCESS_KEY = "ak_sdk_test_full";
    private static final String SECRET_KEY = "sk_sdk_test_full_secret_key_2024";
    private static final String PROJECT_ID = "07e190a0-f21a-11f0-858a-c903b68a232e";
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Remote API Test (via Internet) ===");
        System.out.println("Endpoint: " + ENDPOINT);
        System.out.println("Project ID: " + PROJECT_ID);
        
        try (InteagleClient client = InteagleClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .timeout(Duration.ofSeconds(30))
                .build()) {
            
            // Test 1: AlarmRule API
            System.out.println("\n=== 1. 告警规则查询 ===");
            PageResult<AlarmRule> rules = client.alarmRules().list(
                AlarmRuleQuery.ofProject(PROJECT_ID).pageSize(5).build()
            );
            System.out.println("Total: " + rules.getTotal());
            for (AlarmRule rule : rules) {
                System.out.println("  - [" + rule.getId() + "] " + rule.getName());
            }
            
            // Test 2: Device API
            System.out.println("\n=== 2. 设备查询 ===");
            PageResult<Device> devices = client.devices().list(
                DeviceQuery.builder()
                    .projectId(PROJECT_ID)
                    .includeAlarmRules(true)
                    .pageSize(5)
                    .build()
            );
            System.out.println("Total: " + devices.getTotal());
            for (Device d : devices) {
                System.out.println("  - " + d.getName() + " (AlarmRules: " + 
                    (d.getAlarmRules() != null ? d.getAlarmRules().size() : 0) + ")");
            }
            
            System.out.println("\n=== All Remote Tests Passed ===");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
