package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.client.render.*;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class GirlfriendModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Girl model + renderer
        EntityModelLayerRegistry.registerModelLayer(
                GirlFriendEntityModel.MODEL_LAYER,
                GirlFriendEntityModel::getTexturedModelData
        );
        EntityRendererRegistry.register(
                EntityRegistry.GIRLFRIEND,
                GirlFriendEntityRenderer::new
        );

        // Boy model + renderer
        EntityModelLayerRegistry.registerModelLayer(
                BoyFriendEntityModel.MODEL_LAYER,
                BoyFriendEntityModel::getTexturedModelData
        );
        EntityRendererRegistry.register(
                EntityRegistry.BOYFRIEND,
                BoyFriendEntityRenderer::new
        );
    }
}
