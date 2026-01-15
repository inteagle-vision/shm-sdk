package com.inteagle.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryValue {
    private long ts;
    private Object value;

    public long getTs() { return ts; }
    public void setTs(long ts) { this.ts = ts; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}
