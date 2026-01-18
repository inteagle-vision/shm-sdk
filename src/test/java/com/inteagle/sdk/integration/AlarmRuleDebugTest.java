/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.integration;

import com.inteagle.sdk.InteagleClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 告警规则 Debug 测试
 */
public class AlarmRuleDebugTest {

    private static final String ENDPOINT = "http://localhost:8099";
    private static final String ACCESS_KEY = "ak_sdk_test_full";
    private static final String SECRET_KEY = "sk_sdk_test_full_secret_key_2024";

    public static void main(String[] args) throws Exception {
        System.out.println("=== 告警规则 Debug 测试 ===");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String method = "POST";
        // 1. 测试 debug 端点
        testEndpoint(client, method, "/r/AlarmRule__debug", "{}");

        // 2. 测试 list 端点
        System.out.println("\n=== Test AlarmRule__list ===");
        String listBody = "{\"projectId\":\"07e190a0-f21a-11f0-858a-c903b68a232e\"}";
        testEndpoint(client, method, "/r/AlarmRule__list", listBody);
    }

    private static void testEndpoint(HttpClient client, String method, String uri, String body) throws Exception {
        long timestamp = System.currentTimeMillis();
        String nonce = UUID.randomUUID().toString().replace("-", "");

        // 构建签名字符串: method:uri:timestamp:nonce
        String signString = method + ":" + uri + ":" + timestamp + ":" + nonce;
        String signature = hmacSha256(signString, SECRET_KEY);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT + uri))
                .header("Content-Type", "application/json")
                .header("X-Access-Key", ACCESS_KEY)
                .header("X-Timestamp", String.valueOf(timestamp))
                .header("X-Nonce", nonce)
                .header("X-Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("URI: " + uri);
        System.out.println("Body: " + body);
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }

    private static String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
