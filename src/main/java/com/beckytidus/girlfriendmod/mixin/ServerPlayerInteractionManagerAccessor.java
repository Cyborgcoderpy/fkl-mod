package com.beckytidus.girlfriendmod.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.server.network.ServerPlayerInteractionManager.class)
public interface ServerPlayerInteractionManagerAccessor {
    @Accessor("player")
    ServerPlayerEntity getPlayer();
}
