package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.model.*;
import com.inteagle.sdk.query.*;
import java.time.Duration;

public class PointOnlyTest {
    public static void main(String[] args) throws Exception {
        InteagleClient client = InteagleClient.builder()
            .endpoint("https://api.shm.inteagle.com")
            .credentials("ak_sdk_test_full", "sk_sdk_test_full_secret_key_2024")
            .timeout(Duration.ofSeconds(30))
            .build();
        
        System.out.println("=== 测试监测点查询 ===");
        PageResult<MonitoringPoint> points = client.points().list(
            PointQuery.builder().pageSize(2).build()
        );
        System.out.println("监测点数量: " + points.getTotal());
        for (MonitoringPoint p : points) {
            System.out.println("   • " + p.getName() + " (pointId: " + p.getId() + ")");
            System.out.println("     deviceId: " + p.getDeviceId() + ", deviceName: " + p.getDeviceName());
        }
        
        client.close();
    }
}
