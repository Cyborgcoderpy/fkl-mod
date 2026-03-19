package com.beckytidus.girlfriendmod.mixin;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import org.spongepowered.asm.mixin.Mixin;

// Placeholder – renderer is registered via GirlfriendModClient directly.
// Kept so the mixins.json reference remains valid.
@Mixin(EntityRenderers.class)
public class EntityRenderManagerGirlfriendMixin {
}
