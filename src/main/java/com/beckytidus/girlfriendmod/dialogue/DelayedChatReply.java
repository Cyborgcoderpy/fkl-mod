package com.beckytidus.girlfriendmod.dialogue;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Queues a chat reply to be sent after a random delay,
 * so the girlfriend doesn't reply instantly (feels more natural).
 */
public class DelayedChatReply {

    private final Deque<PendingReply> queue = new ArrayDeque<>();

    public void schedule(ServerPlayerEntity player, String message, int delayTicks) {
        queue.add(new PendingReply(player, message, delayTicks));
    }

    public void tick(GirlFriendEntity gf) {
        if (queue.isEmpty()) return;
        PendingReply top = queue.peek();
        if (top == null) return;
        top.ticksLeft--;
        if (top.ticksLeft <= 0) {
            queue.poll();
            if (top.player != null && top.player.isAlive()) {
                top.player.sendMessage(Text.literal("§b" + top.message), false);
            }
        }
    }

    private static class PendingReply {
        final ServerPlayerEntity player;
        final String message;
        int ticksLeft;

        PendingReply(ServerPlayerEntity player, String message, int ticksLeft) {
            this.player = player;
            this.message = message;
            this.ticksLeft = ticksLeft;
        }
    }
}
