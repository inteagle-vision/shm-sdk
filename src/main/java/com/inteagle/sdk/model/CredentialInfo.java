/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Credential information returned from API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialInfo {
    private String credentialId;
    private String customerId;
    private String accessKeyId;
    private String name;
    private String type;
    private List<String> scope;
    private List<String> allowedTopics;
    private String status;
    private Integer maxConnections;
    private Integer maxMessagesPerSecond;
    private Long maxMessagesPerMonth;
    private ConnectionInfo connection;

    // Getters and Setters
    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccessKeyId() { return accessKeyId; }
    public void setAccessKeyId(String accessKeyId) { this.accessKeyId = accessKeyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getScope() { return scope; }
    public void setScope(List<String> scope) { this.scope = scope; }

    public List<String> getAllowedTopics() { return allowedTopics; }
    public void setAllowedTopics(List<String> allowedTopics) { this.allowedTopics = allowedTopics; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getMaxConnections() { return maxConnections; }
    public void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }

    public Integer getMaxMessagesPerSecond() { return maxMessagesPerSecond; }
    public void setMaxMessagesPerSecond(Integer maxMessagesPerSecond) { this.maxMessagesPerSecond = maxMessagesPerSecond; }

    public Long getMaxMessagesPerMonth() { return maxMessagesPerMonth; }
    public void setMaxMessagesPerMonth(Long maxMessagesPerMonth) { this.maxMessagesPerMonth = maxMessagesPerMonth; }

    public ConnectionInfo getConnection() { return connection; }
    public void setConnection(ConnectionInfo connection) { this.connection = connection; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConnectionInfo {
        private String host;
        private Integer port;
        private String protocol;
        private Boolean tls;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }

        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }

        public Boolean getTls() { return tls; }
        public void setTls(Boolean tls) { this.tls = tls; }
    }
}
