package com.example.mapart.supply;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SupplyInteractionTracker {
    private final SupplyStore supplyStore;
    private final Map<UUID, PendingSupplyRequest> pendingSupplyByPlayer = new ConcurrentHashMap<>();

    public SupplyInteractionTracker(SupplyStore supplyStore) {
        this.supplyStore = supplyStore;
    }

    public void registerCallbacks() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            PendingSupplyRequest request = pendingSupplyByPlayer.get(player.getUuid());
            if (request == null) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            if (world.getBlockEntity(pos) instanceof Inventory) {
                pendingSupplyByPlayer.remove(player.getUuid());
                String dimension = world.getRegistryKey().getValue().toString();
                SupplyPoint point = supplyStore.add(pos, dimension, request.name());
                player.sendMessage(Text.literal("Added supply #" + point.id() + " at " + pos.toShortString() + " in " + dimension
                        + (request.name() == null ? "" : " (" + request.name() + ")")), false);
            } else {
                player.sendMessage(Text.literal("That block is not a container. Right-click a container to register supply."
                ), false);
            }

            return ActionResult.PASS;
        });
    }

    public void beginSupplyRegistration(ServerPlayerEntity player, String name) {
        pendingSupplyByPlayer.put(player.getUuid(), new PendingSupplyRequest(name));
    }

    public boolean hasPendingRegistration(ServerPlayerEntity player) {
        return pendingSupplyByPlayer.containsKey(player.getUuid());
    }

    private record PendingSupplyRequest(String name) {
    }
}
