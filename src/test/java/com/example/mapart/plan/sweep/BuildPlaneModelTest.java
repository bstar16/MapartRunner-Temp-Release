package com.example.mapart.plan.sweep;

import com.example.mapart.plan.Placement;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildPlaneModelTest {

    private final LanePlanner lanePlanner = new LanePlanner();

    @Test
    void classifiesPlacementLaneMembershipBySweepCoordinate() {
        BuildLane lane = xAxisLanes().get(1);
        BuildPlaneModel model = modelWithXAxisPlacements(index -> false);

        List<BuildPlaneModel.LanePlacement> placements = model.placementsForLane(lane.laneIndex());

        assertEquals(2, placements.size());
        assertEquals(1, placements.getFirst().placement().relativePos().getZ());
        assertEquals(1, placements.get(1).placement().relativePos().getZ());
    }

    @Test
    void projectsProgressFromWorldPosition() {
        BuildPlaneModel model = modelWithXAxisPlacements(index -> false);
        BuildLane lane = xAxisLanes().getFirst();

        double projected = model.projectProgress(lane, new Vec3d(3.75, 70, 0.1));

        assertEquals(3.75, projected, 0.0001);
    }

    @Test
    void computesLateralOffsetFromLaneCenter() {
        BuildPlaneModel model = modelWithXAxisPlacements(index -> false);
        BuildLane lane = xAxisLanes().get(1);

        double offset = model.lateralOffsetFromCenter(lane, new Vec3d(4.2, 70, 2.5));

        assertEquals(1.5, offset, 0.0001);
    }

    @Test
    void computesDistanceToLaneEndpointInPlane() {
        BuildPlaneModel model = modelWithXAxisPlacements(index -> false);
        BuildLane lane = xAxisLanes().getFirst();

        double distance = model.distanceToLaneEndpoint(lane, new Vec3d(6.0, 70.0, 2.0));

        assertEquals(Math.sqrt(8.0), distance, 0.0001);
    }

    @Test
    void classifiesAheadBehindRelativeToLaneDirection() {
        BuildLane forwardLane = xAxisLanes().getFirst();
        BuildLane reverseLane = xAxisLanes().get(1);
        BuildPlaneModel model = modelWithXAxisPlacements(index -> false);

        Placement forwardPlacement = new Placement(new BlockPos(6, 0, forwardLane.fixedCoordinate()), block());
        Placement reversePlacement = new Placement(new BlockPos(2, 0, reverseLane.fixedCoordinate()), block());

        assertEquals(PlacementProgressRelation.AHEAD, model.classifyAheadBehind(forwardLane, forwardPlacement, 4.0));
        assertEquals(PlacementProgressRelation.AHEAD, model.classifyAheadBehind(reverseLane, reversePlacement, 4.0));
        assertEquals(PlacementProgressRelation.BEHIND, model.classifyAheadBehind(reverseLane,
                new Placement(new BlockPos(7, 0, reverseLane.fixedCoordinate()), block()), 4.0));
    }

    @Test
    void classifiesWidthBandUsingPrimaryAndEdgeWidths() {
        BuildPlaneModel model = modelWithXAxisPlacements(index -> false);
        BuildLane lane = xAxisLanes().getFirst();

        assertEquals(LaneWidthBand.PRIMARY, model.classifyWidthBand(lane, 1.0));
        assertEquals(LaneWidthBand.EDGE, model.classifyWidthBand(lane, 2.5));
        assertEquals(LaneWidthBand.OUTSIDE, model.classifyWidthBand(lane, 4.1));
    }

    @Test
    void returnsIncompletePlacementsFromCompletionPredicate() {
        BuildPlaneModel model = modelWithXAxisPlacements(index -> index < 2);
        BuildLane lane0 = xAxisLanes().getFirst();

        List<BuildPlaneModel.LanePlacement> allLanePlacements = model.placementsForLane(lane0.laneIndex());
        List<BuildPlaneModel.LanePlacement> incomplete = model.incompletePlacementsForLane(lane0.laneIndex());

        assertEquals(2, allLanePlacements.size());
        assertEquals(1, incomplete.size());
        assertEquals(2, incomplete.getFirst().placementIndex());
    }

    private List<BuildLane> xAxisLanes() {
        return lanePlanner.planLanes(
                new BlockPos(0, 70, 0),
                new BlockPos(8, 70, 2),
                new LanePlannerSettings(2, 2, 1.0)
        );
    }

    private BuildPlaneModel modelWithXAxisPlacements(java.util.function.IntPredicate completion) {
        List<Placement> placements = List.of(
                new Placement(new BlockPos(0, 0, 0), block()),
                new Placement(new BlockPos(1, 0, 1), block()),
                new Placement(new BlockPos(2, 0, 0), block()),
                new Placement(new BlockPos(4, 0, 1), block()),
                new Placement(new BlockPos(7, 0, 5), block())
        );

        return new BuildPlaneModel(xAxisLanes(), placements, completion);
    }

    private static Block block() {
        try {
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            return (Block) unsafe.allocateInstance(Block.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to allocate block", exception);
        }
    }
}
