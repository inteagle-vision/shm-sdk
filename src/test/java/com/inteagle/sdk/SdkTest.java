package com.inteagle.sdk;

import com.inteagle.sdk.mqtt.BridgeMessage;

/**
 * SDK 连接测试示例
 *
 * 演示 HTTP API 和 MQTT 实时订阅的配合使用:
 * 1. 使用 REST API 查询项目/设备信息/监测点/告警历史/历史测量数据
 * 2. 使用 MQTT 订阅实时数据
 * 3. 支持自定义 Topic 订阅
 */
public class SdkTest {

    public static void main(String[] args) throws Exception {
        // 您的 Access Key 凭证
        String ak = "ak_67ef6d00edf1dff79449";
        String sk = "sk_8b925102c211d6ae3b661628dacf8df49101394c";
        String projectId = "07e190a0-f21a-11f0-858a-c903b68a232e";

        // MQTT Broker 配置
        String mqttHost = "broker.shm.inteagle.com";
        int mqttPort = 1883;

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           Inteagle Cloud SDK - 连接测试                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ MQTT:    " + mqttHost + ":" + mqttPort + "                           ║");
        System.out.println("║ AK:      " + ak + "                  ║");
        System.out.println("║ Project: " + projectId + "      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        // ========== 1. 构建客户端 ==========
        System.out.println("[1/5] Building client...");
        InteagleClient client = InteagleClient.builder()
            .apiEndpoint("https://api.shm.inteagle.com")
            .mqttEndpoint(mqttHost, mqttPort)  // 8883 端口自动启用 TLS
            .credentials(ak, sk)
            .build();

        System.out.println("      CustomerId: " + client.getCustomerId());

        // ========== 2. 使用 REST API 查询信息 (可选) ==========
        System.out.println("[2/5] Querying project info via REST API...");
        try {
            // 示例: 查询设备列表
            // List<Device> devices = client.rest().getDevices(projectId);
            // System.out.println("      Found " + devices.size() + " devices");
            System.out.println("      (REST API query skipped in this example)");
        } catch (Exception e) {
            System.out.println("      REST API not available: " + e.getMessage());
        }

        // ========== 3. 连接 MQTT ==========
        System.out.println("[3/5] Connecting to MQTT broker...");
        client.mqtt().connect();
        System.out.println("      Connected: " + client.mqtt().isConnected());

        // ========== 4. 订阅数据 ==========
        System.out.println("[4/5] Subscribing to project: " + projectId);

        // 方式1: 订阅整个项目的所有数据 (推荐)
        client.mqtt().subscribeProject(projectId, SdkTest::handleMessage);

        // 方式2: 订阅特定设备
        // String deviceId = "your-device-id";
        // client.mqtt().subscribeDevice(projectId, deviceId, msg -> {
        //     System.out.println("Device data: " + msg.getPayload());
        // });

        // 方式3: 订阅特定监测点
        // String pointId = "your-point-id";
        // client.mqtt().subscribePoint(projectId, pointId, msg -> {
        //     System.out.println("Point data: " + msg.getPayload());
        // });

        // 方式4: 自定义 Topic (高级用法)
        // String customTopic = "inteagle/" + client.getCustomerId() + "/p/" + projectId + "/alerts/#";
        // client.mqtt().subscribe(customTopic, msg -> {
        //     System.out.println("Alert: " + msg.getPayload());
        // });

        // ========== 5. 等待消息 ==========
        System.out.println("[5/5] Waiting for messages (30s)...");
        System.out.println();
        System.out.println("┌──────────────────────────────────────────────────────────────┐");
        System.out.println("│ Timestamp            │ Type       │ Device                   │");
        System.out.println("├──────────────────────────────────────────────────────────────┤");

        // 等待 30 秒接收消息
        Thread.sleep(30000);

        System.out.println("└──────────────────────────────────────────────────────────────┘");

        // ========== 关闭连接 ==========
        client.close();
        System.out.println("\nTest completed.");
    }

    /**
     * 消息处理回调 - 打印完整消息
     */
    private static void handleMessage(BridgeMessage msg) {
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("收到消息:");
        System.out.println("  Topic:     " + msg.getTopic());
        System.out.println("  Type:      " + msg.getType());
        System.out.println("  Timestamp: " + java.time.Instant.ofEpochMilli(msg.getTs()));
        System.out.println("  Payload:   " + msg.getPayload().toString());
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println();
    }
}
