package com.inteagle.sdk.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MqttSubscriberAuthTest {

    private static final String ACCESS_KEY = "test-access-key";
    private static final String SECRET_KEY = "test-secret-key";
    private static final String CUSTOMER_ID = "test-customer-id";

    @Test
    void createConnectOptionsGeneratesFreshSignedPassword() throws Exception {
        MqttSubscriber subscriber = new MqttSubscriber(
                "127.0.0.1",
                1883,
                ACCESS_KEY,
                SECRET_KEY,
                CUSTOMER_ID,
                false
        );

        MqttConnectOptions first = subscriber.createConnectOptions(ACCESS_KEY);
        Thread.sleep(2);
        MqttConnectOptions second = subscriber.createConnectOptions(ACCESS_KEY);

        String firstPassword = new String(first.getPassword());
        String secondPassword = new String(second.getPassword());

        assertEquals(ACCESS_KEY, first.getUserName());
        assertFalse(first.isAutomaticReconnect());
        assertNotEquals(firstPassword, secondPassword);
        assertValidPassword(firstPassword);
        assertValidPassword(secondPassword);
    }

    private static void assertValidPassword(String password) throws Exception {
        String[] parts = password.split(":", 2);
        assertEquals(2, parts.length);
        assertEquals(hmacSha256(SECRET_KEY, parts[0] + ":" + ACCESS_KEY), parts[1]);
    }

    private static String hmacSha256(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
