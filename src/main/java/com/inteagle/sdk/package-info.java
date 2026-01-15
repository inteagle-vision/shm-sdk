/*
 * Copyright © 2026 Inteagle Inc.
 */

/**
 * Inteagle Cloud SDK - Java client library for Inteagle Cloud Platform.
 *
 * <p>This SDK provides a unified interface for interacting with Inteagle's 
 * Structural Health Monitoring (SHM) cloud platform through:
 * <ul>
 *   <li>REST API for fast queries and data retrieval</li>
 *   <li>MQTT subscriptions for real-time data streaming</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // Create client with credentials
 * InteagleClient client = InteagleClient.builder()
 *     .apiEndpoint("https://api.shm.inteagle.com")
 *     .mqttEndpoint("broker.shm.inteagle.com", 8883)
 *     .credentials(accessKeyId, accessKeySecret)
 *     .build();
 *
 * // Query devices via REST API
 * List<Device> devices = client.rest().getDevices(projectId);
 *
 * // Subscribe to real-time data via MQTT
 * client.mqtt().connect();
 * client.mqtt().subscribeProject(projectId, message -> {
 *     System.out.println("Received: " + message.getPayload());
 * });
 *
 * // Clean up resources
 * client.close();
 * }</pre>
 *
 * <h2>Authentication</h2>
 * <p>The SDK uses Access Key ID and Secret Key (AK/SK) authentication with HMAC-SHA256 signatures.
 * Keys should be securely stored and never hardcoded in production applications.
 *
 * @see com.inteagle.sdk.InteagleClient
 * @see com.inteagle.sdk.rest.RestClient
 * @see com.inteagle.sdk.mqtt.MqttSubscriber
 */
package com.inteagle.sdk;
