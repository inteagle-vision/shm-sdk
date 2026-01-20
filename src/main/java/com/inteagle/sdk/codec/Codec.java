/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.codec;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Codec interface for encoding/decoding message payloads - protocol agnostic
 * <p>
 * Provides abstraction for different encoding formats (JSON, Protobuf, etc.).
 * Can be used across different protocols (MQTT, HTTP, gRPC, etc.).
 * <p>
 * Default implementation uses JSON. Future implementations can support Protobuf
 * for better performance and smaller message sizes.
 *
 * <p>Usage example:
 * <pre>{@code
 * Codec codec = new JsonCodec();
 * JsonNode data = codec.decode("{\"temperature\": 25.5}");
 * String encoded = codec.encode(data);
 * }</pre>
 */
public interface Codec {

    /**
     * Decode a byte array payload into JsonNode
     *
     * @param payload raw byte array from MQTT message
     * @return JsonNode representation of the payload
     * @throws CodecException if decoding fails
     */
    JsonNode decode(byte[] payload) throws CodecException;

    /**
     * Decode a string payload into JsonNode
     *
     * @param payload string representation of the payload
     * @return JsonNode representation of the payload
     * @throws CodecException if decoding fails
     */
    JsonNode decode(String payload) throws CodecException;

    /**
     * Encode a JsonNode into a string
     *
     * @param data JsonNode to encode
     * @return string representation
     * @throws CodecException if encoding fails
     */
    String encode(JsonNode data) throws CodecException;

    /**
     * Encode a JsonNode into a byte array
     *
     * @param data JsonNode to encode
     * @return byte array representation
     * @throws CodecException if encoding fails
     */
    byte[] encodeToBytes(JsonNode data) throws CodecException;

    /**
     * Get the codec format name
     *
     * @return codec format (e.g., "JSON", "PROTOBUF")
     */
    String getFormat();
}
