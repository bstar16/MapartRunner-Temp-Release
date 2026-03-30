package com.example.mapart.plan.sweep.flight;

import com.example.mapart.plan.sweep.BuildLane;
import com.example.mapart.plan.sweep.LaneDirection;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class TurnPlanner {
    public boolean shouldInitiateSoftTurn(double distanceToEndpoint, double endpointApproachDistance) {
        return distanceToEndpoint <= endpointApproachDistance;
    }

    public TurnPlan planSerpentineTurn(BuildLane currentLane, BuildLane nextLane, Vec3d playerPosition) {
        Objects.requireNonNull(currentLane, "currentLane");
        Objects.requireNonNull(nextLane, "nextLane");
        Objects.requireNonNull(playerPosition, "playerPosition");

        if (currentLane.axis() != nextLane.axis()) {
            return TurnPlan.unavailable("lane axis mismatch");
        }
        if (currentLane.laneIndex() == nextLane.laneIndex()) {
            return TurnPlan.unavailable("next lane must differ");
        }

        boolean expectedSerpentineDirectionFlip = currentLane.direction() != nextLane.direction();
        if (!expectedSerpentineDirectionFlip) {
            return TurnPlan.unavailable("serpentine turn expects direction flip");
        }

        double targetCenterCoordinate = nextLane.fixedCoordinate() + 0.5;
        LaneDirection expectedExitDirection = nextLane.direction();
        return TurnPlan.available(targetCenterCoordinate, expectedExitDirection);
    }

    public record TurnPlan(boolean available,
                           String reason,
                           double targetCenterCoordinate,
                           LaneDirection expectedExitDirection) {
        public static TurnPlan unavailable(String reason) {
            return new TurnPlan(false, reason, Double.NaN, LaneDirection.FORWARD);
        }

        public static TurnPlan available(double targetCenterCoordinate, LaneDirection expectedExitDirection) {
            return new TurnPlan(true, "ok", targetCenterCoordinate, expectedExitDirection);
        }
    }
}
