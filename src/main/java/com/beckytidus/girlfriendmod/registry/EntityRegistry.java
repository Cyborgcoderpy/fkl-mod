package com.beckytidus.girlfriendmod.registry;

import com.beckytidus.girlfriendmod.entity.BoyFriendEntity;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class EntityRegistry {

    public static final EntityType<GirlFriendEntity> GIRLFRIEND = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("girlfriend-mod", "girlfriend"),
            EntityType.Builder.<GirlFriendEntity>create(GirlFriendEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.8f)
                    .maxTrackingRange(10)
                    .build()
    );

    public static final EntityType<BoyFriendEntity> BOYFRIEND = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("girlfriend-mod", "boyfriend"),
            EntityType.Builder.<BoyFriendEntity>create(BoyFriendEntity::new, SpawnGroup.MISC)
                    .dimensions(0.6f, 1.9f)
                    .maxTrackingRange(10)
                    .build()
    );

    public static void register() {
        // Triggers static initialiser
    }
}
