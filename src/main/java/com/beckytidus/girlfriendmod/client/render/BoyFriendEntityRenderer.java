package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.entity.BoyFriendEntity;
import com.beckytidus.girlfriendmod.registry.BoyFriendSkins;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class BoyFriendEntityRenderer extends MobEntityRenderer<BoyFriendEntity, BoyFriendEntityRenderState, BoyFriendEntityModel> {

    public BoyFriendEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BoyFriendEntityModel(context.getPart(BoyFriendEntityModel.MODEL_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(BoyFriendEntityRenderState state) {
        return BoyFriendSkins.getSkin(state.skinIndex);
    }

    @Override
    public BoyFriendEntityRenderState createRenderState() {
        return new BoyFriendEntityRenderState();
    }

    @Override
    public void updateRenderState(BoyFriendEntity entity, BoyFriendEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.skinIndex = entity.getSkinIndex();
    }
}
