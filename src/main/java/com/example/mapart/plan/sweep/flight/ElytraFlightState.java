package com.example.mapart.plan.sweep.flight;

public enum ElytraFlightState {
    IDLE,
    TAKEOFF,
    LANE_ENTRY_ALIGNMENT,
    LANE_FOLLOWING,
    APPROACH_ENDPOINT,
    SOFT_TURN,
    RECOVERY,
    COMPLETE,
    FAILED,
    INTERRUPTED
}
