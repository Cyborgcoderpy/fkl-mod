package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class GirlFriendEntityRenderer extends MobEntityRenderer<GirlFriendEntity, GirlFriendEntityRenderState, GirlFriendEntityModel> {

    // Blush overlay texture (a small red-tinted quad drawn on cheeks)
    private static final Identifier BLUSH_TEXTURE = Identifier.of("girlfriend-mod", "textures/entity/blush_overlay.png");

    public GirlFriendEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new GirlFriendEntityModel(context.getPart(GirlFriendEntityModel.MODEL_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(GirlFriendEntityRenderState state) {
        return GirlfriendSkins.getSkin(state.skinIndex);
    }

    @Override
    public GirlFriendEntityRenderState createRenderState() {
        return new GirlFriendEntityRenderState();
    }

    @Override
    public void updateRenderState(GirlFriendEntity entity, GirlFriendEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.skinIndex  = entity.getSkinIndex();
        state.isBlushing = entity.isBlushing;
    }

    @Override
    public void render(GirlFriendEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(state, matrices, vertexConsumers, light);

        // Draw blush cheek overlay if blushing
        if (state.isBlushing) {
            renderBlushOverlay(state, matrices, vertexConsumers, light);
        }
    }

    /**
     * Draws a simple semi-transparent red quad on each cheek.
     * This replaces heart particles with a blushing face effect.
     */
    private void renderBlushOverlay(GirlFriendEntityRenderState state, MatrixStack matrices,
                                     VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        // Position at head level
        matrices.translate(0.0, 1.45, 0.0);

        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(BLUSH_TEXTURE));
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float alpha = 0.6f + 0.2f * MathHelper.sin(state.age * 0.1f); // gentle pulse

        // Left cheek quad
        drawBlushQuad(consumer, matrix, -0.16f, -0.08f, 0.126f, alpha, light);
        // Right cheek quad
        drawBlushQuad(consumer, matrix, 0.06f, -0.08f, 0.126f, alpha, light);

        matrices.pop();
    }

    private void drawBlushQuad(VertexConsumer consumer, Matrix4f matrix,
                                 float x, float y, float z, float alpha, int light) {
        float w = 0.10f, h = 0.05f;
        int r = 255, g = 120, b = 120, a = (int)(alpha * 255);

        consumer.vertex(matrix, x,     y,     z).color(r, g, b, a).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1);
        consumer.vertex(matrix, x,     y + h, z).color(r, g, b, a).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1);
        consumer.vertex(matrix, x + w, y + h, z).color(r, g, b, a).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1);
        consumer.vertex(matrix, x + w, y,     z).color(r, g, b, a).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1);
    }
}
