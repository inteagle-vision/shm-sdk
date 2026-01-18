/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.transport.http;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * HMAC-SHA256 签名认证器
 * <p>
 * 签名算法 (与服务端匹配):
 * <pre>
 * message = method + ":" + path + ":" + timestamp + ":" + nonce
 * signature = Hex(HMAC-SHA256(secretKey, message))
 * </pre>
 *
 * 请求 Headers:
 * <ul>
 *   <li>X-Access-Key: ak_xxx</li>
 *   <li>X-Timestamp: 毫秒时间戳</li>
 *   <li>X-Nonce: 随机字符串 (防重放)</li>
 *   <li>X-Signature: Hex 编码签名</li>
 *   <li>X-HTTP-Method: HTTP 方法 (GET/POST)</li>
 * </ul>
 */
public final class HmacAuthenticator {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final String accessKey;
    private final byte[] secretKeyBytes;

    /**
     * 创建认证器
     *
     * @param accessKey Access Key
     * @param secretKey Secret Key
     */
    public HmacAuthenticator(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取 Access Key
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * 生成签名
     * <p>
     * 签名格式: Hex(HMAC-SHA256(SK, "method:path:timestamp:nonce"))
     *
     * @param timestamp 时间戳 (毫秒)
     * @param nonce     随机数
     * @param method    HTTP 方法 (GET, POST 等)
     * @param path      请求路径
     * @return Hex 编码的签名
     */
    public String sign(long timestamp, String nonce, String method, String path) {
        try {
            // 签名格式: method:path:timestamp:nonce (与服务端匹配)
            String message = method + ":" + path + ":" + timestamp + ":" + nonce;
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(secretKeyBytes, HMAC_SHA256);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to compute HMAC signature", e);
        }
    }

    /**
     * 字节数组转 Hex 字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 创建签名信息
     *
     * @param method HTTP 方法
     * @param path   请求路径
     * @return 签名信息
     */
    public SignatureInfo createSignature(String method, String path) {
        long timestamp = System.currentTimeMillis();
        String nonce = generateNonce();
        String signature = sign(timestamp, nonce, method, path);
        return new SignatureInfo(accessKey, timestamp, nonce, signature);
    }

    /**
     * 生成随机 Nonce
     */
    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 签名信息
     */
    public static final class SignatureInfo {
        private final String accessKey;
        private final long timestamp;
        private final String nonce;
        private final String signature;

        public SignatureInfo(String accessKey, long timestamp, String nonce, String signature) {
            this.accessKey = accessKey;
            this.timestamp = timestamp;
            this.nonce = nonce;
            this.signature = signature;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getNonce() {
            return nonce;
        }

        public String getSignature() {
            return signature;
        }
    }
}
