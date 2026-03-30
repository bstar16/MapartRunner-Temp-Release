package com.example.mapart.plan.sweep.flight;

import com.example.mapart.plan.sweep.BuildLane;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Optional;

public final class ElytraFlightController {
    private final BuildLane lane;
    private final ElytraFlightControllerSettings settings;
    private final LaneEntryPlanner laneEntryPlanner;
    private final TurnPlanner turnPlanner;
    private final FlightRecoveryHandler recoveryHandler;

    private ElytraFlightState state = ElytraFlightState.IDLE;
    private ElytraFlightState recoveryTargetState = ElytraFlightState.TAKEOFF;
    private FlightFailureReason pendingFailureReason;

    private int ticksElapsed;
    private int stateTicks;
    private int recoveryAttempts;

    private boolean endpointApproachObserved;
    private boolean softTurnPlanned;
    private FlightFailureReason terminalFailureReason;

    public ElytraFlightController(BuildLane lane,
                                  ElytraFlightControllerSettings settings,
                                  LaneEntryPlanner laneEntryPlanner,
                                  TurnPlanner turnPlanner,
                                  FlightRecoveryHandler recoveryHandler) {
        this.lane = Objects.requireNonNull(lane, "lane");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.laneEntryPlanner = Objects.requireNonNull(laneEntryPlanner, "laneEntryPlanner");
        this.turnPlanner = Objects.requireNonNull(turnPlanner, "turnPlanner");
        this.recoveryHandler = Objects.requireNonNull(recoveryHandler, "recoveryHandler");
    }

    public ElytraFlightState state() {
        return state;
    }

    public AltitudeBand classifyAltitude(double y) {
        if (y < settings.minAltitude()) {
            return AltitudeBand.BELOW;
        }
        if (y > settings.maxAltitude()) {
            return AltitudeBand.ABOVE;
        }
        return AltitudeBand.IN_BAND;
    }

    public boolean isEndpointApproaching(Vec3d playerPosition) {
        return distanceToLaneEndpoint(playerPosition) <= settings.endpointApproachDistance();
    }

    public void tick(FlightTickInput input) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(input.playerPosition(), "input.playerPosition");

        if (isTerminal()) {
            return;
        }

        ticksElapsed++;
        stateTicks++;

        if (state == ElytraFlightState.IDLE) {
            transitionTo(ElytraFlightState.TAKEOFF);
        }

