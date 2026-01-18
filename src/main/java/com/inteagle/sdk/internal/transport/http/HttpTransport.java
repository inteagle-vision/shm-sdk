/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.transport.http;

import com.inteagle.sdk.exception.*;
import com.inteagle.sdk.internal.serialization.JsonSerializer;
import com.inteagle.sdk.internal.serialization.Serializer;
import com.inteagle.sdk.internal.transport.Transport;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * HTTP 传输实现
 * <p>
 * 使用 OkHttp 发送 HTTP 请求，支持 HMAC-SHA256 签名认证。
 */
public class HttpTransport implements Transport {

    private static final Logger log = LoggerFactory.getLogger(HttpTransport.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String PROTOCOL = "HTTP";

    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final HmacAuthenticator authenticator;
    private final Serializer serializer;

    /**
     * 创建 HTTP 传输
     *
     * @param endpoint  API 端点
     * @param accessKey Access Key
     * @param secretKey Secret Key
     * @param timeout   请求超时
     */
    public HttpTransport(String endpoint, String accessKey, String secretKey, Duration timeout) {
        this.baseUrl = normalizeEndpoint(endpoint);
        this.authenticator = new HmacAuthenticator(accessKey, secretKey);
        this.serializer = new JsonSerializer();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .writeTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public <T> T get(String path, Map<String, Object> queryParams, Class<T> responseType)
            throws SdkException {
        String fullPath = buildQueryPath(path, queryParams);

        try {
            String response = doGet(fullPath);
            return serializer.deserialize(response, responseType);
        } catch (IOException e) {
            throw new ConnectionException(baseUrl + fullPath, e);
        }
    }

    @Override
    public <T> T post(String path, Object body, Class<T> responseType)
            throws SdkException {
        String bodyStr = serializer.serialize(body);

        try {
            String response = doPost(path, bodyStr);
            return serializer.deserialize(response, responseType);
        } catch (IOException e) {
            throw new ConnectionException(baseUrl + path, e);
        }
    }

    @Override
    public <T> T call(String service, String method, Object request, Class<T> responseType)
            throws SdkException {
        String path = buildRpcPath(service, method);
        String body = serializer.serialize(request);

        try {
            String response = doPost(path, body);
            return serializer.deserialize(response, responseType);
        } catch (IOException e) {
            throw new ConnectionException(baseUrl + path, e);
        }
    }

    @Override
    public <T> Stream<T> stream(String service, String method, Object request, Class<T> elementType) {
        // 流式查询由 API 实现类处理，这里提供基础支持
        // 实际的分批查询逻辑在 TelemetryApiImpl 中
        throw new UnsupportedOperationException(
                "Stream operations should be handled by API implementation classes"
        );
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void close() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    // ==================== 内部方法 ====================

    /**
     * 发送 POST 请求
     */
    private String doPost(String path, String body) throws IOException, SdkException {
        HmacAuthenticator.SignatureInfo sig = authenticator.createSignature("POST", path);

        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(RequestBody.create(body, JSON))
                .addHeader("X-Access-Key", sig.getAccessKey())
                .addHeader("X-Timestamp", String.valueOf(sig.getTimestamp()))
                .addHeader("X-Nonce", sig.getNonce())
                .addHeader("X-Signature", sig.getSignature())
                .addHeader("X-HTTP-Method", "POST")
                .addHeader("Content-Type", "application/json")
                .build();

        log.debug("POST {} with body: {}", path, body);

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("Response [{}]: {}", response.code(), responseBody);

            if (!response.isSuccessful()) {
                throw translateHttpError(response.code(), responseBody, path);
            }

            return responseBody.isEmpty() ? "{}" : responseBody;
        }
    }

    /**
     * 发送 GET 请求
     */
    private String doGet(String path) throws IOException, SdkException {
        // 签名时使用不带查询参数的路径
        String signaturePath = path.contains("?") ? path.substring(0, path.indexOf("?")) : path;
        HmacAuthenticator.SignatureInfo sig = authenticator.createSignature("GET", signaturePath);

        Request request = new Request.Builder()
                .url(baseUrl + path)
                .get()
                .addHeader("X-Access-Key", sig.getAccessKey())
                .addHeader("X-Timestamp", String.valueOf(sig.getTimestamp()))
                .addHeader("X-Nonce", sig.getNonce())
                .addHeader("X-Signature", sig.getSignature())
                .addHeader("X-HTTP-Method", "GET")
                .build();

        log.debug("GET {}", path);

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("Response [{}]: {}", response.code(), responseBody);

            if (!response.isSuccessful()) {
                throw translateHttpError(response.code(), responseBody, path);
            }

            return responseBody.isEmpty() ? "{}" : responseBody;
        }
    }

    /**
     * 构建带查询参数的路径
     */
    private String buildQueryPath(String path, Map<String, Object> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return path;
        }

        StringBuilder sb = new StringBuilder(path);
        sb.append("?");
        boolean first = true;

        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            if (!first) {
                sb.append("&");
            }
            first = false;

            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
              .append("=")
              .append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
        }

