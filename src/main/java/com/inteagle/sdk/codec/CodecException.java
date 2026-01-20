/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.codec;

/**
 * Exception thrown when codec operations fail
 */
public class CodecException extends Exception {

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecException(Throwable cause) {
        super(cause);
    }
}
