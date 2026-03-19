package com.beckytidus.girlfriendmod.entity;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.dialogue.DialogueData;
import com.beckytidus.girlfriendmod.dialogue.WaitAndFollowLines;
import com.beckytidus.girlfriendmod.goal.MimicBreakGoal;
import com.beckytidus.girlfriendmod.goal.SleepGoal;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GirlFriendEntity extends TameableEntity {

    // ── Persistent data keys ───────────────────────────────────────────────
    private static final String NBT_SKIN        = "GfSkin";
    private static final String NBT_NAME        = "GfName";
    private static final String NBT_HUNGER      = "GfHunger";
    private static final String NBT_LAST_SEEN   = "GfLastSeenDay";
    private static final String NBT_HUG_COOLDOWN= "GfHugCooldown";
    private static final String NBT_AFFECTION   = "GfAffection";   // 0-100

    // ── Fields ─────────────────────────────────────────────────────────────
    private int skinIndex      = 0;
    private String customName2 = "";   // the entity's chosen name (avoid clash with vanilla)
    private int hunger         = 20;   // max 20
    private long lastSeenDay   = -1;
    private int hugCooldownTicks = 0;
    private int affection      = 50;

    // client-side blushing flag (set via packet / NBT)
    public boolean isBlushing  = false;

    // combat retreat state
    private int retreatTicks   = 0;
    private static final int RETREAT_DISTANCE_WARN   = 6;
    private static final int RETREAT_DISTANCE_FULL   = 12;
    private static final float RETREAT_DMG_WARN      = 5.0f;
    private static final float RETREAT_DMG_FULL      = 10.0f;
    private float damageDealtSinceLastCheck = 0f;
    private int damageCheckTimer = 0;

    // nature interaction cooldown
    private int natureInteractCooldown = 0;

    // sleep
    private SleepGoal sleepGoal;

    // block-breaking helper
    private MimicBreakGoal mimicBreakGoal;

    // dialogue / chat
    private final DialogueData dialogueData = new DialogueData();
    private final DelayedChatReply delayedReply = new DelayedChatReply();
    private final Random rng = new Random();

    // ── Constructor ────────────────────────────────────────────────────────
    public GirlFriendEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
        this.setTamed(false);
    }

    // ── Attributes ─────────────────────────────────────────────────────────
    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    // ── Goals ──────────────────────────────────────────────────────────────
    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        sleepGoal = new SleepGoal(this);
        this.goalSelector.add(2, sleepGoal);
        mimicBreakGoal = new MimicBreakGoal(this);
        this.goalSelector.add(3, mimicBreakGoal);
        this.goalSelector.add(4, new ClimbBlocksGoal(this));
        this.goalSelector.add(5, new OpenDoorGoal(this, true));
        this.goalSelector.add(6, new RetreatGoal(this));
        this.goalSelector.add(7, new FollowOwnerGoal(this, 1.2, 4.0f, 2.0f));
        this.goalSelector.add(8, new NatureInteractGoal(this));
        this.goalSelector.add(9, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(11, new LookAroundGoal(this));

        // Target whoever attacks the owner OR whoever attacks her
        this.targetSelector.add(1, new DefendOwnerGoal(this));
        this.targetSelector.add(2, new GirlfriendRevengeGoal(this));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MobEntity.class, true,
                mob -> mob.getTarget() instanceof PlayerEntity player
                        && player.getUuid().equals(this.getOwnerUuid())));
    }

    public MimicBreakGoal getMimicBreakGoal() { return mimicBreakGoal; }

    // ── Tick ───────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            tickClientEffects();
            return;
        }

        // ── Hunger drain (slow: 1 point every 5 min) ────────────────────
        if (this.age % 6000 == 0 && hunger > 0) {
            hunger--;
        }

        // ── Health regen (1 HP every 30s if hunger > 5) ─────────────────
        if (this.age % 600 == 0 && hunger > 5 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
            hunger = Math.max(0, hunger - 1);
        }

        // ── Hug cooldown ────────────────────────────────────────────────
        if (hugCooldownTicks > 0) hugCooldownTicks--;

        // ── Damage tracking for retreat ─────────────────────────────────
        damageCheckTimer++;
        if (damageCheckTimer >= 60) {
            damageCheckTimer = 0;
            damageDealtSinceLastCheck = 0;
        }

        // ── Nature interactions ──────────────────────────────────────────
        if (natureInteractCooldown > 0) natureInteractCooldown--;

        // ── Proximity affection / greetings ─────────────────────────────
        checkOwnerProximityAffection();

        // ── Delayed chat replies ─────────────────────────────────────────
        delayedReply.tick(this);
    }

    // Client-side tick: blush particles instead of hearts
    private void tickClientEffects() {
        if (isBlushing) {
            World w = this.getWorld();
            double x = this.getX() + (rng.nextDouble() - 0.5) * 0.6;
            double y = this.getY() + this.getHeight() * 0.9 + rng.nextDouble() * 0.2;
            double z = this.getZ() + (rng.nextDouble() - 0.5) * 0.6;
            // Use villager happy particles (green sparkles) as cheek blush proxy
            // In a full build with a custom renderer you'd draw red quads on the face
            w.addParticle(ParticleTypes.VILLAGER_HAPPY, x, y, z, 0, 0.05, 0);
        }
    }

    // ── Owner proximity → hug / kiss logic ────────────────────────────────
    private void checkOwnerProximityAffection() {
        LivingEntity owner = this.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) return;
        if (hugCooldownTicks > 0) return;

        double dist = this.distanceTo(player);
        if (dist > 3.0) return; // must be close

        World w = this.getWorld();
        long currentDay = w.getTimeOfDay() / 24000L;
        long daysSinceLastSeen = (lastSeenDay < 0) ? 0 : currentDay - lastSeenDay;

        if (daysSinceLastSeen > 2) {
            // More than 2 days — hug + kiss + worried scolding
            performHug(player);
            performKiss(player);
            String[] lines = {
                "§d* " + getDisplayName2() + " rushes to you and hugs you tight, then pulls back to look at you. *\n§e\"Thank God you're back... I was so worried. Please don't take that long again, okay?\"",
                "§d* " + getDisplayName2() + " wraps her arms around you and kisses you, eyes watery. *\n§e\"Where were you?! Do you have any idea how long that was? Don't ever do that to me again.\"",
                "§d* " + getDisplayName2() + " hugs you hard, then kisses you before stepping back with a serious look. *\n§e\"You're safe... good. But seriously — why did you take so long? Next time tell me first.\""
            };
            player.sendMessage(Text.literal(lines[rng.nextInt(lines.length)]), false);
            lastSeenDay = currentDay;
            affection = Math.min(100, affection + 10);
            hugCooldownTicks = 2400;
            isBlushing = true;

        } else if (daysSinceLastSeen == 2) {
            // Exactly 2 days — kiss only
            performKiss(player);
            String[] lines = {
                "§d* " + getDisplayName2() + " kisses you softly and smiles with relief. *\n§e\"Two whole days... I missed you more than I expected.\"",
                "§d* " + getDisplayName2() + " leans in and kisses you, then sighs. *\n§e\"Don't disappear for that long again. I kept thinking about you.\"",
                "§d* " + getDisplayName2() + " kisses you and holds your hand for a moment. *\n§e\"You're finally back. Two days felt like forever, you know?\""
            };
            player.sendMessage(Text.literal(lines[rng.nextInt(lines.length)]), false);
            lastSeenDay = currentDay;
            affection = Math.min(100, affection + 7);
            hugCooldownTicks = 2400;
            isBlushing = true;

        } else if (daysSinceLastSeen == 1) {
            // 1 day — hug + relieved message
            performHug(player);
            String[] lines = {
                "§d* " + getDisplayName2() + " hugs you warmly. *\n§e\"Thank God you returned safely! I was starting to worry.\"",
                "§d* " + getDisplayName2() + " pulls you into a hug. *\n§e\"There you are! I'm so glad you're okay. Don't make me wait like that.\"",
                "§d* " + getDisplayName2() + " hugs you tightly. *\n§e\"You're back! Safe and sound... thank goodness. I missed you.\""
            };
            player.sendMessage(Text.literal(lines[rng.nextInt(lines.length)]), false);
            lastSeenDay = currentDay;
            affection = Math.min(100, affection + 5);
            hugCooldownTicks = 2400;
            isBlushing = true;

        } else {
            // Same day — no automatic affection, only chat replies
            lastSeenDay = currentDay;
        }
    }

    private void performHug(PlayerEntity player) {
        this.getLookControl().lookAt(player, 30f, 30f);
        this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.3f, 1.8f);
        spawnHeartlikeParticles();
    }

    private void performKiss(PlayerEntity player) {
        this.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.2f, 2.0f);
        spawnHeartlikeParticles();
    }

    /** Spawn blush-style particles (no heart particles). */
    private void spawnHeartlikeParticles() {
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.VILLAGER_HAPPY,
                    this.getX(), this.getY() + this.getHeight(), this.getZ(),
                    6, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // ── Damage dealt tracking (called by goals/mixin) ─────────────────────
    public void recordDamageDealt(float amount) {
        damageDealtSinceLastCheck += amount;
    }

    public float getDamageDealtRecently() {
        return damageDealtSinceLastCheck;
    }

    // ── Chat / dialogue ────────────────────────────────────────────────────
    public void receiveChat(ServerPlayerEntity player, String message) {
        // Sleepy reply if currently sleeping
        if (sleepGoal != null && sleepGoal.isSleeping()) {
            String[] sleepy = SleepGoal.SLEEPY_REPLIES;
            String reply = sleepy[rng.nextInt(sleepy.length)];
            int delay = 10 + rng.nextInt(20);
            delayedReply.schedule(player, reply, delay);
            return;
        }
        float roll = rng.nextFloat();
        String reply = dialogueData.pickReply(message, this, roll);
        if (reply != null && !reply.isEmpty()) {
            int delayTicks = 20 + rng.nextInt(40);
            delayedReply.schedule(player, getDisplayName2() + ": " + reply, delayTicks);
        }
    }

    // ── Interaction ────────────────────────────────────────────────────────
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient && player instanceof ServerPlayerEntity sp) {
            ItemStack held = player.getStackInHand(hand);
            if (held.isEmpty()) {
                // Right-click with empty hand: show status
                sp.sendMessage(Text.literal("§e[" + getDisplayName2() + "] §fHunger: " + hunger + "/20 | HP: "
                        + (int) this.getHealth() + "/" + (int) this.getMaxHealth()
                        + " | Affection: " + affection + "/100"), false);
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }

    // ── NBT ────────────────────────────────────────────────────────────────
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(NBT_SKIN, skinIndex);
        nbt.putString(NBT_NAME, customName2);
        nbt.putInt(NBT_HUNGER, hunger);
        nbt.putLong(NBT_LAST_SEEN, lastSeenDay);
        nbt.putInt(NBT_HUG_COOLDOWN, hugCooldownTicks);
        nbt.putInt(NBT_AFFECTION, affection);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        skinIndex        = nbt.getInt(NBT_SKIN);
        customName2      = nbt.getString(NBT_NAME);
        hunger           = nbt.contains(NBT_HUNGER)      ? nbt.getInt(NBT_HUNGER)      : 20;
        lastSeenDay      = nbt.contains(NBT_LAST_SEEN)   ? nbt.getLong(NBT_LAST_SEEN)  : -1;
        hugCooldownTicks = nbt.contains(NBT_HUG_COOLDOWN)? nbt.getInt(NBT_HUG_COOLDOWN): 0;
        affection        = nbt.contains(NBT_AFFECTION)   ? nbt.getInt(NBT_AFFECTION)   : 50;
    }

    // ── Getters / setters ──────────────────────────────────────────────────
    public int  getSkinIndex()              { return skinIndex; }
    public void setSkinIndex(int i)         { this.skinIndex = i; }
    public String getDisplayName2()         { return customName2.isEmpty() ? "Her" : customName2; }
    public void setCustomName2(String n)    { this.customName2 = n; }
    public int  getHunger()                 { return hunger; }
    public void setHunger(int h)            { this.hunger = Math.max(0, Math.min(20, h)); }
    public int  getAffection()              { return affection; }
    public void addAffection(int delta)     { affection = Math.max(0, Math.min(100, affection + delta)); }

    // ══════════════════════════════════════════════════════════════════════
    //  INNER GOAL CLASSES
    // ══════════════════════════════════════════════════════════════════════

    // ── 1. ClimbBlocksGoal – place a block to step up a 2-block wall ───────
    static class ClimbBlocksGoal extends Goal {
        private final GirlFriendEntity gf;
        private int cooldown = 0;

        ClimbBlocksGoal(GirlFriendEntity gf) {
            this.gf = gf;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (cooldown-- > 0) return false;
            if (gf.getOwner() == null) return false;
            // Check if blocked by a 2-block wall directly ahead
            return isBlockedByWall();
        }

        private boolean isBlockedByWall() {
            Vec3d vel = gf.getVelocity();
            if (vel.horizontalLengthSquared() < 0.01) return false;
            BlockPos front = gf.getBlockPos().offset(gf.getHorizontalFacing());
            World w = gf.getWorld();
            return w.getBlockState(front).isFullCube(w, front) &&
                   w.getBlockState(front.up()).isFullCube(w, front.up()) &&
                   w.getBlockState(front.up(2)).isAir();
        }

        @Override
        public void start() {
            // Place a dirt block at foot level to step onto
            World w = gf.getWorld();
            BlockPos stepPos = gf.getBlockPos().offset(gf.getHorizontalFacing());
            BlockPos above = stepPos.up();
            if (w.getBlockState(above).isAir()) {
                w.setBlockState(above, Blocks.DIRT.getDefaultState());
            }
            cooldown = 100;
        }

        @Override
        public boolean shouldContinue() { return false; }
    }

    // ── 2. FollowOwnerGoal ─────────────────────────────────────────────────
    static class FollowOwnerGoal extends Goal {
        private final GirlFriendEntity gf;
        private final double speed;
        private final float minDist, maxDist;
        private LivingEntity owner;
        private int updateTimer;

        FollowOwnerGoal(GirlFriendEntity gf, double speed, float maxDist, float minDist) {
            this.gf = gf; this.speed = speed; this.maxDist = maxDist; this.minDist = minDist;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            owner = gf.getOwner();
            if (owner == null || gf.isSitting()) return false;
            return gf.distanceTo(owner) > maxDist;
        }

        @Override
        public boolean shouldContinue() {
            return owner != null && !gf.isSitting() && gf.distanceTo(owner) > minDist;
        }

        @Override
        public void start() { updateTimer = 0; }

        @Override
        public void tick() {
            gf.getLookControl().lookAt(owner, 10f, gf.getMaxLookPitchChange());
            if (--updateTimer <= 0) {
                updateTimer = 10;
                gf.getNavigation().startMovingTo(owner, speed);
            }
        }
    }

    // ── 3. DefendOwnerGoal ─────────────────────────────────────────────────
    static class DefendOwnerGoal extends TargetGoal {
        private final GirlFriendEntity gf;
        private LivingEntity attacker;

        DefendOwnerGoal(GirlFriendEntity gf) {
            super(gf, false);
            this.gf = gf;
            this.setControls(EnumSet.of(Control.TARGET));
        }

        @Override
        public boolean canStart() {
            LivingEntity owner = gf.getOwner();
            if (owner == null) return false;
            attacker = owner.getAttacker();
            return attacker != null && attacker != gf;
        }

        @Override
        public void start() { gf.setTarget(attacker); super.start(); }
    }

    // ── 4. GirlfriendRevengeGoal ───────────────────────────────────────────
    static class GirlfriendRevengeGoal extends RevengeGoal {
        GirlfriendRevengeGoal(GirlFriendEntity gf) {
            super(gf);
        }
    }

    // ── 5. RetreatGoal – maintain distance or flee based on damage dealt ───
    static class RetreatGoal extends Goal {
        private final GirlFriendEntity gf;
        private LivingEntity target;

        RetreatGoal(GirlFriendEntity gf) {
            this.gf = gf;
            this.setControls(EnumSet.of(Control.MOVE));
        }

        @Override
        public boolean canStart() {
            target = gf.getTarget();
            if (target == null) return false;
            float dmg = gf.getDamageDealtRecently();
            return dmg >= RETREAT_DMG_WARN;
        }

        @Override
        public boolean shouldContinue() {
            if (target == null || !target.isAlive()) return false;
            float dmg = gf.getDamageDealtRecently();
            return dmg >= RETREAT_DMG_WARN;
        }

        @Override
        public void tick() {
            if (target == null) return;
            float dmg = gf.getDamageDealtRecently();
            double dist = gf.distanceTo(target);

            if (dmg >= RETREAT_DMG_FULL) {
                // Full retreat – run away to owner
                LivingEntity owner = gf.getOwner();
                if (owner != null) {
                    gf.getNavigation().startMovingTo(owner, 1.5);
                }
            } else if (dmg >= RETREAT_DMG_WARN && dist < RETREAT_DISTANCE_WARN) {
                // Back away to safe distance
                Vec3d away = gf.getPos().subtract(target.getPos()).normalize()
                        .multiply(RETREAT_DISTANCE_WARN);
                gf.getNavigation().startMovingTo(
                        gf.getX() + away.x, gf.getY(), gf.getZ() + away.z, 1.2);
            }
        }
    }

    // ── 6. NatureInteractGoal – interact with cats and villagers ──────────
    static class NatureInteractGoal extends Goal {
        private final GirlFriendEntity gf;
        private Entity nearby;
        private int cooldown = 0;

        NatureInteractGoal(GirlFriendEntity gf) {
            this.gf = gf;
        }

        @Override
        public boolean canStart() {
            if (gf.getOwner() == null) return false;
            if (cooldown-- > 0) return false;
            if (gf.isSitting()) return false;
            World w = gf.getWorld();
            Box box = gf.getBoundingBox().expand(8.0);
            List<Entity> candidates = w.getOtherEntities(gf, box,
                    e -> e instanceof CatEntity || e instanceof VillagerEntity);
            if (candidates.isEmpty()) return false;
            nearby = candidates.get(gf.getRandom().nextInt(candidates.size()));
            return true;
        }

        @Override
        public void start() {
            if (nearby != null) {
                gf.getNavigation().startMovingTo(nearby, 0.9);
            }
        }

        @Override
        public boolean shouldContinue() {
            return nearby != null && nearby.isAlive() && gf.distanceTo(nearby) > 2.5;
        }

        @Override
        public void stop() {
            if (nearby != null && gf.distanceTo(nearby) <= 3.0) {
                // Send a little chat line when she reaches the animal/villager
                LivingEntity owner = gf.getOwner();
                if (owner instanceof ServerPlayerEntity sp) {
                    String line = (nearby instanceof CatEntity)
                            ? "§7* " + gf.getDisplayName2() + " gently pets the cat... *"
                            : "§7* " + gf.getDisplayName2() + " waves at the villager. *";
                    sp.sendMessage(Text.literal(line), false);
                }
            }
            nearby = null;
            cooldown = 1200 + gf.getRandom().nextInt(1200); // 1-2 min
        }
    }
}