        return sb.toString();
    }

    /**
     * 转换 HTTP 错误为 SDK 异常
     */
    private SdkException translateHttpError(int statusCode, String responseBody, String path) {
        // 尝试从响应中解析错误信息
        String message = parseErrorMessage(responseBody, statusCode);

        return switch (statusCode) {
            case 401 -> AuthenticationException.invalidCredentials();
            case 403 -> new AuthenticationException("Access forbidden: " + path);
            case 404 -> new NotFoundException("Resource", path);
            case 429 -> {
                long retryAfter = parseRetryAfter(responseBody);
                yield new RateLimitException(message, retryAfter);
            }
            default -> {
                if (statusCode >= 500) {
                    yield new ServerException(statusCode, message);
                }
                yield new SdkException(statusCode, "HTTP_ERROR", message);
            }
        };
    }

    /**
     * 解析错误消息
     */
    private String parseErrorMessage(String responseBody, int statusCode) {
        try {
            // 尝试解析 JSON 错误响应
            if (responseBody.startsWith("{")) {
                // 简单解析，不依赖完整的 JSON 解析
                if (responseBody.contains("\"message\"")) {
                    int start = responseBody.indexOf("\"message\"") + 11;
                    int end = responseBody.indexOf("\"", start);
                    if (end > start) {
                        return responseBody.substring(start, end);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "HTTP " + statusCode + ": " + responseBody;
    }

    /**
     * 解析请求 ID
     */
    private String parseRequestId(String responseBody) {
        try {
            if (responseBody.contains("\"requestId\"")) {
                int start = responseBody.indexOf("\"requestId\"") + 13;
                int end = responseBody.indexOf("\"", start);
                if (end > start) {
                    return responseBody.substring(start, end);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 解析重试等待时间
     */
    private long parseRetryAfter(String responseBody) {
        try {
            if (responseBody.contains("\"retryAfter\"")) {
                int start = responseBody.indexOf("\"retryAfter\"") + 13;
                int end = responseBody.indexOf(",", start);
                if (end < 0) end = responseBody.indexOf("}", start);
                if (end > start) {
                    return Long.parseLong(responseBody.substring(start, end).trim());
                }
            }
        } catch (Exception ignored) {
        }
        return 1000; // 默认 1 秒
    }

    /**
     * 构建 RPC 风格 API 路径 (兼容旧接口)
     */
    private String buildRpcPath(String service, String method) {
        return "/r/" + service + "__" + method;
    }

    /**
     * 规范化端点 URL
     */
    private String normalizeEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "endpoint must not be null");
        return endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
    }

    /**
     * 获取序列化器 (供测试使用)
     */
    Serializer getSerializer() {
        return serializer;
    }
}
