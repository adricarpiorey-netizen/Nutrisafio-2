package com.nutrisafio.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Quibungo: el mob mas peligroso.
 * - 100 de vida (50 corazones)
 * - Un poco mas rapido que un zombi (0.28; zombi 0.23, zombi bebe 0.345)
 * - Cuando ataca, AGARRA a su victima: la sostiene frente a su boca 2 segundos,
 *   la sacude y la muerde 4 veces (3 de dano cada mordida = 6 corazones), y luego la lanza.
 * - Despues de soltarte espera 5 segundos antes de poder agarrar otra vez (mientras tanto pega normal).
 * - Suelta Ojos Malditos, Huesos de Wither y un Diente de Quibungo.
 */
public class QuibungoEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_GRABBING =
            SynchedEntityData.defineId(QuibungoEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int GRAB_TICKS = 40;       // 2 segundos
    public static final int BITE_EVERY = 10;       // muerde cada medio segundo (4 mordidas)
    public static final float BITE_DAMAGE = 3.0F;  // 4 x 3 = 12 = 6 corazones
    public static final int GRAB_COOLDOWN = 100;   // 5 segundos entre agarres

    private LivingEntity grabbed;
    private int grabTicks;
    private int grabCooldown;

    public QuibungoEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GRABBING, false);
    }

    /** Para la animacion: esta sacudiendo a alguien. */
    public boolean isGrabbing() {
        return this.entityData.get(DATA_GRABBING);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.grabbed != null) return false;
        if (this.grabCooldown <= 0 && target instanceof LivingEntity living && !this.level().isClientSide) {
            startGrab(living);
            return true;
        }
        return super.doHurtTarget(target);
    }

    private void startGrab(LivingEntity target) {
        this.grabbed = target;
        this.grabTicks = GRAB_TICKS;
        this.entityData.set(DATA_GRABBING, true);
        this.getNavigation().stop();
        this.playSound(SoundEvents.RAVAGER_ROAR, 2.0F, 0.8F);
    }

    private void releaseGrab(boolean throwIt) {
        LivingEntity victim = this.grabbed;
        this.grabbed = null;
        this.grabTicks = 0;
        this.grabCooldown = GRAB_COOLDOWN;
        this.entityData.set(DATA_GRABBING, false);
        if (throwIt && victim != null && victim.isAlive()) {
            Vec3 f = forward();
            victim.setDeltaMovement(f.x * 1.2, 0.5, f.z * 1.2);
            victim.hurtMarked = true;
        }
    }

    private Vec3 forward() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        if (this.grabCooldown > 0) this.grabCooldown--;
        if (this.grabbed == null) return;

        LivingEntity v = this.grabbed;
        boolean invalid = !this.isAlive() || !v.isAlive() || v.isRemoved() || v.level() != this.level()
                || (v instanceof Player p && (p.isCreative() || p.isSpectator()));
        if (invalid) {
            releaseGrab(false);
            return;
        }

        this.getNavigation().stop();
        this.grabTicks--;
        // Sostener a la victima frente a la boca y sacudirla de lado a lado
        Vec3 f = forward();
        Vec3 side = new Vec3(-f.z, 0, f.x);
        double shake = Mth.sin(this.grabTicks * 1.3F) * 0.45;
        double bob = Math.abs(Mth.sin(this.grabTicks * 0.9F)) * 0.25;
        double x = this.getX() + f.x * 1.7 + side.x * shake;
        double y = this.getY() + 1.3 + bob;
        double z = this.getZ() + f.z * 1.7 + side.z * shake;
        v.teleportTo(x, y, z);
        v.setDeltaMovement(Vec3.ZERO);
        v.resetFallDistance();

        // Mordidas
        if (this.grabTicks % BITE_EVERY == 0) {
            v.invulnerableTime = 0;
            v.hurt(this.damageSources().mobAttack(this), BITE_DAMAGE);
            this.level().playSound(null, x, y, z, SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE, 1.0F, 0.7F);
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                        x, y + 1.0, z, 4, 0.2, 0.3, 0.2, 0.1);
            }
        }
        if (this.grabTicks <= 0) {
            releaseGrab(true);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (this.grabbed != null) releaseGrab(false);
        super.die(source);
    }

    // Sonidos de devastador (ravager), mas graves
    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.RAVAGER_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.RAVAGER_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }

    @Override
    public float getVoicePitch() { return 0.6F + this.random.nextFloat() * 0.1F; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.5F, 0.7F);
    }
}
