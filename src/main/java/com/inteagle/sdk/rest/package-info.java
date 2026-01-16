/*
 * Copyright © 2026 Inteagle Inc.
 */

/**
 * REST API client implementation for Inteagle Cloud Platform.
 *
 * <p>This package provides the {@link com.inteagle.sdk.rest.RestClient} class
 * for making authenticated HTTP requests to query:
 * <ul>
 *   <li>Projects and project details</li>
 *   <li>Devices and monitoring points</li>
 *   <li>Telemetry data (latest and historical)</li>
 *   <li>Alarms and alert information</li>
 * </ul>
 *
 * <h2>Authentication</h2>
 * <p>All requests are authenticated using HMAC-SHA256 signatures with custom headers:
 * <ul>
 *   <li>X-Access-Key: Customer/Access Key ID</li>
 *   <li>X-Timestamp: Request timestamp</li>
 *   <li>X-Nonce: Random nonce for replay protection</li>
 *   <li>X-Signature: HMAC-SHA256 signature</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * RestClient client = new RestClient(apiEndpoint, accessKeyId, accessKeySecret);
 * 
 * // Query devices
 * List<Device> devices = client.getDevices(projectId);
 * 
 * // Get latest telemetry
 * Map<String, Object> telemetry = client.getLatestTelemetry(deviceId);
 * 
 * // Get historical data
 * Map<String, List<TelemetryValue>> history = client.getTelemetryHistory(
 *     "DEVICE", deviceId, List.of("temperature", "humidity"), 
 *     startTimestamp, endTimestamp
 * );
 * }</pre>
 *
 * @see com.inteagle.sdk.rest.RestClient
 */
package com.inteagle.sdk.rest;
