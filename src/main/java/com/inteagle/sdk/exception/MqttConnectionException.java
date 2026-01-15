/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

/**
 * Exception thrown when MQTT connection or subscription operations fail.
 */
public class MqttConnectionException extends Exception {
    
    public MqttConnectionException(String message) {
        super(message);
    }

    public MqttConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
