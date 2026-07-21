package de.dennisthegamer.miningstats.mixin;

import de.dennisthegamer.miningstats.tracker.OreTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerBreakMixin {

    // destroyBlock decides only AFTER reading the state whether the break is allowed at all:
    // it returns false for blockActionRestricted (adventure mode, spawn protection), a missing
    // canDestroyBlock permission, game master blocks without rights, and air. Counting at HEAD
    // therefore books ores that were never mined.
    //
    // The state still has to be captured at HEAD -- by RETURN the block is already gone
    // client-side and getBlockState(pos) would hand us air. Hence capture here, commit there.
    //
    // Note this only covers refusals the CLIENT makes itself. A server-side rejection
    // (WorldGuard and friends) is invisible at this point and is still counted.
    @Unique
    private BlockState miningstats$pendingState;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void miningstats$captureBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        miningstats$pendingState = client.level != null ? client.level.getBlockState(pos) : null;
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void miningstats$confirmBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = miningstats$pendingState;
        miningstats$pendingState = null;
        if (state != null && cir.getReturnValueZ()) {
            OreTracker.onBlockBroken(pos, state);
        }
    }
}
