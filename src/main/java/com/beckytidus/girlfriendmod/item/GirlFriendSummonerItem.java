package com.beckytidus.girlfriendmod.item;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class GirlFriendSummonerItem extends Item {

    public GirlFriendSummonerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.FAIL;

        // Check there isn't already a girlfriend owned by this player
        boolean alreadyOwned = world.getEntitiesByClass(GirlFriendEntity.class,
                player.getBoundingBox().expand(128),
                gf -> player.getUuid().equals(gf.getOwnerUuid())
        ).size() > 0;

        if (alreadyOwned) {
            player.sendMessage(Text.literal("§cYou already have a companion!"), false);
            return ActionResult.FAIL;
        }

        BlockPos pos = context.getBlockPos().up();
        GirlFriendEntity gf = EntityRegistry.GIRLFRIEND.create((ServerWorld) world);
        if (gf == null) return ActionResult.FAIL;

        Random rng = new Random();
        String name = FemaleNames.random(rng);
        gf.setCustomName2(name);
        gf.setSkinIndex(rng.nextInt(22));
        gf.setOwner(player);
        gf.setTamed(true);
        gf.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
        world.spawnEntity(gf);

        player.sendMessage(Text.literal("§a" + name + " has arrived!"), false);

        if (!player.isCreative()) context.getStack().decrement(1);
        return ActionResult.SUCCESS;
    }
}
