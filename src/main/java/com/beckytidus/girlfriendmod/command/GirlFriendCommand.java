package com.beckytidus.girlfriendmod.command;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class GirlFriendCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("gf")
                // /gf status
                .then(CommandManager.literal("status")
                    .executes(ctx -> cmdStatus(ctx.getSource())))

                // /gf follow
                .then(CommandManager.literal("follow")
                    .executes(ctx -> cmdFollow(ctx.getSource())))

                // /gf wait
                .then(CommandManager.literal("wait")
                    .executes(ctx -> cmdWait(ctx.getSource())))

                // /gf come
                .then(CommandManager.literal("come")
                    .executes(ctx -> cmdCome(ctx.getSource())))

                // /gf skin <0-21>
                .then(CommandManager.literal("skin")
                    .then(CommandManager.argument("index", IntegerArgumentType.integer(0, 21))
                        .executes(ctx -> cmdSkin(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "index")))))

                // /gf rename <name>
                .then(CommandManager.literal("rename")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> cmdRename(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))

                // /gf feed <amount>
                .then(CommandManager.literal("feed")
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 20))
                        .executes(ctx -> cmdFeed(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "amount")))))

                // /gf tp  — teleport her to you
                .then(CommandManager.literal("tp")
                    .executes(ctx -> cmdTp(ctx.getSource())))

                // /gf dismiss — removes / despawns
                .then(CommandManager.literal("dismiss")
                    .executes(ctx -> cmdDismiss(ctx.getSource())))

                // /gf wake — wake her up if sleeping
                .then(CommandManager.literal("wake")
                    .executes(ctx -> cmdWake(ctx.getSource())))

                // /gf helpbreak — toggle block-breaking assistance
                .then(CommandManager.literal("helpbreak")
                    .executes(ctx -> cmdHelpBreak(ctx.getSource())))
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private static GirlFriendEntity findOwned(ServerCommandSource src) {
        if (!(src.getEntity() instanceof ServerPlayerEntity player)) return null;
        List<GirlFriendEntity> list = player.getWorld().getEntitiesByClass(
                GirlFriendEntity.class,
                player.getBoundingBox().expand(128),
                gf -> player.getUuid().equals(gf.getOwnerUuid())
        );
        return list.isEmpty() ? null : list.get(0);
    }

    private static void msg(ServerCommandSource src, String text) {
        src.sendFeedback(() -> Text.literal(text), false);
    }

    // ── Commands ──────────────────────────────────────────────────────────
    private static int cmdStatus(ServerCommandSource src) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        msg(src, "§e[" + gf.getDisplayName2() + "] §fHP: " + (int)gf.getHealth()
                + "/" + (int)gf.getMaxHealth()
                + " | Hunger: " + gf.getHunger() + "/20"
                + " | Affection: " + gf.getAffection() + "/100"
                + " | Sitting: " + gf.isSitting());
        return 1;
    }

    private static int cmdFollow(ServerCommandSource src) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.setSitting(false);
        msg(src, "§a" + gf.getDisplayName2() + " is now following you.");
        return 1;
    }

    private static int cmdWait(ServerCommandSource src) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.setSitting(true);
        msg(src, "§a" + gf.getDisplayName2() + " will wait here.");
        return 1;
    }

    private static int cmdCome(ServerCommandSource src) {
        if (!(src.getEntity() instanceof ServerPlayerEntity player)) return 0;
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.setSitting(false);
        gf.teleport(player.getX(), player.getY(), player.getZ());
        msg(src, "§a" + gf.getDisplayName2() + " teleported to you.");
        return 1;
    }

    private static int cmdSkin(ServerCommandSource src, int index) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.setSkinIndex(index);
        msg(src, "§aSkin changed to #" + index + ".");
        return 1;
    }

    private static int cmdRename(ServerCommandSource src, String name) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.setCustomName2(name);
        msg(src, "§aRenamed to §e" + name + "§a.");
        return 1;
    }

    private static int cmdFeed(ServerCommandSource src, int amount) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.setHunger(gf.getHunger() + amount);
        msg(src, "§aFed " + gf.getDisplayName2() + " +" + amount + " hunger.");
        return 1;
    }

    private static int cmdTp(ServerCommandSource src) {
        if (!(src.getEntity() instanceof ServerPlayerEntity player)) return 0;
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.teleport(player.getX(), player.getY(), player.getZ());
        msg(src, "§a" + gf.getDisplayName2() + " teleported to you.");
        return 1;
    }

    private static int cmdDismiss(ServerCommandSource src) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        String name = gf.getDisplayName2();
        gf.discard();
        msg(src, "§7" + name + " has left...");
        return 1;
    }

    private static int cmdWake(ServerCommandSource src) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        gf.setSleeping(false);
        gf.setSitting(false);
        msg(src, "§a" + gf.getDisplayName2() + " woke up.");
        return 1;
    }

    private static int cmdHelpBreak(ServerCommandSource src) {
        GirlFriendEntity gf = findOwned(src);
        if (gf == null) { msg(src, "§cNo companion found."); return 0; }
        var goal = gf.getMimicBreakGoal();
        if (goal == null) { msg(src, "§cBreak-assist not available."); return 0; }
        boolean nowActive = !goal.isActive();
        goal.setActive(nowActive);
        if (nowActive) {
            msg(src, "§aBreak-assist ON — break any block and "
                    + gf.getDisplayName2() + " will break the same type nearby!");
        } else {
            msg(src, "§7Break-assist OFF.");
        }
        return 1;
    }
}
