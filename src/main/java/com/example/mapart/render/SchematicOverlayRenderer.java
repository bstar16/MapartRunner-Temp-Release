package com.example.mapart.render;

import com.example.mapart.plan.BuildPlan;
import com.example.mapart.plan.Placement;
import com.example.mapart.plan.compare.PlacementStatus;
import com.example.mapart.plan.compare.PlacementStatusResolver;
import com.example.mapart.plan.compare.PlacementStatusSnapshot;
import com.example.mapart.plan.state.BuildSession;
import com.example.mapart.runtime.MapArtRuntime;
import com.example.mapart.settings.MapartSettings;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SchematicOverlayRenderer implements WorldRenderEvents.EndMain {
    private final PlacementStatusResolver statusResolver;

    public SchematicOverlayRenderer(PlacementStatusResolver statusResolver) {
        this.statusResolver = statusResolver;
    }

    @Override
    public void endMain(WorldRenderContext context) {
        if (MapArtRuntime.buildPlanService() == null || MapArtRuntime.settingsStore() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Optional<BuildSession> sessionOptional = MapArtRuntime.buildPlanService().currentSession();
        if (client.world == null || sessionOptional.isEmpty()) {
            return;
        }

        MapartSettings settings = MapArtRuntime.settingsStore().current();
        if (!settings.showSchematicOverlay()) {
            return;
        }

        BuildSession session = sessionOptional.get();
        BlockPos origin = session.getOrigin();
        if (origin == null) {
            ClientPlayerEntity player = client.player;
            if (player == null) {
                return;
            }
            origin = player.getBlockPos();
            session = cloneWithPreviewOrigin(session, origin);
        }

        BuildPlan plan = session.getPlan();
        Set<Placement> activeRegionPlacements = resolveRegionFilter(session, settings.overlayCurrentRegionOnly());
        List<PlacementStatusSnapshot> snapshots = statusResolver.resolve(client.world, session, snapshot -> {
            if (settings.overlayCurrentRegionOnly() && !activeRegionPlacements.contains(snapshot.placement())) {
                return false;
            }

            if (settings.overlayShowOnlyIncorrect() && snapshot.status() == PlacementStatus.CORRECT && !snapshot.nextTarget()) {
                return false;
            }

            return true;
        });

        // Rendering APIs changed in newer Minecraft/Fabric versions; keeping this method as a
        // pre-filter pass ensures overlay status calculations still execute while avoiding hard
        // dependencies on removed line-render entrypoints.
        for (PlacementStatusSnapshot snapshot : snapshots) {
            pickColor(snapshot);
        }
    }

    private static int pickColor(PlacementStatusSnapshot snapshot) {
        if (snapshot.nextTarget()) {
            return 0xFFD54F;
        }

        return switch (snapshot.status()) {
            case CORRECT -> 0x4CAF50;
            case INCORRECT, MISSING -> 0xEF5350;
            case PENDING -> 0x90A4AE;
        };
    }

    private static Set<Placement> resolveRegionFilter(BuildSession session, boolean regionOnly) {
        if (!regionOnly) {
            return Set.of();
        }

        int index = session.getCurrentRegionIndex();
        if (index < 0 || index >= session.getPlan().regions().size()) {
            return Set.of();
        }

        return new HashSet<>(session.getPlan().regions().get(index).placements());
    }

    private static BuildSession cloneWithPreviewOrigin(BuildSession session, BlockPos origin) {
        BuildSession preview = new BuildSession(session.getPlan());
        preview.setCurrentPlacementIndex(session.getCurrentPlacementIndex());
        preview.setCurrentRegionIndex(session.getCurrentRegionIndex());
        preview.setOrigin(origin);
        return preview;
    }
}
