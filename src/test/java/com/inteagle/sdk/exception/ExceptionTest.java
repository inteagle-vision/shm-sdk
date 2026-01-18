/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 异常体系单元测试
 */
class ExceptionTest {

    @Test
    void testSdkException() {
        SdkException ex = new SdkException(500, "SERVER_ERROR", "Internal error");

        assertEquals(500, ex.getStatusCode());
        assertEquals("SERVER_ERROR", ex.getErrorCode());
        assertEquals("Internal error", ex.getMessage());
        assertTrue(ex.isServerError());
        assertFalse(ex.isClientError());
        assertTrue(ex.isRetryable());
    }

    @Test
    void testAuthenticationException() {
        AuthenticationException ex = AuthenticationException.invalidCredentials();

        assertEquals(401, ex.getStatusCode());
        assertEquals("AUTH_FAILED", ex.getErrorCode());
        assertTrue(ex.isClientError());
        assertFalse(ex.isRetryable());
    }

    @Test
    void testNotFoundException() {
        NotFoundException ex = NotFoundException.device("device-123");

        assertEquals(404, ex.getStatusCode());
        assertEquals("NOT_FOUND", ex.getErrorCode());
        assertEquals("Device", ex.getResourceType());
        assertEquals("device-123", ex.getResourceId());
        assertTrue(ex.getMessage().contains("device-123"));
    }

    @Test
    void testRateLimitException() {
        RateLimitException ex = new RateLimitException("Too many requests", 5000L);

        assertEquals(429, ex.getStatusCode());
        assertEquals("RATE_LIMITED", ex.getErrorCode());
        assertEquals(5000L, ex.getRetryAfterMs());
        assertEquals(5L, ex.getRetryAfterSeconds());
        assertTrue(ex.isRetryable());
    }

    @Test
    void testConnectionException() {
        ConnectionException ex = ConnectionException.refused("https://api.example.com");

        assertEquals(0, ex.getStatusCode());
        assertEquals("CONNECTION_ERROR", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("https://api.example.com"));
        assertTrue(ex.isRetryable());
    }

    @Test
    void testServerException() {
        ServerException ex = ServerException.internalError("Database error");

        assertEquals(500, ex.getStatusCode());
        assertEquals("SERVER_ERROR", ex.getErrorCode());
        assertTrue(ex.isRetryable());

        ServerException gatewayEx = ServerException.badGateway("Upstream error");
        assertEquals(502, gatewayEx.getStatusCode());
    }
}
