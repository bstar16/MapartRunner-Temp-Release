package com.example.mapart.plan.sweep.flight;

import com.example.mapart.plan.sweep.BuildLane;
import com.example.mapart.plan.sweep.LaneAxis;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

public final class LaneEntryPlanner {
    public LaneEntryPlan plan(BuildLane lane, Vec3d playerPosition, double centerTolerance) {
        Objects.requireNonNull(lane, "lane");
        Objects.requireNonNull(playerPosition, "playerPosition");

        double laneCenter = lane.fixedCoordinate() + 0.5;
        double lateralOffset = lane.axis() == LaneAxis.X
                ? playerPosition.z - laneCenter
                : playerPosition.x - laneCenter;
        boolean aligned = Math.abs(lateralOffset) <= centerTolerance;

        return new LaneEntryPlan(lateralOffset, centerTolerance, aligned);
    }

    public record LaneEntryPlan(double lateralOffset, double centerTolerance, boolean aligned) {
    }
}