        switch (state) {
            case TAKEOFF -> handleTakeoff(input);
            case LANE_ENTRY_ALIGNMENT -> handleLaneEntryAlignment(input);
            case LANE_FOLLOWING -> handleLaneFollowing(input);
            case APPROACH_ENDPOINT -> handleEndpointApproach(input);
            case SOFT_TURN -> handleSoftTurn(input);
            case RECOVERY -> handleRecovery();
            default -> {
            }
        }
    }

    public void interrupt() {
        if (isTerminal()) {
            return;
        }
        terminalFailureReason = FlightFailureReason.INTERRUPTED_BY_CALLER;
        transitionTo(ElytraFlightState.INTERRUPTED);
    }

    public ElytraFlightResult result() {
        return new ElytraFlightResult(
                lane.laneIndex(),
                state,
                ticksElapsed,
                recoveryAttempts,
                endpointApproachObserved,
                softTurnPlanned,
                Optional.ofNullable(terminalFailureReason)
        );
    }

    private void handleTakeoff(FlightTickInput input) {
        if (input.fallFlying()) {
            transitionTo(ElytraFlightState.LANE_ENTRY_ALIGNMENT);
            return;
        }

        if (stateTicks >= settings.takeoffTimeoutTicks()) {
            requestRecovery(ElytraFlightState.TAKEOFF, FlightFailureReason.TAKEOFF_TIMEOUT);
        }
    }

    private void handleLaneEntryAlignment(FlightTickInput input) {
        LaneEntryPlanner.LaneEntryPlan plan = laneEntryPlanner.plan(
                lane,
                input.playerPosition(),
                settings.laneEntryCenterTolerance()
        );

        if (plan.aligned()) {
            transitionTo(ElytraFlightState.LANE_FOLLOWING);
            return;
        }

        if (stateTicks >= settings.laneEntryTimeoutTicks()) {
            requestRecovery(ElytraFlightState.LANE_ENTRY_ALIGNMENT, FlightFailureReason.ENTRY_ALIGNMENT_TIMEOUT);
        }
    }

    private void handleLaneFollowing(FlightTickInput input) {
        AltitudeBand altitudeBand = classifyAltitude(input.playerPosition().y);
        if (altitudeBand != AltitudeBand.IN_BAND) {
            requestRecovery(ElytraFlightState.LANE_FOLLOWING, FlightFailureReason.ALTITUDE_BAND_VIOLATION);
            return;
        }

        if (isEndpointApproaching(input.playerPosition())) {
            endpointApproachObserved = true;
            transitionTo(ElytraFlightState.APPROACH_ENDPOINT);
        }
    }

    private void handleEndpointApproach(FlightTickInput input) {
        if (distanceToLaneEndpoint(input.playerPosition()) <= lane.endpointTolerance()) {
            if (input.nextLane().isPresent()) {
                TurnPlanner.TurnPlan turnPlan = turnPlanner.planSerpentineTurn(lane, input.nextLane().get(), input.playerPosition());
                if (!turnPlan.available()) {
                    fail(FlightFailureReason.TURN_PLAN_UNAVAILABLE);
                    return;
                }
                softTurnPlanned = true;
                transitionTo(ElytraFlightState.SOFT_TURN);
                return;
            }
            transitionTo(ElytraFlightState.COMPLETE);
            return;
        }

        if (stateTicks >= settings.endpointApproachTimeoutTicks()) {
            requestRecovery(ElytraFlightState.APPROACH_ENDPOINT, FlightFailureReason.ENDPOINT_APPROACH_TIMEOUT);
        }
    }

    private void handleSoftTurn(FlightTickInput input) {
        if (input.nextLane().isEmpty()) {
            fail(FlightFailureReason.TURN_PLAN_UNAVAILABLE);
            return;
        }

        // Skeleton behavior: reaching this state indicates a valid turn plan exists.
        transitionTo(ElytraFlightState.COMPLETE);
    }

    private void handleRecovery() {
        FlightRecoveryHandler.RecoveryDecision decision = recoveryHandler.evaluate(
                recoveryAttempts,
                settings.maxRecoveryAttempts(),
                pendingFailureReason
        );

        if (!decision.retryPermitted()) {
            fail(FlightFailureReason.RECOVERY_EXHAUSTED);
            return;
        }

        transitionTo(recoveryTargetState);
    }

    private void requestRecovery(ElytraFlightState targetState, FlightFailureReason reason) {
        recoveryAttempts++;
        recoveryTargetState = targetState;
        pendingFailureReason = reason;
        transitionTo(ElytraFlightState.RECOVERY);
    }

    private double distanceToLaneEndpoint(Vec3d playerPosition) {
        return playerPosition.distanceTo(Vec3d.ofCenter(lane.endPoint()));
    }

    private void fail(FlightFailureReason reason) {
        terminalFailureReason = reason;
        transitionTo(ElytraFlightState.FAILED);
    }

    private boolean isTerminal() {
        return state == ElytraFlightState.COMPLETE
                || state == ElytraFlightState.FAILED
                || state == ElytraFlightState.INTERRUPTED;
    }

    private void transitionTo(ElytraFlightState nextState) {
        state = nextState;
        stateTicks = 0;
    }

    public enum AltitudeBand {
        BELOW,
        IN_BAND,
        ABOVE
    }

    public record FlightTickInput(Vec3d playerPosition, boolean fallFlying, Optional<BuildLane> nextLane) {
        public FlightTickInput {
            Objects.requireNonNull(playerPosition, "playerPosition");
            Objects.requireNonNull(nextLane, "nextLane");
        }

        public static FlightTickInput currentLaneOnly(Vec3d playerPosition, boolean fallFlying) {
            return new FlightTickInput(playerPosition, fallFlying, Optional.empty());
        }
    }
}
