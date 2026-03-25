package com.miningstats.mixin;

import com.miningstats.tracker.OreTracker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerBreakMixin {

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Capture the block state before it's destroyed
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            BlockState state = client.level.getBlockState(pos);
            OreTracker.onBlockBroken(pos, state);
        }
    }
}
