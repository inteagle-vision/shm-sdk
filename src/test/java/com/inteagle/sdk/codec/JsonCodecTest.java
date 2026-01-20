package com.inteagle.sdk.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JsonCodecTest {

    private JsonCodec codec;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        codec = new JsonCodec();
        mapper = new ObjectMapper();
    }

    @Test
    void testDecodeString() throws CodecException {
        String json = "{\"temperature\":25.5,\"humidity\":60}";

        JsonNode node = codec.decode(json);

        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals(25.5, node.path("temperature").asDouble());
        assertEquals(60, node.path("humidity").asInt());
    }

    @Test
    void testDecodeBytes() throws CodecException {
        String json = "{\"dx\":10.0,\"dy\":20.0}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        JsonNode node = codec.decode(bytes);

        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals(10.0, node.path("dx").asDouble());
        assertEquals(20.0, node.path("dy").asDouble());
    }

    @Test
    void testDecodeNullString() {
        assertThrows(CodecException.class, () -> {
            codec.decode((String) null);
        });
    }

    @Test
    void testDecodeEmptyString() {
        assertThrows(CodecException.class, () -> {
            codec.decode("");
        });
    }

    @Test
    void testDecodeNullBytes() {
        assertThrows(CodecException.class, () -> {
            codec.decode((byte[]) null);
        });
    }

    @Test
    void testDecodeEmptyBytes() {
        assertThrows(CodecException.class, () -> {
            codec.decode(new byte[0]);
        });
    }

    @Test
    void testDecodeInvalidJson() {
        assertThrows(CodecException.class, () -> {
            codec.decode("{invalid json}");
        });
    }

    @Test
    void testEncodeString() throws Exception {
        JsonNode node = mapper.readTree("{\"temperature\":25.5}");

        String encoded = codec.encode(node);

        assertNotNull(encoded);
        JsonNode decoded = mapper.readTree(encoded);
        assertEquals(25.5, decoded.path("temperature").asDouble());
    }

    @Test
    void testEncodeToBytes() throws Exception {
        JsonNode node = mapper.readTree("{\"dx\":10.0}");

        byte[] bytes = codec.encodeToBytes(node);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        String json = new String(bytes, StandardCharsets.UTF_8);
        JsonNode decoded = mapper.readTree(json);
        assertEquals(10.0, decoded.path("dx").asDouble());
    }

    @Test
    void testEncodeNull() {
        assertThrows(CodecException.class, () -> {
            codec.encode(null);
        });
    }

    @Test
    void testEncodeToBytesNull() {
        assertThrows(CodecException.class, () -> {
            codec.encodeToBytes(null);
        });
    }

    @Test
    void testGetFormat() {
        assertEquals("JSON", codec.getFormat());
    }

    @Test
    void testGetMapper() {
        ObjectMapper retrievedMapper = codec.getMapper();
        assertNotNull(retrievedMapper);
    }

    @Test
    void testCustomObjectMapper() {
        ObjectMapper customMapper = new ObjectMapper();
        JsonCodec customCodec = new JsonCodec(customMapper);

        assertSame(customMapper, customCodec.getMapper());
    }

    @Test
    void testConstructorWithNullMapper() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JsonCodec(null);
        });
    }

    @Test
    void testDecodeArray() throws CodecException {
        String json = "[1,2,3,4,5]";

        JsonNode node = codec.decode(json);

        assertNotNull(node);
        assertTrue(node.isArray());
        assertEquals(5, node.size());
        assertEquals(1, node.get(0).asInt());
        assertEquals(5, node.get(4).asInt());
    }

    @Test
    void testDecodeComplexObject() throws CodecException {
        String json = "{\"entity\":{\"type\":\"DEVICE\",\"id\":\"123\",\"profile\":\"VDM-V1\"}," +
                      "\"payload\":{\"dx\":10.0,\"dy\":20.0}}";

        JsonNode node = codec.decode(json);

        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("DEVICE", node.path("entity").path("type").asText());
        assertEquals("VDM-V1", node.path("entity").path("profile").asText());
        assertEquals(10.0, node.path("payload").path("dx").asDouble());
    }
}
