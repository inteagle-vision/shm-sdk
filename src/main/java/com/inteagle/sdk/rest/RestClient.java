/*
 * Copyright © 2024-2026 Inteagle Inc.
 */
package com.inteagle.sdk.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inteagle.sdk.exception.ApiException;
import com.inteagle.sdk.model.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * REST API client for fast data queries.
 *
 * <p>Provides methods to query projects, devices, telemetry, and alarms.
 */
public class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;
    private final String customerId;
    private final String accessToken;
    private final OkHttpClient httpClient;

    public RestClient(String baseUrl, String customerId, String accessToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.customerId = customerId;
        this.accessToken = accessToken;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Get all projects for the customer.
     */
    public List<Project> getProjects() throws ApiException {
        String url = baseUrl + "/api/v1/projects";
        JsonNode response = get(url);
        return parseList(response.get("data"), Project.class);
    }

    /**
     * Get a specific project by ID.
     */
    public Project getProject(String projectId) throws ApiException {
        String url = baseUrl + "/api/v1/projects/" + projectId;
        JsonNode response = get(url);
        return parse(response.get("data"), Project.class);
    }

    /**
     * Get all devices in a project.
     */
    public List<Device> getDevices(String projectId) throws ApiException {
        String url = baseUrl + "/api/v1/projects/" + projectId + "/devices";
        JsonNode response = get(url);
        return parseList(response.get("data"), Device.class);
    }

    /**
     * Get a specific device by ID.
     */
    public Device getDevice(String deviceId) throws ApiException {
        String url = baseUrl + "/api/v1/devices/" + deviceId;
        JsonNode response = get(url);
        return parse(response.get("data"), Device.class);
    }

    /**
     * Get monitoring points in a project.
     */
    public List<MonitoringPoint> getMonitoringPoints(String projectId) throws ApiException {
        String url = baseUrl + "/api/v1/projects/" + projectId + "/points";
        JsonNode response = get(url);
        return parseList(response.get("data"), MonitoringPoint.class);
    }

    /**
     * Get latest telemetry for devices (batch).
     * Conforms to open-api-docs: POST /api/v1/telemetry/latest
     *
     * @param entityType  Entity type: DEVICE or POINT
     * @param entityIds   Entity IDs to query
     * @param metrics     Metrics to retrieve (optional, null returns all)
     */
    public List<Map<String, Object>> getLatestTelemetry(String entityType, List<String> entityIds, List<String> metrics) throws ApiException {
        String url = baseUrl + "/api/v1/telemetry/latest";
        ObjectNode body = MAPPER.createObjectNode();
        body.put("customerId", customerId);
        body.put("entityType", entityType);
        body.set("entityIds", MAPPER.valueToTree(entityIds));
        if (metrics != null && !metrics.isEmpty()) {
            body.set("metrics", MAPPER.valueToTree(metrics));
        }
        JsonNode response = post(url, body.toString());
        return MAPPER.convertValue(response.get("data"), new TypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * Get latest telemetry for a single device (convenience method).
     */
    public Map<String, Object> getLatestTelemetry(String deviceId) throws ApiException {
        List<Map<String, Object>> results = getLatestTelemetry("DEVICE", List.of(deviceId), null);
        if (results != null && !results.isEmpty()) {
            return results.get(0);
        }
        return Map.of();
    }

    /**
     * Get telemetry history for an entity.
     * Conforms to open-api-docs: POST /api/v1/telemetry
     *
     * @param entityType  Entity type: DEVICE or POINT
     * @param entityId    Entity ID
     * @param metrics     Metrics to retrieve (optional)
     * @param startTs     Start timestamp (milliseconds)
     * @param endTs       End timestamp (milliseconds)
     */
    public Map<String, List<TelemetryValue>> getTelemetryHistory(
            String entityType, String entityId, List<String> metrics, long startTs, long endTs) throws ApiException {
        String url = baseUrl + "/api/v1/telemetry";
        ObjectNode body = MAPPER.createObjectNode();
        body.put("customerId", customerId);
        body.put("entityType", entityType);
        body.put("entityId", entityId);
        if (metrics != null && !metrics.isEmpty()) {
            body.set("metrics", MAPPER.valueToTree(metrics));
        }
        body.put("startTs", startTs);
        body.put("endTs", endTs);
        JsonNode response = post(url, body.toString());
        return MAPPER.convertValue(response.get("data").get("data"), new TypeReference<Map<String, List<TelemetryValue>>>() {});
    }

    /**
     * Get telemetry history for a device (convenience method).
     */
    public Map<String, List<TelemetryValue>> getTelemetryHistory(
            String deviceId, String keys, long startTs, long endTs) throws ApiException {
        List<String> metrics = keys != null ? List.of(keys.split(",")) : null;
        return getTelemetryHistory("DEVICE", deviceId, metrics, startTs, endTs);
    }

    /**
     * Get alarms.
     * Conforms to open-api-docs: GET /api/v1/alarms
     *
     * @param projectId     Project ID (optional)
     * @param searchStatus  Status filter: ACTIVE, CLEARED, ACK, UNACK (optional)
     */
    public List<Alarm> getAlarms(String projectId, String searchStatus) throws ApiException {
        StringBuilder url = new StringBuilder(baseUrl + "/api/v1/alarms?");
        if (projectId != null) {
            url.append("projectId=").append(projectId).append("&");
        }
        if (searchStatus != null) {
            url.append("searchStatus=").append(searchStatus).append("&");
        }
        JsonNode response = get(url.toString());
        JsonNode data = response.get("data");
        if (data.has("items")) {
            return parseList(data.get("items"), Alarm.class);
        }
        return parseList(data, Alarm.class);
    }

    /**
     * Get active alarms for a project (convenience method).
     */
    public List<Alarm> getActiveAlarms(String projectId) throws ApiException {
        return getAlarms(projectId, "ACTIVE");
    }

    /**
     * Acknowledge an alarm.
     */
    public void acknowledgeAlarm(String alarmId) throws ApiException {
        String url = baseUrl + "/api/v1/alarms/" + alarmId + "/ack";
        post(url, "{}");
    }

    /**
     * Clear an alarm.
     */
    public void clearAlarm(String alarmId) throws ApiException {
        String url = baseUrl + "/api/v1/alarms/" + alarmId + "/clear";
        post(url, "{}");
    }

    // HTTP methods

    private JsonNode get(String url) throws ApiException {
        Request request = new Request.Builder()
                .url(url)
                .header("X-Authorization", "Bearer " + accessToken)
                .get()
                .build();
        return execute(request);
    }

    private JsonNode post(String url, String body) throws ApiException {
        Request request = new Request.Builder()
                .url(url)
                .header("X-Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body, JSON))
                .build();
        return execute(request);
    }

    private JsonNode execute(Request request) throws ApiException {
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                log.error("API error: {} {} - {}", response.code(), response.message(), body);
                throw new ApiException(response.code(), "API error: " + response.message());
            }

            if (body.isEmpty()) {
                return MAPPER.createObjectNode();
            }
            return MAPPER.readTree(body);

        } catch (IOException e) {
            log.error("HTTP request failed: {}", e.getMessage());
            throw new ApiException(0, "HTTP request failed: " + e.getMessage(), e);
        }
    }

    private <T> T parse(JsonNode node, Class<T> clazz) {
        return MAPPER.convertValue(node, clazz);
    }

    private <T> List<T> parseList(JsonNode node, Class<T> clazz) {
        return MAPPER.convertValue(node, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}
