package com.inteagle.sdk.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testFromJsonNode() throws Exception {
        String json = "{\"type\":\"DEVICE\",\"id\":\"550e8400-e29b-41d4-a716-446655440000\",\"profile\":\"VDM-V1\"}";
        JsonNode node = mapper.readTree(json);

        Entity entity = Entity.from(node);

        assertNotNull(entity);
        assertEquals(Entity.EntityType.DEVICE, entity.getType());
        assertEquals("550e8400-e29b-41d4-a716-446655440000", entity.getId());
        assertEquals("VDM-V1", entity.getProfile());
        assertTrue(entity.isDevice());
        assertFalse(entity.isAsset());
    }

    @Test
    void testFromJsonNodeWithAsset() throws Exception {
        String json = "{\"type\":\"ASSET\",\"id\":\"asset-123\",\"profile\":\"Building\"}";
        JsonNode node = mapper.readTree(json);

        Entity entity = Entity.from(node);

        assertNotNull(entity);
        assertEquals(Entity.EntityType.ASSET, entity.getType());
        assertEquals("asset-123", entity.getId());
        assertEquals("Building", entity.getProfile());
        assertFalse(entity.isDevice());
        assertTrue(entity.isAsset());
    }

    @Test
    void testFromJsonNodeWithNull() {
        Entity entity = Entity.from(null);
        assertNull(entity);
    }

    @Test
    void testFromJsonNodeWithMissingFields() throws Exception {
        String json = "{\"type\":\"DEVICE\"}";
        JsonNode node = mapper.readTree(json);

        Entity entity = Entity.from(node);

        assertNotNull(entity);
        assertEquals(Entity.EntityType.DEVICE, entity.getType());
        assertNull(entity.getId());
        assertNull(entity.getProfile());
    }

    @Test
    void testOfMethod() {
        Entity entity = Entity.of(Entity.EntityType.DEVICE, "device-123", "VDM-V2W");

        assertNotNull(entity);
        assertEquals(Entity.EntityType.DEVICE, entity.getType());
        assertEquals("device-123", entity.getId());
        assertEquals("VDM-V2W", entity.getProfile());
    }

    @Test
    void testOfMethodWithString() {
        Entity entity = Entity.of("ASSET", "asset-456", "Warehouse");

        assertNotNull(entity);
        assertEquals(Entity.EntityType.ASSET, entity.getType());
        assertEquals("asset-456", entity.getId());
        assertEquals("Warehouse", entity.getProfile());
    }

    @Test
    void testUnknownEntityType() throws Exception {
        String json = "{\"type\":\"UNKNOWN_TYPE\",\"id\":\"test-id\"}";
        JsonNode node = mapper.readTree(json);

        Entity entity = Entity.from(node);

        assertNotNull(entity);
        assertEquals(Entity.EntityType.UNKNOWN, entity.getType());
    }

    @Test
    void testToString() {
        Entity entity = Entity.of(Entity.EntityType.DEVICE, "device-123", "VDM-V1");
        String str = entity.toString();

        assertTrue(str.contains("DEVICE"));
        assertTrue(str.contains("device-123"));
        assertTrue(str.contains("VDM-V1"));
    }
}
