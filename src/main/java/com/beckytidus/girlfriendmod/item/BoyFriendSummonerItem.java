package com.beckytidus.girlfriendmod.item;

import com.beckytidus.girlfriendmod.entity.BoyFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.MaleNames;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BoyFriendSummonerItem extends Item {

    public BoyFriendSummonerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.FAIL;

        // Only one boyfriend per player
        boolean alreadyOwned = world.getEntitiesByClass(BoyFriendEntity.class,
                player.getBoundingBox().expand(128),
                bf -> player.getUuid().equals(bf.getOwnerUuid())
        ).size() > 0;

        if (alreadyOwned) {
            player.sendMessage(Text.literal("§cYou already have a companion!"), false);
            return ActionResult.FAIL;
        }

        BlockPos pos = context.getBlockPos().up();
        BoyFriendEntity bf = EntityRegistry.BOYFRIEND.create((ServerWorld) world);
        if (bf == null) return ActionResult.FAIL;

        Random rng = new Random();
        String name = MaleNames.random(rng);
        bf.setCustomName2(name);
        bf.setSkinIndex(rng.nextInt(10));
        bf.setOwner(player);
        bf.setTamed(true);
        bf.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
        world.spawnEntity(bf);

        player.sendMessage(Text.literal("§b" + name + " has arrived!"), false);
        if (!player.isCreative()) context.getStack().decrement(1);
        return ActionResult.SUCCESS;
    }
}
