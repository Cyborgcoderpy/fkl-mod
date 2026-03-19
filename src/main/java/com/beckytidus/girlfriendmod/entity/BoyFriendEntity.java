package com.beckytidus.girlfriendmod.entity;

import com.beckytidus.girlfriendmod.dialogue.BoyDialogueData;
import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.goal.MimicBreakGoal;
import com.beckytidus.girlfriendmod.goal.SleepGoal;
import com.beckytidus.girlfriendmod.registry.BoyFriendSkins;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class BoyFriendEntity extends TameableEntity {

    // ── NBT keys ───────────────────────────────────────────────────────────
    private static final String NBT_SKIN        = "BfSkin";
    private static final String NBT_NAME        = "BfName";
    private static final String NBT_HUNGER      = "BfHunger";
    private static final String NBT_LAST_SEEN   = "BfLastSeenDay";
    private static final String NBT_GREET_CD    = "BfGreetCooldown";

    // ── Fields ─────────────────────────────────────────────────────────────
    private int skinIndex        = 0;
    private String customName2   = "";
    private int hunger           = 20;
    private long lastSeenDay     = -1;
    private int greetCooldown    = 0;

    // combat
    private float damageDealtRecently = 0f;
    private int   damageCheckTimer    = 0;
    private static final float DMG_WARN = 5.0f;
    private static final float DMG_FULL = 10.0f;
    private static final int   DIST_WARN = 6;

    // goals
    private SleepGoal      sleepGoal;
    private MimicBreakGoal mimicBreakGoal;

    // dialogue
    private final BoyDialogueData  dialogue    = new BoyDialogueData();
    private final DelayedChatReply delayedReply = new DelayedChatReply();
    private final Random rng = new Random();

    // ── Constructor ────────────────────────────────────────────────────────
    public BoyFriendEntity(EntityType<? extends TameableEntity> type, World world) {
        super(type, world);
        this.setTamed(false);
    }

    // ── Attributes ─────────────────────────────────────────────────────────
    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.36)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)   // slightly stronger
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

        this.targetSelector.add(1, new DefendOwnerGoal(this));
        this.targetSelector.add(2, new RevengeGoal(this));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, MobEntity.class, true,
                mob -> mob.getTarget() instanceof PlayerEntity p
                        && p.getUuid().equals(this.getOwnerUuid())));
    }

    // ── Tick ───────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) return;

        // Hunger drain
        if (this.age % 6000 == 0 && hunger > 0) hunger--;

        // Health regen
        if (this.age % 600 == 0 && hunger > 5 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0f);
            hunger = Math.max(0, hunger - 1);
        }

        // Greet cooldown
        if (greetCooldown > 0) greetCooldown--;

        // Damage tracking
        if (++damageCheckTimer >= 60) {
            damageCheckTimer = 0;
            damageDealtRecently = 0;
        }

        // Reunion check
        checkOwnerReunion();

        // Delayed replies
        delayedReply.tick(this);
    }

    // ── Reunion — friendly bro greetings, NO romance ───────────────────────
    private void checkOwnerReunion() {
        LivingEntity owner = this.getOwner();
        if (!(owner instanceof ServerPlayerEntity player)) return;
        if (greetCooldown > 0) return;
        if (this.distanceTo(player) > 3.0) return;

        World w = this.getWorld();
        long currentDay  = w.getTimeOfDay() / 24000L;
        long daysAway    = (lastSeenDay < 0) ? 0 : currentDay - lastSeenDay;

        if (daysAway > 2) {
            // More than 2 days — worried bro energy
            String[] lines = {
                "§b* " + getDisplayName2() + " jogs over and gives you a firm pat on the back. *\n§f\"BRO. Where were you?! I was actually worried. Don't do that again, seriously.\"",
                "§b* " + getDisplayName2() + " looks visibly relieved to see you. *\n§f\"Finally! I thought something happened to you. Took you way too long man, give me a heads up next time!\"",
                "§b* " + getDisplayName2() + " walks up and punches your shoulder lightly. *\n§f\"There he is! Thought I'd lost you. What took so long? Don't disappear like that.\""
            };
            player.sendMessage(Text.literal(lines[rng.nextInt(lines.length)]), false);
            greetCooldown = 2400;
            affectionParticles();

        } else if (daysAway == 2) {
            // 2 days — relieved
            String[] lines = {
                "§b* " + getDisplayName2() + " nods with relief. *\n§f\"Two days man. Didn't know where you went. Glad you're back safe though.\"",
                "§b* " + getDisplayName2() + " waves you over. *\n§f\"Finally! Two whole days. I held things down here, don't worry. Welcome back.\"",
                "§b* " + getDisplayName2() + " looks up. *\n§f\"Oh thank god. Was starting to think you weren't coming back. Two days is a long time bro.\""
            };
            player.sendMessage(Text.literal(lines[rng.nextInt(lines.length)]), false);
            greetCooldown = 2400;
            affectionParticles();

        } else if (daysAway == 1) {
            // 1 day — casual friendly
            String[] lines = {
                "§b* " + getDisplayName2() + " gives a wave. *\n§f\"Hey! Glad you made it back safe. Was wondering when you'd show up.\"",
                "§b* " + getDisplayName2() + " looks up from what he was doing. *\n§f\"There you are! Thank goodness. Was starting to get a little worried not gonna lie.\"",
                "§b* " + getDisplayName2() + " walks over. *\n§f\"Welcome back man. You good? Everything go okay out there?\""
            };
            player.sendMessage(Text.literal(lines[rng.nextInt(lines.length)]), false);
            greetCooldown = 2400;

        } else {
            // Same day — no greeting
        }

        lastSeenDay = currentDay;
    }

    private void affectionParticles() {
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY() + this.getHeight(), this.getZ(),
                    4, 0.3, 0.3, 0.3, 0.05);
        }
    }

    // ── Chat ──────────────────────────────────────────────────────────────
    public void receiveChat(ServerPlayerEntity player, String message) {
        if (sleepGoal != null && sleepGoal.isSleeping()) {
            String[] sleepy = SleepGoal.SLEEPY_REPLIES;
            delayedReply.schedule(player, sleepy[rng.nextInt(sleepy.length)], 10 + rng.nextInt(20));
            return;
        }
        float roll = rng.nextFloat();
        String reply = dialogue.pickReply(message, this, roll);
        if (reply != null && !reply.isEmpty()) {
            delayedReply.schedule(player, getDisplayName2() + ": " + reply, 20 + rng.nextInt(40));
        }
    }

    // ── Interaction ────────────────────────────────────────────────────────
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient && player instanceof ServerPlayerEntity sp) {
            if (player.getStackInHand(hand).isEmpty()) {
                sp.sendMessage(Text.literal("§b[" + getDisplayName2() + "] §fHunger: " + hunger
                        + "/20 | HP: " + (int) this.getHealth() + "/" + (int) this.getMaxHealth()), false);
                return ActionResult.SUCCESS;
            }
        }
        return super.interactMob(player, hand);
    }

    // ── Damage tracking ────────────────────────────────────────────────────
    public void recordDamageDealt(float amount) { damageDealtRecently += amount; }
    public float getDamageDealtRecently()        { return damageDealtRecently; }

    // ── NBT ────────────────────────────────────────────────────────────────
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt(NBT_SKIN, skinIndex);
        nbt.putString(NBT_NAME, customName2);
        nbt.putInt(NBT_HUNGER, hunger);
        nbt.putLong(NBT_LAST_SEEN, lastSeenDay);
        nbt.putInt(NBT_GREET_CD, greetCooldown);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        skinIndex     = nbt.getInt(NBT_SKIN);
        customName2   = nbt.getString(NBT_NAME);
        hunger        = nbt.contains(NBT_HUNGER)    ? nbt.getInt(NBT_HUNGER)    : 20;
        lastSeenDay   = nbt.contains(NBT_LAST_SEEN) ? nbt.getLong(NBT_LAST_SEEN): -1;
        greetCooldown = nbt.contains(NBT_GREET_CD)  ? nbt.getInt(NBT_GREET_CD)  : 0;
    }

    // ── Getters / setters ──────────────────────────────────────────────────
    public int    getSkinIndex()            { return skinIndex; }
    public void   setSkinIndex(int i)       { this.skinIndex = i; }
    public String getDisplayName2()         { return customName2.isEmpty() ? "Him" : customName2; }
    public void   setCustomName2(String n)  { this.customName2 = n; }
    public int    getHunger()               { return hunger; }
    public void   setHunger(int h)          { this.hunger = Math.max(0, Math.min(20, h)); }
    public MimicBreakGoal getMimicBreakGoal() { return mimicBreakGoal; }

    // ══════════════════════════════════════════════════════════════════════
    //  INNER GOAL CLASSES  (identical logic to GirlFriendEntity goals)
    // ══════════════════════════════════════════════════════════════════════

    static class ClimbBlocksGoal extends Goal {
        private final BoyFriendEntity bf;
        private int cooldown = 0;
        ClimbBlocksGoal(BoyFriendEntity bf) {
            this.bf = bf;
            this.setControls(EnumSet.of(Control.MOVE));
        }
        @Override public boolean canStart() {
            if (cooldown-- > 0) return false;
            if (bf.getOwner() == null) return false;
            Vec3d vel = bf.getVelocity();
            if (vel.horizontalLengthSquared() < 0.01) return false;
            BlockPos front = bf.getBlockPos().offset(bf.getHorizontalFacing());
            World w = bf.getWorld();
            return w.getBlockState(front).isFullCube(w, front)
                && w.getBlockState(front.up()).isFullCube(w, front.up())
                && w.getBlockState(front.up(2)).isAir();
        }
        @Override public void start() {
            World w = bf.getWorld();
            BlockPos above = bf.getBlockPos().offset(bf.getHorizontalFacing()).up();
            if (w.getBlockState(above).isAir()) w.setBlockState(above, Blocks.DIRT.getDefaultState());
            cooldown = 100;
        }
        @Override public boolean shouldContinue() { return false; }
    }

    static class FollowOwnerGoal extends Goal {
        private final BoyFriendEntity bf;
        private final double speed;
        private final float minDist, maxDist;
        private LivingEntity owner;
        private int timer;
        FollowOwnerGoal(BoyFriendEntity bf, double speed, float maxDist, float minDist) {
            this.bf = bf; this.speed = speed; this.maxDist = maxDist; this.minDist = minDist;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }
        @Override public boolean canStart() {
            owner = bf.getOwner();
            return owner != null && !bf.isSitting() && bf.distanceTo(owner) > maxDist;
        }
        @Override public boolean shouldContinue() {
            return owner != null && !bf.isSitting() && bf.distanceTo(owner) > minDist;
        }
        @Override public void start() { timer = 0; }
        @Override public void tick() {
            bf.getLookControl().lookAt(owner, 10f, bf.getMaxLookPitchChange());
            if (--timer <= 0) { timer = 10; bf.getNavigation().startMovingTo(owner, speed); }
        }
    }

    static class DefendOwnerGoal extends TargetGoal {
        private final BoyFriendEntity bf;
        private LivingEntity attacker;
        DefendOwnerGoal(BoyFriendEntity bf) {
            super(bf, false);
            this.bf = bf;
            this.setControls(EnumSet.of(Control.TARGET));
        }
        @Override public boolean canStart() {
            LivingEntity owner = bf.getOwner();
            if (owner == null) return false;
            attacker = owner.getAttacker();
            return attacker != null && attacker != bf;
        }
        @Override public void start() { bf.setTarget(attacker); super.start(); }
    }

    static class RetreatGoal extends Goal {
        private final BoyFriendEntity bf;
        private LivingEntity target;
        RetreatGoal(BoyFriendEntity bf) {
            this.bf = bf;
            this.setControls(EnumSet.of(Control.MOVE));
        }
        @Override public boolean canStart() {
            target = bf.getTarget();
            return target != null && bf.getDamageDealtRecently() >= DMG_WARN;
        }
        @Override public boolean shouldContinue() {
            return target != null && target.isAlive() && bf.getDamageDealtRecently() >= DMG_WARN;
        }
        @Override public void tick() {
            if (target == null) return;
            if (bf.getDamageDealtRecently() >= DMG_FULL) {
                LivingEntity owner = bf.getOwner();
                if (owner != null) bf.getNavigation().startMovingTo(owner, 1.5);
            } else if (bf.distanceTo(target) < DIST_WARN) {
                Vec3d away = bf.getPos().subtract(target.getPos()).normalize().multiply(DIST_WARN);
                bf.getNavigation().startMovingTo(bf.getX() + away.x, bf.getY(), bf.getZ() + away.z, 1.2);
            }
        }
    }

    static class NatureInteractGoal extends Goal {
        private final BoyFriendEntity bf;
        private Entity nearby;
        private int cooldown = 0;
        NatureInteractGoal(BoyFriendEntity bf) { this.bf = bf; }
        @Override public boolean canStart() {
            if (bf.getOwner() == null || cooldown-- > 0 || bf.isSitting()) return false;
            List<Entity> candidates = bf.getWorld().getOtherEntities(bf,
                    bf.getBoundingBox().expand(8.0),
                    e -> e instanceof CatEntity || e instanceof VillagerEntity);
            if (candidates.isEmpty()) return false;
            nearby = candidates.get(bf.getRandom().nextInt(candidates.size()));
            return true;
        }
        @Override public void start() {
            if (nearby != null) bf.getNavigation().startMovingTo(nearby, 0.9);
        }
        @Override public boolean shouldContinue() {
            return nearby != null && nearby.isAlive() && bf.distanceTo(nearby) > 2.5;
        }
        @Override public void stop() {
            if (nearby != null && bf.distanceTo(nearby) <= 3.0) {
                LivingEntity owner = bf.getOwner();
                if (owner instanceof ServerPlayerEntity sp) {
                    String line = (nearby instanceof CatEntity)
                            ? "§7* " + bf.getDisplayName2() + " crouches down and pets the cat. *"
                            : "§7* " + bf.getDisplayName2() + " gives the villager a nod. *";
                    sp.sendMessage(Text.literal(line), false);
                }
            }
            nearby = null;
            cooldown = 1200 + bf.getRandom().nextInt(1200);
        }
    }
}
