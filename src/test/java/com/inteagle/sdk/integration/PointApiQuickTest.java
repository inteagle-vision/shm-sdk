package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.model.MonitoringPoint;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.query.PointQuery;
import java.time.Duration;

public class PointApiQuickTest {
    private static final String ENDPOINT = "http://localhost:8099";
    private static final String ACCESS_KEY = "ak_sdk_test_full";
    private static final String SECRET_KEY = "sk_sdk_test_full_secret_key_2024";
    private static final String PROJECT_ID = "07e190a0-f21a-11f0-858a-c903b68a232e";
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Point API Test ===");
        System.out.println("Endpoint: " + ENDPOINT);
        System.out.println("Project ID: " + PROJECT_ID);
        
        try (InteagleClient client = InteagleClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .timeout(Duration.ofSeconds(30))
                .build()) {
            
            // Test 1: Query by projectId with includeAlarmRules
            System.out.println("\n=== 1. Query points by projectId with includes ===");
            PageResult<MonitoringPoint> result = client.points().list(
                PointQuery.builder()
                    .projectId(PROJECT_ID)
                    .includeAttributes(true)
                    .includeAlarmRules(true)
                    .pageSize(5)
                    .build()
            );
            System.out.println("Total: " + result.getTotal());
            for (MonitoringPoint p : result) {
                System.out.println("  - " + p.getName() + " [" + p.getType() + "]");
                if (p.getAlarmRules() != null) {
                    System.out.println("    AlarmRules: " + p.getAlarmRules().size());
                }
            }
            
            System.out.println("\n=== All Tests Passed ===");
        }
    }
}
