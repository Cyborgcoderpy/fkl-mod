package com.beckytidus.girlfriendmod.goal;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * When /gf helpbreak is active, she watches what block type the owner
 * just broke, then finds and breaks all nearby matching blocks automatically.
 */
public class MimicBreakGoal extends Goal {

    private final GirlFriendEntity gf;

    // The block type she should help break (set externally)
    private Block targetBlockType = null;
    private boolean active = false;

    // Current block she is walking to / breaking
    private BlockPos currentTarget = null;
    private int breakProgress = 0;
    private static final int BREAK_TICKS = 40; // 2 seconds per block
    private static final int SEARCH_RADIUS = 6;

    public MimicBreakGoal(GirlFriendEntity gf) {
        this.gf = gf;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    // ── Called by the block-break event listener ───────────────────────────
    public void onOwnerBrokeBlock(Block block) {
        if (!active) return;
        // If no target type yet, or same type — adopt it
        if (targetBlockType == null || targetBlockType == block) {
            targetBlockType = block;
        }
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            targetBlockType = null;
            currentTarget   = null;
            breakProgress   = 0;
            notifyOwner(active
                ? "§a" + gf.getDisplayName2() + " will help you break blocks!"
                : "§7" + gf.getDisplayName2() + " stopped helping break blocks.");
        } else {
            notifyOwner("§a" + gf.getDisplayName2()
                    + " will help! Break the first block and she'll match it.");
        }
    }

    public boolean isActive() { return active; }

    // ── Goal logic ────────────────────────────────────────────────────────
    @Override
    public boolean canStart() {
        return active && targetBlockType != null && gf.getOwner() != null;
    }

    @Override
    public boolean shouldContinue() {
        return active && targetBlockType != null;
    }

    @Override
    public void tick() {
        if (targetBlockType == null) return;
        World world = gf.getWorld();

        // Find a new target if we don't have one
        if (currentTarget == null || world.getBlockState(currentTarget).getBlock() != targetBlockType) {
            currentTarget = findNearestMatchingBlock(world);
            breakProgress = 0;
            if (currentTarget != null) {
                gf.getNavigation().startMovingTo(
                        currentTarget.getX() + 0.5, currentTarget.getY(), currentTarget.getZ() + 0.5, 1.1);
            }
            return;
        }

        // Walk to it
        double dist = gf.getPos().distanceTo(
                net.minecraft.util.math.Vec3d.ofCenter(currentTarget));
        if (dist > 2.5) {
            gf.getNavigation().startMovingTo(
                    currentTarget.getX() + 0.5, currentTarget.getY(), currentTarget.getZ() + 0.5, 1.1);
            return;
        }

        // Look at the block
        gf.getLookControl().lookAt(
                currentTarget.getX() + 0.5,
                currentTarget.getY() + 0.5,
                currentTarget.getZ() + 0.5, 30f, 30f);

        // Break progress
        breakProgress++;
        if (breakProgress >= BREAK_TICKS) {
            breakProgress = 0;
            // Break the block and drop loot
            if (world instanceof ServerWorld sw) {
                BlockState state = sw.getBlockState(currentTarget);
                sw.breakBlock(currentTarget, true, gf); // drop items
            }
            currentTarget = null;
        }
    }

    @Override
    public void stop() {
        currentTarget = null;
        breakProgress = 0;
        gf.getNavigation().stop();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private BlockPos findNearestMatchingBlock(World world) {
        BlockPos center = gf.getOwner() != null
                ? gf.getOwner().getBlockPos()
                : gf.getBlockPos();

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterateOutwards(center, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS)) {
            if (world.getBlockState(pos).getBlock() == targetBlockType) {
                double d = gf.getPos().squaredDistanceTo(
                        net.minecraft.util.math.Vec3d.ofCenter(pos));
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.toImmutable();
                }
            }
        }
        return best;
    }

    private void notifyOwner(String msg) {
        if (gf.getOwner() instanceof ServerPlayerEntity p) {
            p.sendMessage(Text.literal(msg), false);
        }
    }
}
