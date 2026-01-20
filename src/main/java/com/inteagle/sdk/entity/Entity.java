/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.entity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Entity information - protocol agnostic
 * <p>
 * Represents metadata about the source entity (Device or Asset).
 * Can be used across different protocols (MQTT, HTTP, gRPC, etc.).
 *
 * <p>Usage example:
 * <pre>{@code
 * Entity entity = Entity.of(
 *     Entity.EntityType.DEVICE,
 *     "550e8400-e29b-41d4-a716-446655440000",
 *     "VDM-V1"
 * );
 *
 * if (entity.isDevice()) {
 *     String profile = entity.getProfile();
 * }
 * }</pre>
 */
public class Entity {

    /**
     * Entity type enumeration
     */
    public enum EntityType {
        DEVICE("DEVICE"),
        ASSET("ASSET"),
        UNKNOWN("UNKNOWN");

        private final String value;

        EntityType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static EntityType fromString(String text) {
            if (text == null) return UNKNOWN;
            for (EntityType type : EntityType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    private final EntityType type;
    private final String id;
    private final String profile;

    private Entity(EntityType type, String id, String profile) {
        this.type = type;
        this.id = id;
        this.profile = profile;
    }

    /**
     * Create Entity from JsonNode
     *
     * @param entityNode JSON node containing entity fields (type, id, profile)
     * @return Entity instance, or null if entityNode is null or missing
     */
    public static Entity from(JsonNode entityNode) {
        if (entityNode == null || entityNode.isMissingNode() || entityNode.isNull()) {
            return null;
        }

        EntityType type = EntityType.fromString(
                getTextValue(entityNode, "type")
        );
        String id = getTextValue(entityNode, "id");
        String profile = getTextValue(entityNode, "profile");

        return new Entity(type, id, profile);
    }

    /**
     * Create Entity with explicit values
     */
    public static Entity of(EntityType type, String id, String profile) {
        return new Entity(type, id, profile);
    }

    /**
     * Create Entity with explicit values (string type)
     */
    public static Entity of(String type, String id, String profile) {
        return new Entity(EntityType.fromString(type), id, profile);
    }

    private static String getTextValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    // ==================== Getters ====================

    /**
     * Get entity type (DEVICE or ASSET)
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Get entity UUID
     */
    public String getId() {
        return id;
    }

    /**
     * Get entity profile name (Device Profile or Asset Profile)
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Check if this is a device entity
     */
    public boolean isDevice() {
        return type == EntityType.DEVICE;
    }

    /**
     * Check if this is an asset entity
     */
    public boolean isAsset() {
        return type == EntityType.ASSET;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "type=" + type +
                ", id='" + id + '\'' +
                ", profile='" + profile + '\'' +
                '}';
    }
}
