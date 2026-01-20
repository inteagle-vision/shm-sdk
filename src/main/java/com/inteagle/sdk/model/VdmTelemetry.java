/*
 * Copyright © 2026 Inteagle Inc.
 */
package com.inteagle.sdk.model;

/**
 * VDM displacement meter telemetry data
 * <p>
 * Supports VDM-V1, VDM-V2W, VDM-X1, VDM-X2W, VDM-V2K device profiles.
 * <p>
 * All fields are optional (nullable) - a null value indicates the field was not present
 * in the telemetry payload.
 *
 * <p>Field meanings:
 * <ul>
 *   <li><b>dx, dy, dz</b>: Displacement in X, Y, Z directions (mm)</li>
 *   <li><b>dT</b>: Temperature (°C)</li>
 *   <li><b>dA</b>: Angle (degrees)</li>
 *   <li><b>dx_reset, dy_reset, dz_reset</b>: Cumulative displacement since last reset (mm)</li>
 *   <li><b>bat</b>: Battery level (0-100%)</li>
 *   <li><b>status</b>: Device status code</li>
 *   <li><b>signal</b>: Signal strength (RSSI or quality indicator)</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * VdmTelemetry telemetry = VdmTelemetry.builder()
 *     .dx(12.5)
 *     .dy(-3.2)
 *     .dT(25.0)
 *     .bat(85)
 *     .build();
 *
 * if (telemetry.getDx() != null) {
 *     System.out.println("X displacement: " + telemetry.getDx() + " mm");
 * }
 * }</pre>
 */
public class VdmTelemetry {

    private final Double dx;          // X displacement (mm)
    private final Double dy;          // Y displacement (mm)
    private final Double dz;          // Z displacement (mm)
    private final Double dT;          // Temperature (°C)
    private final Double dA;          // Angle (degrees)
    private final Double dxReset;     // Cumulative X displacement since reset
    private final Double dyReset;     // Cumulative Y displacement since reset
    private final Double dzReset;     // Cumulative Z displacement since reset
    private final Integer bat;        // Battery level (0-100%)
    private final Integer status;     // Status code
    private final Integer signal;     // Signal strength

    private VdmTelemetry(Builder builder) {
        this.dx = builder.dx;
        this.dy = builder.dy;
        this.dz = builder.dz;
        this.dT = builder.dT;
        this.dA = builder.dA;
        this.dxReset = builder.dxReset;
        this.dyReset = builder.dyReset;
        this.dzReset = builder.dzReset;
        this.bat = builder.bat;
        this.status = builder.status;
        this.signal = builder.signal;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ==================== Getters ====================

    /**
     * Get X-axis displacement in mm
     * @return displacement value, or null if not present
     */
    public Double getDx() {
        return dx;
    }

    /**
     * Get Y-axis displacement in mm
     * @return displacement value, or null if not present
     */
    public Double getDy() {
        return dy;
    }

    /**
     * Get Z-axis displacement in mm
     * @return displacement value, or null if not present
     */
    public Double getDz() {
        return dz;
    }

    /**
     * Get temperature in °C
     * @return temperature value, or null if not present
     */
    public Double getTemperature() {
        return dT;
    }

    /**
     * Get angle in degrees
     * @return angle value, or null if not present
     */
    public Double getAngle() {
        return dA;
    }

    /**
     * Get cumulative X displacement since reset in mm
     * @return displacement value, or null if not present
     */
    public Double getDxReset() {
        return dxReset;
    }

    /**
     * Get cumulative Y displacement since reset in mm
     * @return displacement value, or null if not present
     */
    public Double getDyReset() {
        return dyReset;
    }

    /**
     * Get cumulative Z displacement since reset in mm
     * @return displacement value, or null if not present
     */
    public Double getDzReset() {
        return dzReset;
    }

    /**
     * Get battery level (0-100%)
     * @return battery level, or null if not present
     */
    public Integer getBattery() {
        return bat;
    }

    /**
     * Get device status code
     * @return status code, or null if not present
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Get signal strength
     * @return signal strength, or null if not present
     */
    public Integer getSignal() {
        return signal;
    }

    // ==================== Utility Methods ====================

    /**
     * Check if displacement data is available
     * @return true if at least one of dx, dy, dz is present
     */
    public boolean hasDisplacement() {
        return dx != null || dy != null || dz != null;
    }

    /**
     * Check if reset displacement data is available
     * @return true if at least one of dx_reset, dy_reset, dz_reset is present
     */
    public boolean hasResetDisplacement() {
        return dxReset != null || dyReset != null || dzReset != null;
    }

    /**
     * Check if temperature data is available
     * @return true if temperature is present
     */
    public boolean hasTemperature() {
        return dT != null;
    }

    /**
     * Check if battery data is available
     * @return true if battery is present
     */
    public boolean hasBattery() {
        return bat != null;
    }

    /**
     * Check if battery is low (below 20%)
     * @return true if battery is below 20%, false otherwise or if battery data is unavailable
     */
    public boolean isBatteryLow() {
        return bat != null && bat < 20;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VdmTelemetry{");
        boolean first = true;

        if (dx != null) {
            sb.append("dx=").append(dx);
            first = false;
        }
        if (dy != null) {
            if (!first) sb.append(", ");
            sb.append("dy=").append(dy);
            first = false;
        }
        if (dz != null) {
            if (!first) sb.append(", ");
            sb.append("dz=").append(dz);
            first = false;
        }
        if (dT != null) {
            if (!first) sb.append(", ");
            sb.append("dT=").append(dT);
            first = false;
        }
        if (dA != null) {
            if (!first) sb.append(", ");
            sb.append("dA=").append(dA);
            first = false;
        }
        if (bat != null) {
            if (!first) sb.append(", ");
            sb.append("bat=").append(bat);
            first = false;
        }
        if (status != null) {
            if (!first) sb.append(", ");
            sb.append("status=").append(status);
            first = false;
        }
        if (signal != null) {
            if (!first) sb.append(", ");
            sb.append("signal=").append(signal);
        }

        sb.append('}');
        return sb.toString();
    }

    // ==================== Builder ====================

    public static class Builder {
        private Double dx;
        private Double dy;
        private Double dz;
        private Double dT;
        private Double dA;
        private Double dxReset;
        private Double dyReset;
        private Double dzReset;
        private Integer bat;
        private Integer status;
        private Integer signal;

        public Builder dx(Double dx) {
            this.dx = dx;
            return this;
        }

        public Builder dy(Double dy) {
            this.dy = dy;
            return this;
        }

        public Builder dz(Double dz) {
            this.dz = dz;
            return this;
        }

        public Builder temperature(Double dT) {
            this.dT = dT;
            return this;
        }

        public Builder dT(Double dT) {
            this.dT = dT;
            return this;
        }

        public Builder angle(Double dA) {
            this.dA = dA;
            return this;
        }

        public Builder dA(Double dA) {
            this.dA = dA;
            return this;
        }

        public Builder dxReset(Double dxReset) {
            this.dxReset = dxReset;
            return this;
        }

        public Builder dyReset(Double dyReset) {
            this.dyReset = dyReset;
            return this;
        }

        public Builder dzReset(Double dzReset) {
            this.dzReset = dzReset;
            return this;
        }

        public Builder battery(Integer bat) {
            this.bat = bat;
            return this;
        }

        public Builder bat(Integer bat) {
            this.bat = bat;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder signal(Integer signal) {
            this.signal = signal;
            return this;
        }

        public VdmTelemetry build() {
            return new VdmTelemetry(this);
        }
    }
}
