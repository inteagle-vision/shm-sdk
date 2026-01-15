package com.inteagle.sdk.mqtt;

import com.fasterxml.jackson.databind.JsonNode;

public class BridgeMessage {
    private final String topic;
    private final MessageType type;
    private final long ts;
    private final JsonNode payload;
    private final String rawPayload;

    private BridgeMessage(Builder builder) {
        this.topic = builder.topic;
        this.type = builder.type;
        this.ts = builder.ts;
        this.payload = builder.payload;
        this.rawPayload = builder.rawPayload;
    }

    public static Builder builder() { return new Builder(); }
    public String getTopic() { return topic; }
    public MessageType getType() { return type; }
    public long getTs() { return ts; }
    public JsonNode getPayload() { return payload; }
    public String getRawPayload() { return rawPayload; }

    @Override
    public String toString() {
        return "BridgeMessage{topic='" + topic + "', type=" + type + ", ts=" + ts + "}";
    }

    public static class Builder {
        private String topic;
        private MessageType type;
        private long ts;
        private JsonNode payload;
        private String rawPayload;

        public Builder topic(String topic) { this.topic = topic; return this; }
        public Builder type(MessageType type) { this.type = type; return this; }
        public Builder ts(long ts) { this.ts = ts; return this; }
        public Builder payload(JsonNode payload) { this.payload = payload; return this; }
        public Builder rawPayload(String rawPayload) { this.rawPayload = rawPayload; return this; }
        public BridgeMessage build() { return new BridgeMessage(this); }
    }
}
