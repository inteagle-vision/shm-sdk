package com.inteagle.sdk;

import com.inteagle.sdk.mqtt.MqttSubscriber;

/**
 * MQTT 实时连接测试
 */
public class MqttLiveTest {
    public static void main(String[] args) throws Exception {
        String host = "broker.shm.inteagle.com";
        int port = 1883;
        String accessKeyId = System.getenv().getOrDefault("INTEAGLE_ACCESS_KEY", "ak_test123456789012");
        String accessKeySecret = System.getenv().getOrDefault("INTEAGLE_SECRET_KEY", "sk_test1234567890123456789012345678901234567890");
        String customerId = "test-customer";
        
        System.out.println("=== MQTT Live Test ===");
        System.out.println("Broker: " + host + ":" + port);
        System.out.println("AccessKey: " + accessKeyId);
        System.out.println();
        
        try (MqttSubscriber subscriber = new MqttSubscriber(host, port, accessKeyId, accessKeySecret, customerId, false)) {
            System.out.println("Connecting...");
            subscriber.connect();
            System.out.println("Connected!");
            
            // 订阅
            String topic = "cloud/telemetry/#";
            System.out.println("Subscribing to: " + topic);
            subscriber.subscribe(topic, msg -> {
                System.out.println("Received: " + msg.getTopic() + " -> " + msg.getRawPayload());
            });
            System.out.println("Subscribed!");
            
            System.out.println("Waiting for messages (5 seconds)...");
            Thread.sleep(5000);
            
            System.out.println("Disconnecting...");
        }
        
        System.out.println("=== SUCCESS ===");
    }
}
