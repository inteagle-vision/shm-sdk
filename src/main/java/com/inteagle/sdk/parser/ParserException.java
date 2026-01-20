/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.parser;

/**
 * Exception thrown when protocol parsing fails
 */
public class ParserException extends Exception {

    private final String profile;

    public ParserException(String message) {
        super(message);
        this.profile = null;
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
        this.profile = null;
    }

    public ParserException(String profile, String message) {
        super(message);
        this.profile = profile;
    }

    public ParserException(String profile, String message, Throwable cause) {
        super(message, cause);
        this.profile = profile;
    }

    /**
     * Get the device profile that failed to parse
     *
     * @return profile name, or null if not specified
     */
    public String getProfile() {
        return profile;
    }

    @Override
    public String getMessage() {
        if (profile != null) {
            return "Profile '" + profile + "': " + super.getMessage();
        }
        return super.getMessage();
    }
}
