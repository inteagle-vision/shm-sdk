package com.inteagle.sdk.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inteagle.sdk.model.VdmTelemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VdmParserTest {

    private VdmParser parser;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        parser = new VdmParser();
        mapper = new ObjectMapper();
    }

    @Test
    void testGetSupportedProfiles() {
        Set<String> profiles = parser.getSupportedProfiles();

        assertEquals(5, profiles.size());
        assertTrue(profiles.contains("VDM-V1"));
        assertTrue(profiles.contains("VDM-V2W"));
        assertTrue(profiles.contains("VDM-X1"));
        assertTrue(profiles.contains("VDM-X2W"));
        assertTrue(profiles.contains("VDM-V2K"));
    }

    @Test
    void testSupports() {
        assertTrue(parser.supports("VDM-V1"));
        assertTrue(parser.supports("VDM-V2W"));
        assertFalse(parser.supports("UNKNOWN"));
        assertFalse(parser.supports("TILT-SENSOR"));
    }

    @Test
    void testParseBasicDisplacement() throws Exception {
        String json = "{\"dx\":12.5,\"dy\":-3.2,\"dz\":0.8}";
        JsonNode payload = mapper.readTree(json);

        VdmTelemetry telemetry = parser.parse("VDM-V1", payload);

        assertNotNull(telemetry);
        assertEquals(12.5, telemetry.getDx());
        assertEquals(-3.2, telemetry.getDy());
        assertEquals(0.8, telemetry.getDz());
        assertTrue(telemetry.hasDisplacement());
    }

    @Test
    void testParseWithTemperatureAndBattery() throws Exception {
        String json = "{\"dx\":5.0,\"dT\":25.5,\"bat\":85}";
        JsonNode payload = mapper.readTree(json);

        VdmTelemetry telemetry = parser.parse("VDM-V2W", payload);

        assertNotNull(telemetry);
        assertEquals(5.0, telemetry.getDx());
        assertEquals(25.5, telemetry.getTemperature());
        assertEquals(85, telemetry.getBattery());
        assertTrue(telemetry.hasTemperature());
        assertTrue(telemetry.hasBattery());
        assertFalse(telemetry.isBatteryLow());
    }

    @Test
    void testParseWithResetValues() throws Exception {
        String json = "{\"dx\":1.0,\"dy\":2.0,\"dx_reset\":100.5,\"dy_reset\":200.3}";
        JsonNode payload = mapper.readTree(json);

        VdmTelemetry telemetry = parser.parse("VDM-X1", payload);

        assertNotNull(telemetry);
        assertEquals(1.0, telemetry.getDx());
        assertEquals(2.0, telemetry.getDy());
        assertEquals(100.5, telemetry.getDxReset());
        assertEquals(200.3, telemetry.getDyReset());
        assertTrue(telemetry.hasResetDisplacement());
    }

    @Test
    void testParseWithAllFields() throws Exception {
        String json = "{\"dx\":10.0,\"dy\":20.0,\"dz\":30.0," +
                      "\"dT\":22.5,\"dA\":45.0," +
                      "\"dx_reset\":1000.0,\"dy_reset\":2000.0,\"dz_reset\":3000.0," +
                      "\"bat\":75,\"status\":1,\"signal\":-55}";
        JsonNode payload = mapper.readTree(json);

        VdmTelemetry telemetry = parser.parse("VDM-V2K", payload);

        assertNotNull(telemetry);
        assertEquals(10.0, telemetry.getDx());
        assertEquals(20.0, telemetry.getDy());
        assertEquals(30.0, telemetry.getDz());
        assertEquals(22.5, telemetry.getTemperature());
        assertEquals(45.0, telemetry.getAngle());
        assertEquals(1000.0, telemetry.getDxReset());
        assertEquals(2000.0, telemetry.getDyReset());
        assertEquals(3000.0, telemetry.getDzReset());
        assertEquals(75, telemetry.getBattery());
        assertEquals(1, telemetry.getStatus());
        assertEquals(-55, telemetry.getSignal());
    }

    @Test
    void testParseWithPartialData() throws Exception {
        String json = "{\"dx\":5.0,\"bat\":15}";
        JsonNode payload = mapper.readTree(json);

        VdmTelemetry telemetry = parser.parse("VDM-V1", payload);

        assertNotNull(telemetry);
        assertEquals(5.0, telemetry.getDx());
        assertNull(telemetry.getDy());
        assertNull(telemetry.getDz());
        assertNull(telemetry.getTemperature());
        assertEquals(15, telemetry.getBattery());
        assertTrue(telemetry.isBatteryLow());
    }

    @Test
    void testParseWithEmptyPayload() throws Exception {
        String json = "{}";
        JsonNode payload = mapper.readTree(json);

        VdmTelemetry telemetry = parser.parse("VDM-V1", payload);

        assertNotNull(telemetry);
        assertNull(telemetry.getDx());
        assertNull(telemetry.getDy());
        assertNull(telemetry.getDz());
        assertFalse(telemetry.hasDisplacement());
        assertFalse(telemetry.hasTemperature());
        assertFalse(telemetry.hasBattery());
    }

    @Test
    void testParseWithNullPayload() {
        assertThrows(ParserException.class, () -> {
            parser.parse("VDM-V1", null);
        });
    }

    @Test
    void testParseWithUnsupportedProfile() throws Exception {
        String json = "{\"dx\":5.0}";
        JsonNode payload = mapper.readTree(json);

        assertThrows(ParserException.class, () -> {
            parser.parse("UNKNOWN-PROFILE", payload);
        });
    }

    @Test
    void testParseWithNullProfile() throws Exception {
        String json = "{\"dx\":5.0}";
        JsonNode payload = mapper.readTree(json);

        assertThrows(ParserException.class, () -> {
            parser.parse(null, payload);
        });
    }

    @Test
    void testGetParserName() {
        assertEquals("VDM Parser", parser.getParserName());
    }
}
