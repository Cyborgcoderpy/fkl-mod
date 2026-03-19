package com.beckytidus.girlfriendmod.client.render;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class GirlFriendEntityModel extends net.minecraft.client.render.entity.model.SinglePartEntityModel<GirlFriendEntityRenderState> {

    public static final EntityModelLayer MODEL_LAYER =
            new EntityModelLayer(Identifier.of("girlfriend-mod", "girlfriend"), "main");

    private final ModelPart root;

    public GirlFriendEntityModel(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData partData = modelData.getRoot();

        // Head
        partData.addChild("head",
                ModelPartBuilder.create().uv(0, 0).cuboid(-4f, -8f, -4f, 8, 8, 8),
                ModelTransform.pivot(0f, 0f, 0f));

        // Hat layer
        partData.addChild("hat",
                ModelPartBuilder.create().uv(32, 0).cuboid(-4f, -8f, -4f, 8, 8, 8, new Dilation(0.5f)),
                ModelTransform.pivot(0f, 0f, 0f));

        // Body
        partData.addChild("body",
                ModelPartBuilder.create().uv(16, 16).cuboid(-4f, 0f, -2f, 8, 12, 4),
                ModelTransform.pivot(0f, 0f, 0f));

        // Arms
        partData.addChild("right_arm",
                ModelPartBuilder.create().uv(40, 16).cuboid(-3f, -2f, -2f, 4, 12, 4),
                ModelTransform.pivot(-5f, 2f, 0f));
        partData.addChild("left_arm",
                ModelPartBuilder.create().uv(32, 48).cuboid(-1f, -2f, -2f, 4, 12, 4),
                ModelTransform.pivot(5f, 2f, 0f));

        // Legs
        partData.addChild("right_leg",
                ModelPartBuilder.create().uv(0, 16).cuboid(-2f, 0f, -2f, 4, 12, 4),
                ModelTransform.pivot(-1.9f, 12f, 0f));
        partData.addChild("left_leg",
                ModelPartBuilder.create().uv(16, 48).cuboid(-2f, 0f, -2f, 4, 12, 4),
                ModelTransform.pivot(1.9f, 12f, 0f));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public ModelPart getPart() { return root; }

    @Override
    public void setAngles(GirlFriendEntityRenderState state) {
        // Basic walk animation via BipedEntityModel style
        float limbAngle = state.limbFrequency;
        float limbDist  = state.limbAmplitudeMultiplier;

        ModelPart rightArm = root.getChild("right_arm");
        ModelPart leftArm  = root.getChild("left_arm");
        ModelPart rightLeg = root.getChild("right_leg");
        ModelPart leftLeg  = root.getChild("left_leg");
        ModelPart head     = root.getChild("head");

        float swing = (float) Math.cos(limbAngle * 0.6662f) * limbDist;

        rightArm.pitch = -swing * 0.5f;
        leftArm.pitch  =  swing * 0.5f;
        rightLeg.pitch =  swing;
        leftLeg.pitch  = -swing;

        head.yaw   = state.headYaw   * ((float) Math.PI / 180f);
        head.pitch = state.headPitch * ((float) Math.PI / 180f);
    }
}
