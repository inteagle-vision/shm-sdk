package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;
import com.inteagle.sdk.model.*;
import com.inteagle.sdk.query.*;
import java.time.Duration;

public class RelationQueryTest {
    public static void main(String[] args) throws Exception {
        InteagleClient client = InteagleClient.builder()
            .endpoint("https://api.shm.inteagle.com")
            .credentials("ak_sdk_test_full", "sk_sdk_test_full_secret_key_2024")
            .timeout(Duration.ofSeconds(30))
            .build();
        
        System.out.println("=== 测试监测点关联设备 ===");
        PageResult<MonitoringPoint> points = client.points().list(
            PointQuery.builder().pageSize(5).build()
        );
        System.out.println("监测点数量: " + points.getTotal());
        for (MonitoringPoint p : points) {
            System.out.println("   • " + p.getName());
            System.out.println("     pointId: " + p.getId());
            System.out.println("     deviceId: " + p.getDeviceId());
            System.out.println("     deviceName: " + p.getDeviceName());
        }
        
        System.out.println("\n=== 测试设备关联监测点 (列表查询) ===");
        PageResult<Device> devices = client.devices().list(
            DeviceQuery.builder().pageSize(10).includeMonitoringPoints(true).build()
        );
        System.out.println("设备数量: " + devices.getTotal());
        for (Device d : devices) {
            System.out.println("   • " + d.getName());
            System.out.println("     deviceId: " + d.getDeviceId());
            if (d.getMonitoringPoints() != null && !d.getMonitoringPoints().isEmpty()) {
                System.out.println("     关联监测点: " + d.getMonitoringPoints().size() + " 个");
                for (Device.MonitoringPointRef mp : d.getMonitoringPoints()) {
                    System.out.println("       - " + mp.getName() + " (" + mp.getPointId() + ")");
                }
            } else {
                System.out.println("     关联监测点: 无");
            }
        }

        // 测试有关联的设备 - A6B9CA2DF4D418C0 (已知有 Measures 关系)
        System.out.println("\n=== 测试特定设备 (A6B9CA2DF4D418C0) 关联监测点 ===");
        try {
            Device device = client.devices().get("4eeb7970-dfcd-11f0-bd75-9de672c6cc25");
            if (device != null) {
                System.out.println("   • " + device.getName());
                System.out.println("     deviceId: " + device.getDeviceId());
                if (device.getMonitoringPoints() != null && !device.getMonitoringPoints().isEmpty()) {
                    System.out.println("     关联监测点: " + device.getMonitoringPoints().size() + " 个");
                    for (Device.MonitoringPointRef mp : device.getMonitoringPoints()) {
                        System.out.println("       - " + mp.getName() + " (" + mp.getPointId() + ")");
                    }
                } else {
                    System.out.println("     关联监测点: 无");
                }
            }
        } catch (Exception e) {
            System.out.println("   获取设备失败: " + e.getMessage());
        }

        client.close();
        System.out.println("\n✅ 双向关联查询测试完成!");
    }
}
