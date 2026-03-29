package com.example.mapart.plan.sweep;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class LaneCoordinateMapper {

    public int placementProgress(BuildLane lane, BlockPos position) {
        return lane.axis() == LaneAxis.X ? position.getX() : position.getZ();
    }

    public int placementSweepCoordinate(BuildLane lane, BlockPos position) {
        return lane.axis() == LaneAxis.X ? position.getZ() : position.getX();
    }

    public double projectProgress(BuildLane lane, Vec3d position) {
        return lane.axis() == LaneAxis.X ? position.x : position.z;
    }

    public double lateralOffsetFromCenter(BuildLane lane, Vec3d position) {
        double sweepCoordinate = lane.axis() == LaneAxis.X ? position.z : position.x;
        return sweepCoordinate - lane.fixedCoordinate();
    }

    public double distanceToEndpoint(BuildLane lane, Vec3d position) {
        double dx = position.x - lane.endPoint().getX();
        double dz = position.z - lane.endPoint().getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
