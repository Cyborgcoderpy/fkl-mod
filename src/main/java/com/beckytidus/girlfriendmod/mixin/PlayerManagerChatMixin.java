package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.entity.BoyFriendEntity;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Intercepts outgoing chat messages and forwards them to nearby owned
 * GirlFriendEntity instances so they can reply.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class PlayerManagerChatMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "handleDecoratedMessage", at = @At("TAIL"))
    private void onChat(SignedMessage message, CallbackInfo ci) {
        if (player == null) return;
        World world = player.getWorld();
        String text = message.getContent().getString();

        // Forward to girlfriend
        List<GirlFriendEntity> girlfriends = world.getEntitiesByClass(
                GirlFriendEntity.class,
                player.getBoundingBox().expand(32),
                gf -> player.getUuid().equals(gf.getOwnerUuid())
        );
        for (GirlFriendEntity gf : girlfriends) {
            gf.receiveChat(player, text);
        }

        // Forward to boyfriend
        List<BoyFriendEntity> boyfriends = world.getEntitiesByClass(
                BoyFriendEntity.class,
                player.getBoundingBox().expand(32),
                bf -> player.getUuid().equals(bf.getOwnerUuid())
        );
        for (BoyFriendEntity bf : boyfriends) {
            bf.receiveChat(player, text);
        }
    }
}
