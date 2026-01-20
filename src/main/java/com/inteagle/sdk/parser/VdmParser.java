/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.inteagle.sdk.model.VdmTelemetry;

import java.util.Set;

/**
 * Parser for VDM displacement meter telemetry data
 * <p>
 * Supports the following VDM device profiles:
 * <ul>
 *   <li>VDM-V1 - Single-axis vertical displacement meter</li>
 *   <li>VDM-V2W - Dual-axis vertical displacement meter with wireless</li>
 *   <li>VDM-X1 - Single-axis horizontal displacement meter</li>
 *   <li>VDM-X2W - Dual-axis horizontal displacement meter with wireless</li>
 *   <li>VDM-V2K - Dual-axis displacement meter with extended range</li>
 * </ul>
 *
 * <p>Parses raw JSON payload into type-safe VdmTelemetry objects.
 *
 * <p>Usage example:
 * <pre>{@code
 * VdmParser parser = new VdmParser();
 * VdmTelemetry telemetry = parser.parse("VDM-V1", payload);
 *
 * if (telemetry.getDx() != null) {
 *     System.out.println("X displacement: " + telemetry.getDx() + " mm");
 * }
 * }</pre>
 */
public class VdmParser implements ProtocolParser<VdmTelemetry> {

    private static final Set<String> SUPPORTED_PROFILES = Set.of(
            "VDM-V1",
            "VDM-V2W",
            "VDM-X1",
            "VDM-X2W",
            "VDM-V2K"
    );

    @Override
    public Set<String> getSupportedProfiles() {
        return SUPPORTED_PROFILES;
    }

    @Override
    public VdmTelemetry parse(String profile, JsonNode payload) throws ParserException {
        if (profile == null || profile.isEmpty()) {
            throw new ParserException("profile cannot be null or empty");
        }

        if (!supports(profile)) {
            throw new ParserException(profile, "Unsupported profile");
        }

        if (payload == null || payload.isNull()) {
            throw new ParserException(profile, "payload cannot be null");
        }

        try {
            VdmTelemetry.Builder builder = VdmTelemetry.builder();

            // Parse displacement fields
            if (payload.has("dx")) {
                builder.dx(getDoubleValue(payload, "dx"));
            }
            if (payload.has("dy")) {
                builder.dy(getDoubleValue(payload, "dy"));
            }
            if (payload.has("dz")) {
                builder.dz(getDoubleValue(payload, "dz"));
            }

            // Parse temperature
            if (payload.has("dT")) {
                builder.dT(getDoubleValue(payload, "dT"));
            }

            // Parse angle
            if (payload.has("dA")) {
                builder.dA(getDoubleValue(payload, "dA"));
            }

            // Parse reset displacement fields
            if (payload.has("dx_reset")) {
                builder.dxReset(getDoubleValue(payload, "dx_reset"));
            }
            if (payload.has("dy_reset")) {
                builder.dyReset(getDoubleValue(payload, "dy_reset"));
            }
            if (payload.has("dz_reset")) {
                builder.dzReset(getDoubleValue(payload, "dz_reset"));
            }

            // Parse battery
            if (payload.has("bat")) {
                builder.bat(getIntValue(payload, "bat"));
            }

            // Parse status
            if (payload.has("status")) {
                builder.status(getIntValue(payload, "status"));
            }

            // Parse signal
            if (payload.has("signal")) {
                builder.signal(getIntValue(payload, "signal"));
            }

            return builder.build();

        } catch (Exception e) {
            throw new ParserException(profile, "Failed to parse VDM telemetry", e);
        }
    }

    @Override
    public String getParserName() {
        return "VDM Parser";
    }

    /**
     * Safely extract Double value from JsonNode
     *
     * @param node JSON node
     * @param field field name
     * @return Double value, or null if field is missing/null
     */
    private Double getDoubleValue(JsonNode node, String field) {
        JsonNode valueNode = node.path(field);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        if (valueNode.isNumber()) {
            return valueNode.doubleValue();
        }
        if (valueNode.isTextual()) {
            try {
                return Double.parseDouble(valueNode.asText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Safely extract Integer value from JsonNode
     *
     * @param node JSON node
     * @param field field name
     * @return Integer value, or null if field is missing/null
     */
    private Integer getIntValue(JsonNode node, String field) {
        JsonNode valueNode = node.path(field);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        if (valueNode.isNumber()) {
            return valueNode.intValue();
        }
        if (valueNode.isTextual()) {
            try {
                return Integer.parseInt(valueNode.asText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
