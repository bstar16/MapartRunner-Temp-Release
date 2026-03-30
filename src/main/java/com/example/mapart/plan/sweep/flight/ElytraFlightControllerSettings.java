package com.example.mapart.plan.sweep.flight;

public record ElytraFlightControllerSettings(
        double minAltitude,
        double maxAltitude,
        double laneEntryCenterTolerance,
        double endpointApproachDistance,
        int takeoffTimeoutTicks,
        int laneEntryTimeoutTicks,
        int endpointApproachTimeoutTicks,
        int maxRecoveryAttempts
) {
    public ElytraFlightControllerSettings {
        if (Double.isNaN(minAltitude) || Double.isNaN(maxAltitude) || minAltitude > maxAltitude) {
            throw new IllegalArgumentException("minAltitude must be <= maxAltitude and not NaN");
        }
        if (laneEntryCenterTolerance < 0.0 || Double.isNaN(laneEntryCenterTolerance)) {
            throw new IllegalArgumentException("laneEntryCenterTolerance must be >= 0");
        }
        if (endpointApproachDistance < 0.0 || Double.isNaN(endpointApproachDistance)) {
            throw new IllegalArgumentException("endpointApproachDistance must be >= 0");
        }
        if (takeoffTimeoutTicks <= 0 || laneEntryTimeoutTicks <= 0 || endpointApproachTimeoutTicks <= 0) {
            throw new IllegalArgumentException("timeouts must be > 0");
        }
        if (maxRecoveryAttempts < 0) {
            throw new IllegalArgumentException("maxRecoveryAttempts must be >= 0");
        }
    }

    public static ElytraFlightControllerSettings defaults() {
        return new ElytraFlightControllerSettings(65.0, 80.0, 0.75, 6.0, 40, 40, 60, 1);
    }
}
