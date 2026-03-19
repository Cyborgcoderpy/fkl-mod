package com.beckytidus.girlfriendmod.goal;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Makes the girlfriend find a nearby bed at night and sleep in it.
 * She wakes at dawn, says goodnight when lying down, and gives
 * sleepy replies if chatted to while asleep.
 */
public class SleepGoal extends Goal {

    private static final int SEARCH_RADIUS  = 16;
    private static final int NIGHT_START    = 12542; // Minecraft time ticks
    private static final int DAWN           = 23460;

    private final GirlFriendEntity gf;
    private BlockPos targetBed   = null;
    private State    state       = State.IDLE;
    private int      sleepTicks  = 0;

    public enum State { IDLE, WALKING_TO_BED, SLEEPING }

    public SleepGoal(GirlFriendEntity gf) {
        this.gf = gf;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
    }

    // ── canStart ──────────────────────────────────────────────────────────
    @Override
    public boolean canStart() {
        if (gf.isSitting()) return false;
        if (gf.getTarget() != null) return false;          // don't sleep mid-fight
        if (!isNight(gf.getWorld())) return false;
        if (state == State.SLEEPING) return false;

        Optional<BlockPos> bed = findFreeBed();
        if (bed.isEmpty()) {
            notifyOwner("§7* " + gf.getDisplayName2() + " couldn't find a bed... *");
            return false;
        }
        targetBed = bed.get();
        return true;
    }

    @Override
    public boolean shouldContinue() {
        // Keep sleeping until dawn
        if (state == State.SLEEPING) {
            return !isDawn(gf.getWorld()) && isStillBed(targetBed);
        }
        // Keep walking to bed while it's night
        return isNight(gf.getWorld()) && targetBed != null;
    }

    // ── start ─────────────────────────────────────────────────────────────
    @Override
    public void start() {
        state = State.WALKING_TO_BED;
        sleepTicks = 0;
        gf.getNavigation().startMovingTo(
                targetBed.getX() + 0.5, targetBed.getY(), targetBed.getZ() + 0.5, 1.0);
    }

    // ── tick ──────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        if (targetBed == null) return;

        if (state == State.WALKING_TO_BED) {
            // Arrived at bed?
            double dist = gf.getPos().distanceTo(
                    net.minecraft.util.math.Vec3d.ofCenter(targetBed));
            if (dist < 2.0) {
                layDown();
            }
        } else if (state == State.SLEEPING) {
            sleepTicks++;
            // Keep entity on bed position
            gf.setPosition(targetBed.getX() + 0.5, targetBed.getY() + 0.2, targetBed.getZ() + 0.5);
            gf.setVelocity(0, 0, 0);

            // Check if owner just went to sleep too — say goodnight once
            if (sleepTicks == 20) {
                checkOwnerSleeping();
            }
        }
    }

    // ── stop ─────────────────────────────────────────────────────────────
    @Override
    public void stop() {
        if (state == State.SLEEPING) {
            wakeUp();
        }
        state     = State.IDLE;
        targetBed = null;
        gf.setSleeping(false);
    }

    // ── helpers ───────────────────────────────────────────────────────────
    private void layDown() {
        state = State.SLEEPING;
        gf.getNavigation().stop();
        gf.setSleeping(true);

        String[] goodnightLines = {
            "§7* " + gf.getDisplayName2() + " curls up in bed... *",
            "§7* " + gf.getDisplayName2() + " yawns and climbs into bed... *",
            "§7* " + gf.getDisplayName2() + " pulls the covers over herself... *",
            "§7* " + gf.getDisplayName2() + " whispers goodnight... *"
        };
        notifyOwner(goodnightLines[gf.getRandom().nextInt(goodnightLines.length)]);
    }

    private void wakeUp() {
        gf.setSleeping(false);
        String[] wakeLines = {
            "§7* " + gf.getDisplayName2() + " stretches and gets up. *",
            "§7* " + gf.getDisplayName2() + " yawns and rubs her eyes. *",
            "§7* " + gf.getDisplayName2() + " wakes up slowly. *"
        };
        notifyOwner(wakeLines[gf.getRandom().nextInt(wakeLines.length)]);
    }

    private void checkOwnerSleeping() {
        if (!(gf.getOwner() instanceof ServerPlayerEntity player)) return;
        if (player.isSleeping()) {
            notifyOwner("§d* " + gf.getDisplayName2() + " smiles seeing you asleep. Goodnight! *");
        } else {
            notifyOwner("§7* " + gf.getDisplayName2() + " says goodnight quietly... *");
        }
    }

    private void notifyOwner(String msg) {
        if (gf.getOwner() instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.literal(msg), false);
        }
    }

    /** Find a free bed (HEAD part, not occupied) within SEARCH_RADIUS. */
    private Optional<BlockPos> findFreeBed() {
        World world = gf.getWorld();
        BlockPos center = gf.getBlockPos();

        for (BlockPos pos : BlockPos.iterateOutwards(center, SEARCH_RADIUS, 4, SEARCH_RADIUS)) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BedBlock) {
                // Only target the HEAD part so we don't double-claim
                if (state.get(BedBlock.PART) == BedPart.HEAD) {
                    if (!state.get(BedBlock.OCCUPIED)) {
                        return Optional.of(pos.toImmutable());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private boolean isStillBed(BlockPos pos) {
        if (pos == null) return false;
        return gf.getWorld().getBlockState(pos).getBlock() instanceof BedBlock;
    }

    private static boolean isNight(World w) {
        long time = w.getTimeOfDay() % 24000;
        return time >= NIGHT_START || time < DAWN;
    }

    private static boolean isDawn(World w) {
        long time = w.getTimeOfDay() % 24000;
        return time >= DAWN || time < 1000;
    }

    // ── Public state query (for dialogue) ─────────────────────────────────
    public boolean isSleeping() { return state == State.SLEEPING; }

    /** Sleepy replies when chatted to while asleep. */
    public static final String[] SLEEPY_REPLIES = {
        "§7...zzz...",
        "§7*mumbles* ...five more minutes...",
        "§7*turns over* ...not now...",
        "§7...mmh?... *still asleep*",
        "§7*sleepy murmur* ...go away...",
        "§7zzz... *doesn't wake up*"
    };
}
