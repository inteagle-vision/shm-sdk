/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JSON codec implementation
 * <p>
 * Default codec for encoding/decoding JSON payloads.
 * Thread-safe and suitable for concurrent use.
 *
 * <p>Usage example:
 * <pre>{@code
 * Codec codec = new JsonCodec();
 * JsonNode data = codec.decode("{\"temperature\": 25.5}");
 * double temp = data.path("temperature").asDouble();
 * }</pre>
 */
public class JsonCodec implements Codec {

    private final ObjectMapper mapper;

    /**
     * Create JsonCodec with default ObjectMapper
     */
    public JsonCodec() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Create JsonCodec with custom ObjectMapper
     *
     * @param mapper custom ObjectMapper instance
     */
    public JsonCodec(ObjectMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper cannot be null");
        }
        this.mapper = mapper;
    }

    @Override
    public JsonNode decode(byte[] payload) throws CodecException {
        if (payload == null || payload.length == 0) {
            throw new CodecException("payload cannot be null or empty");
        }

        try {
            return mapper.readTree(payload);
        } catch (IOException e) {
            throw new CodecException("Failed to decode JSON from bytes", e);
        }
    }

    @Override
    public JsonNode decode(String payload) throws CodecException {
        if (payload == null || payload.isEmpty()) {
            throw new CodecException("payload cannot be null or empty");
        }

        try {
            return mapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new CodecException("Failed to decode JSON from string", e);
        }
    }

    @Override
    public String encode(JsonNode data) throws CodecException {
        if (data == null) {
            throw new CodecException("data cannot be null");
        }

        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new CodecException("Failed to encode JSON to string", e);
        }
    }

    @Override
    public byte[] encodeToBytes(JsonNode data) throws CodecException {
        if (data == null) {
            throw new CodecException("data cannot be null");
        }

        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new CodecException("Failed to encode JSON to bytes", e);
        }
    }

    @Override
    public String getFormat() {
        return "JSON";
    }

    /**
     * Get the underlying ObjectMapper
     *
     * @return ObjectMapper instance
     */
    public ObjectMapper getMapper() {
        return mapper;
    }
}
