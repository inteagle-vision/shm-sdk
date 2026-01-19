package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.model.Device;
import com.inteagle.sdk.model.PageResult;
import com.inteagle.sdk.model.attr.AttrTypes;
import com.inteagle.sdk.model.attr.Target;
import com.inteagle.sdk.query.DeviceQuery;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class DeviceApiQuickTest {
    private static final String ENDPOINT = "https://api.shm.inteagle.com";
//    private static final String ENDPOINT = "http://localhost:8095";
    private static final String ACCESS_KEY = "ak_sdk_test_full";
    private static final String SECRET_KEY = "sk_sdk_test_full_secret_key_2024";
    private static final String PROJECT_ID = "07e190a0-f21a-11f0-858a-c903b68a232e";
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Device API Test ===");
        System.out.println("Endpoint: " + ENDPOINT);
        System.out.println("Project ID: " + PROJECT_ID);
        
        try (InteagleClient client = InteagleClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .timeout(Duration.ofSeconds(30))
                .build()) {
            
            // Test 1: Query by projectId with includeMonitoringPoints and includeAlarmRules
            System.out.println("\n=== 1. Query devices by projectId with includes ===");
            PageResult<Device> result = client.devices().list(
                DeviceQuery.builder()
//                    .projectId(PROJECT_ID)
                    .includeMonitoringPoints(true)
                    .includeAlarmRules(true)
                    .includeAttributes(true)
                    .pageSize(50)
                    .build()
            );
            System.out.println("Total: " + result.getTotal());
            for (Device d : result) {
                System.out.println("  - " + d.getName() + " [" + d.getType() + "] status=" + d.getStatus());
                if (d.getMonitoringPoints() != null) {
                    System.out.println("    MonitoringPoints: " + d.getMonitoringPoints().size());
                    for (Device.MonitoringPointRef mp : d.getMonitoringPoints()) {
                        System.out.println("      - " + mp.getName() + " (" + mp.getPointType() + ")");
                    }
                }
                if (d.getAlarmRules() != null) {
                    System.out.println("    AlarmRules: " + d.getAlarmRules().size());
                }

                // 显示 attributes - 使用新的类型安全 API
                if (d.hasAttribute("active")) {
                    Boolean active = d.getBoolAttr("active");
                    System.out.println("    active: " + active);
                }

                // 使用强类型获取 targets
                List<Target> targets = d.getAttribute("targets", AttrTypes.TARGET_LIST);
                if (targets != null && !targets.isEmpty()) {
                    System.out.println("    targets: " + targets.size() + " 个目标点");
                    for (Target t : targets) {
                        System.out.println("      - " + t.getTargetId()
                            + " [" + t.getTargetModel() + "]"
                            + " basePoint=" + t.getBasePoint()
                            + " roi=" + t.getRoi());
                    }
                }
            }
            
            System.out.println("\n=== All Tests Passed ===");
        }
    }
}
