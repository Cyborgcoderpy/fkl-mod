package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.goal.MimicBreakGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Intercepts player block-break events on the server and notifies any
 * owned GirlFriendEntity that has MimicBreakGoal active.
 */
@Mixin(net.minecraft.server.network.ServerPlayerInteractionManager.class)
public class BlockBreakMixin {

    @Inject(method = "processBlockBreakingAction", at = @At("TAIL"))
    private void onBlockBroken(BlockPos pos, net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action action,
                                net.minecraft.util.math.Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        // We catch the actual removal in the method below
    }

    @Inject(
        method = "tryBreakBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;I)Z"
        )
    )
    private void onActualBreak(BlockPos pos, CallbackInfo ci) {
        // Access the player via the enclosing class shadow
        ServerPlayerEntity player = ((ServerPlayerInteractionManagerAccessor) this).getPlayer();
        if (player == null) return;
        World world = player.getWorld();
        BlockState broken = world.getBlockState(pos);

        // Notify nearby owned girlfriend
        List<GirlFriendEntity> list = world.getEntitiesByClass(
                GirlFriendEntity.class,
                player.getBoundingBox().expand(32),
                gf -> player.getUuid().equals(gf.getOwnerUuid())
        );
        for (GirlFriendEntity gf : list) {
            MimicBreakGoal goal = gf.getMimicBreakGoal();
            if (goal != null) {
                goal.onOwnerBrokeBlock(broken.getBlock());
            }
        }
    }
}
