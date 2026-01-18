/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.internal.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonSerializer 单元测试
 */
class JsonSerializerTest {

    private JsonSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JsonSerializer();
    }

    @Test
    void testSerializeMap() {
        Map<String, Object> data = Map.of(
                "name", "test",
                "value", 123
        );

        String json = serializer.serialize(data);

        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"test\""));
        assertTrue(json.contains("123"));
    }

    @Test
    void testDeserialize() {
        String json = "{\"name\":\"device-001\",\"active\":true}";

        TestDevice device = serializer.deserialize(json, TestDevice.class);

        assertEquals("device-001", device.name);
        assertEquals(true, device.active);
    }

    @Test
    void testDeserializeList() {
        String json = "[{\"name\":\"a\"},{\"name\":\"b\"}]";

        List<TestDevice> list = serializer.deserializeList(json, TestDevice.class);

        assertEquals(2, list.size());
        assertEquals("a", list.get(0).name);
        assertEquals("b", list.get(1).name);
    }

    @Test
    void testSerializeNull() {
        assertEquals("null", serializer.serialize(null));
    }

    @Test
    void testDeserializeNull() {
        assertNull(serializer.deserialize((String) null, TestDevice.class));
        assertNull(serializer.deserialize("", TestDevice.class));
        assertNull(serializer.deserialize("null", TestDevice.class));
    }

    @Test
    void testDeserializeEmptyList() {
        List<TestDevice> list = serializer.deserializeList(null, TestDevice.class);
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetFormat() {
        assertEquals("JSON", serializer.getFormat());
    }

    @Test
    void testParseTree() {
        String json = "{\"key\":\"value\"}";
        var node = serializer.parseTree(json);

        assertTrue(node.has("key"));
        assertEquals("value", node.get("key").asText());
    }

    // 测试用的简单类
    public static class TestDevice {
        public String name;
        public Boolean active;
    }
}
