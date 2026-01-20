package com.inteagle.sdk;

import com.inteagle.sdk.model.CredentialInfo;
import com.inteagle.sdk.model.MqttInfo;
import com.inteagle.sdk.mqtt.MqttSubscriber;

/**
 * MQTT 自动发现测试
 * <p>
 * 演示如何通过 API 自动获取 customerId 和 MQTT 连接信息。
 *
 * 运行方式:
 * export INTEAGLE_ACCESS_KEY=ak_sdk_test_full
 * export INTEAGLE_SECRET_KEY=sk_sdk_test_full_secret_key_2024
 * export API_ENDPOINT=http://192.168.30.100:9090
 * mvn exec:java -Dexec.mainClass="com.inteagle.sdk.MqttAutoDiscoveryTest"
 */
public class MqttAutoDiscoveryTest {

    public static void main(String[] args) throws Exception {
        String endpoint = System.getenv().getOrDefault("API_ENDPOINT", "http://192.168.30.100:9090");
        String accessKey = System.getenv().getOrDefault("INTEAGLE_ACCESS_KEY", "ak_sdk_test_full");
        String secretKey = System.getenv().getOrDefault("INTEAGLE_SECRET_KEY", "sk_sdk_test_full_secret_key_2024");

        System.out.println("=== MQTT Auto-Discovery Test ===");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("AccessKey: " + accessKey);
        System.out.println();

        // 创建客户端
        InteagleClient client = InteagleClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        // 方式1: 获取凭据信息
        System.out.println("=== 获取凭据信息 (/credential) ===");
        try {
            CredentialInfo credentialInfo = client.me();
            System.out.println("CustomerId: " + credentialInfo.getCustomerId());
            System.out.println("CredentialId: " + credentialInfo.getCredentialId());
            System.out.println("AccessKeyId: " + credentialInfo.getAccessKeyId());
        } catch (Exception e) {
            System.out.println("获取凭据信息失败: " + e.getMessage());
        }
        System.out.println();

        // 方式2: 获取 MQTT 连接信息
        System.out.println("=== 获取 MQTT 信息 (/credential/mqtt) ===");
        try {
            MqttInfo mqttInfo = client.mqttInfo();
            System.out.println("CustomerId: " + mqttInfo.getCustomerId());
            System.out.println("Broker: " + mqttInfo.getBrokerHost() + ":" + mqttInfo.getBrokerPort());
            System.out.println("TopicPrefix: " + mqttInfo.getTopicPrefix());
            System.out.println("UseTls: " + mqttInfo.isUseTls());
        } catch (Exception e) {
            System.out.println("获取 MQTT 信息失败: " + e.getMessage());
        }
        System.out.println();

        // 方式3: 使用 createMqttSubscriber() 自动获取配置
        System.out.println("=== 使用自动发现创建 MQTT 订阅器 ===");
        try (MqttSubscriber subscriber = client.createMqttSubscriber()) {
            System.out.println("Connecting...");
            subscriber.connect();
            System.out.println("Connected!");

            // 订阅遥测数据
            subscriber.subscribeTelemetry(telemetry -> {
                System.out.println("[遥测] 设备: " + telemetry.getDeviceId());
                System.out.println("  数据: " + telemetry.getValues());
            });

            System.out.println("\n等待消息 (10 秒)...");
            Thread.sleep(10000);

            System.out.println("\nDisconnecting...");
        } catch (Exception e) {
            System.out.println("MQTT 连接失败: " + e.getMessage());
            e.printStackTrace();
        }

        client.close();
        System.out.println("=== DONE ===");
    }
}
