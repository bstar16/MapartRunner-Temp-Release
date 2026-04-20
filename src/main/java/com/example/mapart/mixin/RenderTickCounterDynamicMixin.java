package com.example.mapart.mixin;

import com.example.mapart.runtime.ClientTimerController;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public abstract class RenderTickCounterDynamicMixin {
    @Shadow
    private float dynamicDeltaTicks;

    @Inject(
            method = "beginRenderTick(J)I",
            at = @At("RETURN")
    )
    private void mapart$applyClientTimerMultiplier(long timeMillis, CallbackInfoReturnable<Integer> cir) {
        this.dynamicDeltaTicks *= (float) ClientTimerController.getMultiplier();
    }
}
