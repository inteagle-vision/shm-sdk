/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for protocol parsers
 * <p>
 * Manages a collection of ProtocolParser instances and routes parsing requests
 * to the appropriate parser based on device profile.
 * <p>
 * Thread-safe and suitable for concurrent use.
 *
 * <p>Usage example:
 * <pre>{@code
 * // Register parsers
 * ParserRegistry registry = ParserRegistry.getInstance();
 * registry.register(new VdmParser());
 * registry.register(new TiltParser());
 *
 * // Parse data
 * ProtocolParser<?> parser = registry.getParser("VDM-V1");
 * Object telemetry = parser.parse("VDM-V1", payload);
 * }</pre>
 */
public class ParserRegistry {

    private static final Logger log = LoggerFactory.getLogger(ParserRegistry.class);
    private static final ParserRegistry INSTANCE = new ParserRegistry();

    // Map: profile -> parser
    private final Map<String, ProtocolParser<?>> profileParsers = new ConcurrentHashMap<>();

    private ParserRegistry() {
        // Singleton
    }

    /**
     * Get the singleton instance
     *
     * @return ParserRegistry instance
     */
    public static ParserRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a protocol parser
     * <p>
     * The parser will be associated with all profiles it supports.
     * If a profile is already registered, the new parser will replace the old one.
     *
     * @param parser the parser to register
     * @param <T> telemetry data type
     */
    public <T> void register(ProtocolParser<T> parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser cannot be null");
        }

        Set<String> profiles = parser.getSupportedProfiles();
        if (profiles == null || profiles.isEmpty()) {
            log.warn("Parser {} supports no profiles, skipping registration", parser.getParserName());
            return;
        }

        for (String profile : profiles) {
            if (profile == null || profile.isEmpty()) {
                log.warn("Skipping null or empty profile in parser {}", parser.getParserName());
                continue;
            }

            ProtocolParser<?> existing = profileParsers.put(profile, parser);
            if (existing != null && !existing.equals(parser)) {
                log.info("Replaced parser for profile '{}': {} -> {}",
                        profile, existing.getParserName(), parser.getParserName());
            } else {
                log.info("Registered parser for profile '{}': {}",
                        profile, parser.getParserName());
            }
        }
    }

    /**
     * Unregister a protocol parser
     * <p>
     * Removes the parser for all profiles it supports.
     *
     * @param parser the parser to unregister
     * @param <T> telemetry data type
     */
    public <T> void unregister(ProtocolParser<T> parser) {
        if (parser == null) {
            return;
        }

        Set<String> profiles = parser.getSupportedProfiles();
        if (profiles == null) {
            return;
        }

        for (String profile : profiles) {
            ProtocolParser<?> current = profileParsers.get(profile);
            if (current != null && current.equals(parser)) {
                profileParsers.remove(profile);
                log.info("Unregistered parser for profile '{}': {}", profile, parser.getParserName());
            }
        }
    }

    /**
     * Get parser for a specific device profile
     *
     * @param profile device profile name
     * @return parser instance, or null if no parser is registered for the profile
     */
    public ProtocolParser<?> getParser(String profile) {
        if (profile == null || profile.isEmpty()) {
            return null;
        }
        return profileParsers.get(profile);
    }

    /**
     * Check if a parser is registered for a profile
     *
     * @param profile device profile name
     * @return true if a parser is registered, false otherwise
     */
    public boolean hasParser(String profile) {
        return profile != null && profileParsers.containsKey(profile);
    }

    /**
     * Get all registered device profiles
     *
     * @return set of profile names
     */
    public Set<String> getRegisteredProfiles() {
        return Set.copyOf(profileParsers.keySet());
    }

    /**
     * Clear all registered parsers
     * <p>
     * Useful for testing or reconfiguration.
     */
    public void clear() {
        profileParsers.clear();
        log.info("Cleared all registered parsers");
    }

    /**
     * Get the number of registered profiles
     *
     * @return profile count
     */
    public int size() {
        return profileParsers.size();
    }
}
