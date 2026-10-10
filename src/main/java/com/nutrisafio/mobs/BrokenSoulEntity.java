package com.nutrisafio.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Broken Soul: un alma rota del Nether.
 * - Es neutral como el Enderman: si no lo atacas, no te hace nada.
 * - Si lo atacas, te persigue para matarte.
 * - 30 de vida (15 corazones; el Enderman tiene 20)
 * - 8 de dano por golpe (el Enderman hace 7)
 * - Misma velocidad que el Enderman, no le afecta el fuego ni la lava.
 * - Suelta Huesos de Wither.
 */
public class BrokenSoulEntity extends Monster {

    public BrokenSoulEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // Solo ataca a quien le pego (no busca jugadores por su cuenta)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Si su objetivo murio, se fue lejos o cambio a creativo, se calma
        LivingEntity target = this.getTarget();
        if (!this.level().isClientSide && target != null) {
            boolean gone = !target.isAlive() || this.distanceToSqr(target) > 64 * 64
                    || (target instanceof Player p && (p.isCreative() || p.isSpectator()));
            if (gone) this.setTarget(null);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) this.playSound(SoundEvents.WITHER_SKELETON_HURT, 0.8F, 0.6F);
        return hit;
    }

    // Sonidos: esqueleto wither tranquilo, grito de enderman cuando esta enojado
    @Override
    protected SoundEvent getAmbientSound() {
        return this.getTarget() != null ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.WITHER_SKELETON_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.WITHER_SKELETON_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.WITHER_SKELETON_STEP, 0.15F, 0.8F);
    }
}
