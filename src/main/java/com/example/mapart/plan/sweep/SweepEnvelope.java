package com.example.mapart.plan.sweep;

import com.example.mapart.plan.BuildPlan;
import com.example.mapart.plan.Placement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

/**
 * World-space sweep constraints derived from schematic-relative bounds + selected lane.
 */
public final class SweepEnvelope {
    private final BlockPos origin;
    private final WorldRectangle schematicBounds;

    private SweepEnvelope(BlockPos origin, WorldRectangle schematicBounds) {
        this.origin = origin;
        this.schematicBounds = schematicBounds;
    }

    public static SweepEnvelope fromPlan(BuildPlan plan, BlockPos origin) {
        Objects.requireNonNull(plan, "plan");
        Objects.requireNonNull(origin, "origin");
        if (plan.placements().isEmpty()) {
            throw new IllegalArgumentException("plan must have at least one placement");
        }

        int minRelX = Integer.MAX_VALUE;
        int maxRelX = Integer.MIN_VALUE;
        int minRelZ = Integer.MAX_VALUE;
        int maxRelZ = Integer.MIN_VALUE;

        for (Placement placement : plan.placements()) {
            BlockPos pos = placement.relativePos();
            minRelX = Math.min(minRelX, pos.getX());
            maxRelX = Math.max(maxRelX, pos.getX());
            minRelZ = Math.min(minRelZ, pos.getZ());
            maxRelZ = Math.max(maxRelZ, pos.getZ());
        }

        double minWorldX = origin.getX() + minRelX;
        double maxWorldX = origin.getX() + maxRelX + 1.0;
        double minWorldZ = origin.getZ() + minRelZ;
        double maxWorldZ = origin.getZ() + maxRelZ + 1.0;
        return new SweepEnvelope(origin, new WorldRectangle(minWorldX, maxWorldX, minWorldZ, maxWorldZ));
    }

    public boolean inSchematicBounds(Vec3d worldPosition, double margin) {
        return schematicBounds.contains(worldPosition, margin);
    }

    public LaneCorridor activeLaneCorridor(BuildLane lane, double lateralPadding, double progressPadding) {
        Objects.requireNonNull(lane, "lane");

        double minX;
        double maxX;
        double minZ;
        double maxZ;

        if (lane.axis() == LaneAxis.X) {
            minX = origin.getX() + lane.minProgress() - progressPadding;
            maxX = origin.getX() + lane.maxProgress() + 1.0 + progressPadding;
            double centerZ = origin.getZ() + lane.fixedCoordinate() + 0.5;
            double halfWidth = Math.max(1.0, lane.primaryHalfWidth()) + lateralPadding;
            minZ = centerZ - halfWidth;
            maxZ = centerZ + halfWidth;
        } else {
            minZ = origin.getZ() + lane.minProgress() - progressPadding;
            maxZ = origin.getZ() + lane.maxProgress() + 1.0 + progressPadding;
            double centerX = origin.getX() + lane.fixedCoordinate() + 0.5;
            double halfWidth = Math.max(1.0, lane.primaryHalfWidth()) + lateralPadding;
            minX = centerX - halfWidth;
            maxX = centerX + halfWidth;
        }

        return new LaneCorridor(new WorldRectangle(minX, maxX, minZ, maxZ));
    }

    public record LaneCorridor(WorldRectangle rectangle) {
        public LaneCorridor {
            Objects.requireNonNull(rectangle, "rectangle");
        }

        public boolean contains(Vec3d worldPosition) {
            return rectangle.contains(worldPosition, 0.0);
        }

        public Vec3d clampCenterlineTarget(Vec3d target) {
            return rectangle.clamp(target);
        }
    }

    public record WorldRectangle(double minX, double maxX, double minZ, double maxZ) {
        public WorldRectangle {
            if (Double.isNaN(minX) || Double.isNaN(maxX) || Double.isNaN(minZ) || Double.isNaN(maxZ)) {
                throw new IllegalArgumentException("rectangle coordinates must not be NaN");
            }
            if (minX > maxX || minZ > maxZ) {
                throw new IllegalArgumentException("rectangle min must be <= max");
            }
        }

        public boolean contains(Vec3d worldPosition, double margin) {
            return worldPosition.x >= minX - margin
                    && worldPosition.x <= maxX + margin
                    && worldPosition.z >= minZ - margin
                    && worldPosition.z <= maxZ + margin;
        }

        public Vec3d clamp(Vec3d target) {
            double clampedX = Math.max(minX, Math.min(maxX, target.x));
            double clampedZ = Math.max(minZ, Math.min(maxZ, target.z));
            return new Vec3d(clampedX, target.y, clampedZ);
        }
    }
}
