package com.inteagle.sdk.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParserRegistryTest {

    private ParserRegistry registry;
    private VdmParser vdmParser;

    @BeforeEach
    void setUp() {
        registry = ParserRegistry.getInstance();
        registry.clear(); // Clear registry before each test
        vdmParser = new VdmParser();
    }

    @AfterEach
    void tearDown() {
        registry.clear(); // Clean up after each test
    }

    @Test
    void testGetInstance() {
        ParserRegistry instance1 = ParserRegistry.getInstance();
        ParserRegistry instance2 = ParserRegistry.getInstance();

        assertSame(instance1, instance2, "getInstance should return the same instance");
    }

    @Test
    void testRegisterParser() {
        registry.register(vdmParser);

        assertTrue(registry.hasParser("VDM-V1"));
        assertTrue(registry.hasParser("VDM-V2W"));
        assertTrue(registry.hasParser("VDM-X1"));
        assertTrue(registry.hasParser("VDM-X2W"));
        assertTrue(registry.hasParser("VDM-V2K"));
        assertEquals(5, registry.size());
    }

    @Test
    void testGetParser() {
        registry.register(vdmParser);

        ProtocolParser<?> parser = registry.getParser("VDM-V1");

        assertNotNull(parser);
        assertEquals("VDM Parser", parser.getParserName());
        assertSame(vdmParser, parser);
    }

    @Test
    void testGetParserForUnregisteredProfile() {
        ProtocolParser<?> parser = registry.getParser("UNKNOWN-PROFILE");

        assertNull(parser);
    }

    @Test
    void testGetParserWithNull() {
        ProtocolParser<?> parser = registry.getParser(null);

        assertNull(parser);
    }

    @Test
    void testHasParser() {
        assertFalse(registry.hasParser("VDM-V1"));

        registry.register(vdmParser);

        assertTrue(registry.hasParser("VDM-V1"));
        assertFalse(registry.hasParser("UNKNOWN"));
    }

    @Test
    void testGetRegisteredProfiles() {
        assertTrue(registry.getRegisteredProfiles().isEmpty());

        registry.register(vdmParser);

        Set<String> profiles = registry.getRegisteredProfiles();

        assertEquals(5, profiles.size());
        assertTrue(profiles.contains("VDM-V1"));
        assertTrue(profiles.contains("VDM-V2W"));
        assertTrue(profiles.contains("VDM-X1"));
        assertTrue(profiles.contains("VDM-X2W"));
        assertTrue(profiles.contains("VDM-V2K"));
    }

    @Test
    void testUnregisterParser() {
        registry.register(vdmParser);
        assertTrue(registry.hasParser("VDM-V1"));

        registry.unregister(vdmParser);

        assertFalse(registry.hasParser("VDM-V1"));
        assertFalse(registry.hasParser("VDM-V2W"));
        assertEquals(0, registry.size());
    }

    @Test
    void testClear() {
        registry.register(vdmParser);
        assertEquals(5, registry.size());

        registry.clear();

        assertEquals(0, registry.size());
        assertFalse(registry.hasParser("VDM-V1"));
    }

    @Test
    void testRegisterNullParser() {
        assertThrows(IllegalArgumentException.class, () -> {
            registry.register(null);
        });
    }

    @Test
    void testReplaceParser() {
        VdmParser parser1 = new VdmParser();
        VdmParser parser2 = new VdmParser();

        registry.register(parser1);
        ProtocolParser<?> retrieved1 = registry.getParser("VDM-V1");
        assertSame(parser1, retrieved1);

        // Register another parser for the same profiles
        registry.register(parser2);
        ProtocolParser<?> retrieved2 = registry.getParser("VDM-V1");
        assertSame(parser2, retrieved2);
        assertNotSame(parser1, retrieved2);
    }
}
