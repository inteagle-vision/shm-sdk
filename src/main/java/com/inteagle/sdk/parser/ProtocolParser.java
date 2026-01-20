/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

/**
 * Protocol parser interface for device-specific data parsing
 * <p>
 * Implementations parse raw telemetry payloads into structured, type-safe data objects
 * based on the device profile (e.g., VDM displacement meters, tilt sensors).
 * <p>
 * Each parser supports one or more device profiles and can transform generic
 * telemetry data into profile-specific data structures.
 *
 * <p>Usage example:
 * <pre>{@code
 * ProtocolParser parser = new VdmParser();
 * Set<String> profiles = parser.getSupportedProfiles(); // ["VDM-V1", "VDM-V2W", ...]
 *
 * if (parser.supports("VDM-V1")) {
 *     VdmTelemetry telemetry = (VdmTelemetry) parser.parse(payload);
 *     Double dx = telemetry.getDx();
 * }
 * }</pre>
 *
 * @param <T> The type of parsed telemetry data
 */
public interface ProtocolParser<T> {

    /**
     * Get the set of device profiles supported by this parser
     * <p>
     * Profile names should match the Device Profile names in ThingsBoard
     * (e.g., "VDM-V1", "VDM-V2W", "VDM-X1", "TILT-SENSOR").
     *
     * @return set of supported profile names
     */
    Set<String> getSupportedProfiles();

    /**
     * Check if this parser supports a specific device profile
     *
     * @param profile device profile name
     * @return true if supported, false otherwise
     */
    default boolean supports(String profile) {
        return getSupportedProfiles().contains(profile);
    }

    /**
     * Parse raw telemetry payload into typed data
     * <p>
     * The payload typically contains key-value pairs from the device.
     * The parser extracts and validates fields according to the profile specification.
     *
     * @param profile device profile name
     * @param payload raw telemetry payload from MQTT message
     * @return parsed telemetry data of type T
     * @throws ParserException if parsing fails or profile is not supported
     */
    T parse(String profile, JsonNode payload) throws ParserException;

    /**
     * Get a human-readable name for this parser
     *
     * @return parser name (e.g., "VDM Parser", "Tilt Sensor Parser")
     */
    String getParserName();
}
