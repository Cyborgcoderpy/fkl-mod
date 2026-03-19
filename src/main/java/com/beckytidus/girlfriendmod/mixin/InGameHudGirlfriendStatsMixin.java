package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public class InGameHudGirlfriendStatsMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        List<GirlFriendEntity> list = client.world.getEntitiesByClass(
                GirlFriendEntity.class,
                client.player.getBoundingBox().expand(64),
                gf -> client.player.getUuid().equals(gf.getOwnerUuid())
        );
        if (list.isEmpty()) return;

        GirlFriendEntity gf = list.get(0);
        int screenWidth = client.getWindow().getScaledWidth();

        // Draw in top-right corner
        String name   = "§d" + gf.getDisplayName2();
        String hp     = "§c❤ " + (int) gf.getHealth() + "/" + (int) gf.getMaxHealth();
        String hunger = "§6🍖 " + gf.getHunger() + "/20";

        context.drawTextWithShadow(client.textRenderer, name,   screenWidth - 100, 5,  0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, hp,     screenWidth - 100, 15, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, hunger, screenWidth - 100, 25, 0xFFFFFF);
    }
}
