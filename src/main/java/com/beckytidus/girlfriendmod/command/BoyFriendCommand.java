package com.beckytidus.girlfriendmod.command;

import com.beckytidus.girlfriendmod.entity.BoyFriendEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class BoyFriendCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("bf")
                .then(CommandManager.literal("status")
                    .executes(ctx -> cmdStatus(ctx.getSource())))
                .then(CommandManager.literal("follow")
                    .executes(ctx -> cmdFollow(ctx.getSource())))
                .then(CommandManager.literal("wait")
                    .executes(ctx -> cmdWait(ctx.getSource())))
                .then(CommandManager.literal("come")
                    .executes(ctx -> cmdCome(ctx.getSource())))
                .then(CommandManager.literal("tp")
                    .executes(ctx -> cmdCome(ctx.getSource())))
                .then(CommandManager.literal("skin")
                    .then(CommandManager.argument("index", IntegerArgumentType.integer(0, 9))
                        .executes(ctx -> cmdSkin(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "index")))))
                .then(CommandManager.literal("rename")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> cmdRename(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))
                .then(CommandManager.literal("feed")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 20))
                        .executes(ctx -> cmdFeed(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "amount")))))
                .then(CommandManager.literal("helpbreak")
                    .executes(ctx -> cmdHelpBreak(ctx.getSource())))
                .then(CommandManager.literal("wake")
                    .executes(ctx -> cmdWake(ctx.getSource())))
                .then(CommandManager.literal("dismiss")
                    .executes(ctx -> cmdDismiss(ctx.getSource())))
        );
    }

    private static BoyFriendEntity findOwned(ServerCommandSource src) {
        if (!(src.getEntity() instanceof ServerPlayerEntity player)) return null;
        List<BoyFriendEntity> list = player.getWorld().getEntitiesByClass(
                BoyFriendEntity.class,
                player.getBoundingBox().expand(128),
                bf -> player.getUuid().equals(bf.getOwnerUuid())
        );
        return list.isEmpty() ? null : list.get(0);
    }

    private static void msg(ServerCommandSource src, String text) {
        src.sendFeedback(() -> Text.literal(text), false);
    }

    private static int cmdStatus(ServerCommandSource src) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        msg(src, "§b[" + bf.getDisplayName2() + "] §fHP: " + (int) bf.getHealth()
                + "/" + (int) bf.getMaxHealth()
                + " | Hunger: " + bf.getHunger() + "/20"
                + " | Sitting: " + bf.isSitting());
        return 1;
    }

    private static int cmdFollow(ServerCommandSource src) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        bf.setSitting(false);
        msg(src, "§a" + bf.getDisplayName2() + " is now following you.");
        return 1;
    }

    private static int cmdWait(ServerCommandSource src) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        bf.setSitting(true);
        msg(src, "§a" + bf.getDisplayName2() + " will wait here.");
        return 1;
    }

    private static int cmdCome(ServerCommandSource src) {
        if (!(src.getEntity() instanceof ServerPlayerEntity player)) return 0;
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        bf.setSitting(false);
        bf.teleport(player.getX(), player.getY(), player.getZ());
        msg(src, "§a" + bf.getDisplayName2() + " teleported to you.");
        return 1;
    }

    private static int cmdSkin(ServerCommandSource src, int index) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        bf.setSkinIndex(index);
        msg(src, "§aSkin changed to #" + index + ".");
        return 1;
    }

    private static int cmdRename(ServerCommandSource src, String name) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        bf.setCustomName2(name);
        msg(src, "§aRenamed to §e" + name + "§a.");
        return 1;
    }

    private static int cmdFeed(ServerCommandSource src, int amount) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        bf.setHunger(bf.getHunger() + amount);
        msg(src, "§aFed " + bf.getDisplayName2() + " +" + amount + " hunger.");
        return 1;
    }

    private static int cmdHelpBreak(ServerCommandSource src) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        var goal = bf.getMimicBreakGoal();
        if (goal == null) { msg(src, "§cBreak-assist not available."); return 0; }
        boolean nowActive = !goal.isActive();
        goal.setActive(nowActive);
        msg(src, nowActive
            ? "§aBreak-assist ON — " + bf.getDisplayName2() + " will help you break blocks!"
            : "§7Break-assist OFF.");
        return 1;
    }

    private static int cmdWake(ServerCommandSource src) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        bf.setSleeping(false);
        bf.setSitting(false);
        msg(src, "§a" + bf.getDisplayName2() + " woke up.");
        return 1;
    }

    private static int cmdDismiss(ServerCommandSource src) {
        BoyFriendEntity bf = findOwned(src);
        if (bf == null) { msg(src, "§cNo companion found."); return 0; }
        String name = bf.getDisplayName2();
        bf.discard();
        msg(src, "§7" + name + " has left...");
        return 1;
    }
}
