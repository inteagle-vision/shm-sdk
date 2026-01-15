/*
 * Copyright © 2026 Inteagle Inc.
 */

/**
 * Data model classes for Inteagle Cloud Platform entities.
 *
 * <p>This package contains POJOs (Plain Old Java Objects) representing:
 * <ul>
 *   <li>{@link com.inteagle.sdk.model.Project} - Project information</li>
 *   <li>{@link com.inteagle.sdk.model.Device} - Device metadata</li>
 *   <li>{@link com.inteagle.sdk.model.MonitoringPoint} - Monitoring point details</li>
 *   <li>{@link com.inteagle.sdk.model.TelemetryValue} - Time-series telemetry data</li>
 *   <li>{@link com.inteagle.sdk.model.Alarm} - Alert and alarm information</li>
 *   <li>{@link com.inteagle.sdk.model.CredentialInfo} - Access credential metadata</li>
 * </ul>
 *
 * <p>All model classes use Jackson annotations for JSON serialization/deserialization
 * and include {@code @JsonIgnoreProperties(ignoreUnknown = true)} for forward compatibility.
 *
 * @see com.inteagle.sdk.rest.RestClient
 */
package com.inteagle.sdk.model;
