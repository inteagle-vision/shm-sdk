/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.mqtt;

/**
 * Message types supported by Cloud Bridge.
 */
public enum MessageType {
    TELEMETRY("telemetry"),
    ALARM("3A"),
    EVENT("event"),
    IMAGE("image"),
    ATTRIBUTES("attributes"),
    UNKNOWN("unknown");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageType fromString(String text) {
        if (text == null) return UNKNOWN;
        for (MessageType type : MessageType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
