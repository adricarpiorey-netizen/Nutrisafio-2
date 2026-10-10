package com.nutrisafio.mobs;

import java.util.EnumSet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * Jester: un bufon que se acerca y explota como un creeper, pero mas rapido.
 * - 20 de vida (igual que un creeper)
 * - Un poco mas rapido que un zombi (0.27 vs 0.23)
 * - Mecha de 1 segundo (el creeper tarda 1.5 segundos)
 * - La explosion hace dano pero NO rompe bloques
 * - A los jugadores cercanos les llena la pantalla de confeti por 5 segundos
 */
public class JesterEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR =
            SynchedEntityData.defineId(JesterEntity.class, EntityDataSerializers.INT);

    public static final int MAX_SWELL = 20;          // 1 segundo (creeper: 30)
    public static final float EXPLOSION_POWER = 3.0F; // igual que un creeper
    public static final int CONFETTI_TICKS = 100;     // 5 segundos
    public static final double CONFETTI_RADIUS = 8.0;

    private int oldSwell;
    private int swell;

    public JesterEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new JesterSwellGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1);
    }

    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(int dir) {
        this.entityData.set(DATA_SWELL_DIR, dir);
    }

    /** Que tan "inflado" esta (0 a 1), para el efecto de crecer y parpadear. */
    public float getSwelling(float partialTick) {
        return Mth.lerp(partialTick, (float) this.oldSwell, (float) this.swell) / (float) (MAX_SWELL - 2);
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldSwell = this.swell;
            int dir = this.getSwellDir();
            if (dir > 0 && this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 1.3F);
                this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.PRIME_FUSE);
            }
            this.swell += dir;
            if (this.swell < 0) this.swell = 0;
            if (this.swell >= MAX_SWELL) {
                this.swell = MAX_SWELL;
                this.explode();
            }
        }
        super.tick();
    }

    /** El creeper no hace dano al pegar: solo se acerca. Igual el Jester. */
    @Override
    public boolean doHurtTarget(Entity target) {
        return true;
    }

    private void explode() {
        if (!(this.level() instanceof ServerLevel server)) return;
        this.dead = true;
        // Dano a las entidades, sin romper bloques
        server.explode(this, this.getX(), this.getY(), this.getZ(), EXPLOSION_POWER, Level.ExplosionInteraction.NONE);

        // Lluvia de confeti en el mundo (particulas de colores)
        float[][] colors = {
                {1.0F, 0.2F, 0.2F}, {1.0F, 0.85F, 0.1F}, {0.2F, 0.8F, 0.3F},
                {0.2F, 0.5F, 1.0F}, {0.9F, 0.3F, 0.9F}, {1.0F, 0.55F, 0.1F}};
        for (float[] c : colors) {
            server.sendParticles(new DustParticleOptions(new Vector3f(c[0], c[1], c[2]), 1.5F),
                    this.getX(), this.getY() + 1.2, this.getZ(), 40, 2.0, 1.5, 2.0, 0.0);
        }
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.HOSTILE, 2.0F, 1.0F);

        // Confeti en la pantalla de los jugadores cercanos
        for (ServerPlayer player : server.players()) {
            if (player.distanceToSqr(this) <= CONFETTI_RADIUS * CONFETTI_RADIUS && !player.isSpectator()) {
                ServerPlayNetworking.send(player, new ConfettiPayload(CONFETTI_TICKS));
            }
        }
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Swell", (short) this.swell);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.swell = tag.getShort("Swell");
    }

    // Sonidos (de creeper)
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.CREEPER_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.CREEPER_DEATH; }

    /** Igual que el SwellGoal del creeper, pero para el Jester. */
    static class JesterSwellGoal extends Goal {
        private final JesterEntity jester;
        private LivingEntity target;

        JesterSwellGoal(JesterEntity jester) {
            this.jester = jester;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.jester.getTarget();
            return this.jester.getSwellDir() > 0 || (t != null && this.jester.distanceToSqr(t) < 9.0);
        }

        @Override
        public void start() {
            this.jester.getNavigation().stop();
            this.target = this.jester.getTarget();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target == null || !this.target.isAlive()) {
                this.jester.setSwellDir(-1);
            } else if (this.jester.distanceToSqr(this.target) > 49.0) {
                this.jester.setSwellDir(-1);
            } else if (!this.jester.getSensing().hasLineOfSight(this.target)) {
                this.jester.setSwellDir(-1);
            } else {
                this.jester.setSwellDir(1);
            }
        }
    }
}
