/*
 * Copyright © 2026 Inteagle Inc.
 */

/**
 * MQTT subscription client for real-time data streaming from Inteagle Cloud Platform.
 *
 * <p>This package provides classes for subscribing to real-time data updates via MQTT:
 * <ul>
 *   <li>{@link com.inteagle.sdk.mqtt.MqttSubscriber} - Main MQTT client for subscriptions</li>
 *   <li>{@link com.inteagle.sdk.mqtt.BridgeMessage} - Message wrapper with type and payload</li>
 *   <li>{@link com.inteagle.sdk.mqtt.MessageType} - Enum of supported message types</li>
 *   <li>{@link com.inteagle.sdk.mqtt.TopicBuilder} - Utility for building MQTT topic patterns</li>
 * </ul>
 *
 * <h2>Supported Message Types</h2>
 * <ul>
 *   <li>TELEMETRY - Device sensor data and measurements</li>
 *   <li>ATTRIBUTES - Device configuration and metadata</li>
 *   <li>ALARM - Alert notifications (create, update, clear)</li>
 *   <li>EVENT - System events</li>
 *   <li>IMAGE - Camera image data</li>
 * </ul>
 *
 * <h2>Topic Format</h2>
 * <pre>
 * inteagle/{customerId}/p/{projectId}/d/{deviceId}    # Device data
 * inteagle/{customerId}/p/{projectId}/mp/{pointId}    # Monitoring point data
 * inteagle/{customerId}/p/{projectId}/#               # All project data
 * </pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * MqttSubscriber mqtt = new MqttSubscriber(
 *     host, port, accessKeyId, accessKeySecret, customerId, useTls
 * );
 * 
 * // Connect to broker
 * mqtt.connect();
 * 
 * // Subscribe to project data
 * mqtt.subscribeProject(projectId, message -> {
 *     System.out.println("Type: " + message.getType());
 *     System.out.println("Payload: " + message.getPayload());
 * });
 * 
 * // Subscribe to specific device
 * mqtt.subscribeDevice(projectId, deviceId, message -> {
 *     // Handle device messages
 * });
 * 
 * // Clean up
 * mqtt.close();
 * }</pre>
 *
 * @see com.inteagle.sdk.mqtt.MqttSubscriber
 * @see com.inteagle.sdk.mqtt.BridgeMessage
 */
package com.inteagle.sdk.mqtt;
