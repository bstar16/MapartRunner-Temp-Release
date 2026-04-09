package com.example.mapart.plan.sweep.flight;

public final class FlightRecoveryHandler {
    public RecoveryDecision evaluate(int recoveryAttempts, int maxRecoveryAttempts, FlightFailureReason reason) {
        if (recoveryAttempts > maxRecoveryAttempts) {
            return new RecoveryDecision(false, FlightFailureReason.RECOVERY_EXHAUSTED);
        }

        if (reason == FlightFailureReason.TAKEOFF_TIMEOUT
                || reason == FlightFailureReason.ENTRY_ALIGNMENT_TIMEOUT
                || reason == FlightFailureReason.ALTITUDE_BAND_VIOLATION
                || reason == FlightFailureReason.ENDPOINT_APPROACH_TIMEOUT
                || reason == FlightFailureReason.LANE_CORRIDOR_VIOLATION
                || reason == FlightFailureReason.SCHEMATIC_BOUNDS_VIOLATION) {
            return new RecoveryDecision(true, reason);
        }

        return new RecoveryDecision(false, reason);
    }

    public record RecoveryDecision(boolean retryPermitted, FlightFailureReason reason) {
    }
}
