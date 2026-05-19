package com.inteagle.sdk;

import com.inteagle.sdk.model.CredentialInfo;
import com.inteagle.sdk.mqtt.MqttSubscriber;

/**
 * MQTT 实时连接测试
 * <p>
 * 用户配置 MQTT broker 地址，SDK 自动获取 customerId。
 * <p>
 * 运行方式:
 * export INTEAGLE_ACCESS_KEY=ak_xxx
 * export INTEAGLE_SECRET_KEY=sk_xxx
 * export API_ENDPOINT=https://api.shm.inteagle.com
 * export MQTT_BROKER=broker.shm.inteagle.com
 * export MQTT_PORT=21883
 * mvn exec:java -Dexec.mainClass="com.inteagle.sdk.MqttLiveTest" -Dexec.classpathScope=test
 */
public class MqttLiveTest {
    public static void main(String[] args) throws Exception {
        // API 配置
        String endpoint = System.getenv().getOrDefault("API_ENDPOINT", "https://api.shm.inteagle.com");
        String accessKey = getenvRequired("INTEAGLE_ACCESS_KEY");
        String secretKey = getenvRequired("INTEAGLE_SECRET_KEY");

        // MQTT Broker 配置 (用户填写)
        String mqttBroker = System.getenv().getOrDefault("MQTT_BROKER", "broker.shm.inteagle.com");
        int mqttPort = Integer.parseInt(System.getenv().getOrDefault("MQTT_PORT", "21883"));
        boolean mqttUseTls = Boolean.parseBoolean(System.getenv().getOrDefault("MQTT_USE_TLS", "false"));

        System.out.println("=== MQTT Live Test ===");
        System.out.println("API Endpoint: " + endpoint);
        System.out.println("AccessKey: " + mask(accessKey));
        System.out.println("MQTT Broker: " + mqttBroker + ":" + mqttPort + " (TLS=" + mqttUseTls + ")");
        System.out.println();

        // 创建客户端，配置 API 和 MQTT broker
        InteagleClient client = InteagleClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .mqtt(mqttBroker, mqttPort)      // 用户配置 broker 地址
                .mqttUseTls(mqttUseTls)
                .build();

        // 从 API 获取 customerId
        CredentialInfo credentialInfo = client.me();
        System.out.println("=== 凭据信息 (自动获取) ===");
        System.out.println("CustomerId: " + credentialInfo.getCustomerId());
        System.out.println("CredentialId: " + credentialInfo.getCredentialId());
        System.out.println();

        // 使用自动发现创建 MQTT 订阅器 (broker 用户配置，customerId 自动获取)
        try (MqttSubscriber subscriber = client.createMqttSubscriber()) {
            System.out.println("Connecting with retry...");
            subscriber.connectWithRetry();
            System.out.println("Connected!");

            // 方式1: 订阅所有类型，使用类型过滤
            System.out.println("\n=== 订阅所有类型数据 ===");
            subscriber.subscribeAll(
                    // 遥测数据处理
                    telemetry -> {
                        System.out.println("[遥测] 设备: " + telemetry.getDeviceId());
                        System.out.println("  时间: " + telemetry.getTimestamp());
                        System.out.println("  数据: " + telemetry.getValues());
                        // 示例: 获取特定值
                        Double temp = telemetry.getDouble("temperature");
                        if (temp != null) {
                            System.out.println("  温度: " + temp + "°C");
                        }
                    },
                    // 告警数据处理
                    alarm -> {
                        System.out.println("[告警] ID: " + alarm.getId());
                        System.out.println("  类型: " + alarm.getAlarmType());
                        System.out.println("  级别: " + alarm.getSeverity());
                        System.out.println("  状态: " + alarm.getStatus());
                        if (alarm.isCritical()) {
                            System.out.println("  ⚠️ 严重告警，需要立即处理!");
                        }
                    },
                    // 事件数据处理
                    event -> {
                        System.out.println("[事件] 设备: " + event.getDeviceId());
                        System.out.println("  类型: " + event.getEventType());
                        System.out.println("  内容: " + event.getBody());
                    },
                    // 图像数据处理
                    image -> {
                        System.out.println("[图像] 设备: " + image.getDeviceId());
                        if (image.hasUrl()) {
                            System.out.println("  URL: " + image.getImageUrl());
                        } else if (image.hasBase64()) {
                            System.out.println("  Base64 数据: " + image.getBase64Data().length() + " chars");
                        }
                    }
            );

            System.out.println("\n等待消息 (30 秒)...");
            System.out.println("可以向设备发送数据来测试接收");
            Thread.sleep(30000);

            System.out.println("\nDisconnecting...");
        }

        client.close();
        System.out.println("=== SUCCESS ===");
    }

    private static String getenvRequired(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Please set " + name);
        }
        return value;
    }

    private static String mask(String value) {
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }
}
