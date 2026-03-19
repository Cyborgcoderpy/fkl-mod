package com.beckytidus.girlfriendmod.event;

import com.beckytidus.girlfriendmod.entity.BoyFriendEntity;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class EntityAttributeHandler {
    public static void register() {
        FabricDefaultAttributeRegistry.register(
                EntityRegistry.GIRLFRIEND,
                GirlFriendEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                EntityRegistry.BOYFRIEND,
                BoyFriendEntity.createAttributes()
        );
    }
}
