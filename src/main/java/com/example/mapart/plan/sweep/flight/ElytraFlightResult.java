package com.example.mapart.plan.sweep.flight;

import java.util.Optional;

public record ElytraFlightResult(
        int laneIndex,
        ElytraFlightState finalState,
        int ticksElapsed,
        int recoveryAttempts,
        boolean endpointApproachObserved,
        boolean softTurnPlanned,
        Optional<FlightFailureReason> failureReason
) {
    public boolean success() {
        return finalState == ElytraFlightState.COMPLETE;
    }
}
