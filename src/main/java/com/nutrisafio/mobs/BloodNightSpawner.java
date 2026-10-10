package com.nutrisafio.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Vector3f;

/**
 * Hace aparecer Beasts durante la Noche de Sangre del datapack "noche".
 * Lee el marcador "#activa noche": si vale 1, la Noche de Sangre esta activa.
 * Los Beasts llevan la etiqueta "sangre", asi el datapack los quita al amanecer.
 */
public final class BloodNightSpawner {
    private static final int EVERY_TICKS = 80;      // cada 4 segundos (igual que las oleadas)
    private static final float CHANCE = 0.08F;      // 8% por jugador: poco comun
    private static final int MAX_NEAR = 2;          // maximo 2 Beasts cerca de cada jugador

    private BloodNightSpawner() {}

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % EVERY_TICKS != 0) return;
        if (!isBloodNight(server)) return;
        ServerLevel level = server.overworld();
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) continue;
            if (level.random.nextFloat() >= CHANCE) continue;
            int near = level.getEntitiesOfClass(BeastEntity.class, player.getBoundingBox().inflate(48)).size();
            if (near >= MAX_NEAR) continue;
            trySpawnNear(level, player);
        }
    }

    private static boolean isBloodNight(MinecraftServer server) {
        Scoreboard board = server.getScoreboard();
        Objective obj = board.getObjective("noche");
        if (obj == null) return false;
        ReadOnlyScoreInfo info = board.getPlayerScoreInfo(ScoreHolder.forNameOnly("#activa"), obj);
        return info != null && info.value() == 1;
    }

    private static void trySpawnNear(ServerLevel level, ServerPlayer player) {
        for (int attempt = 0; attempt < 6; attempt++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double dist = 14 + level.random.nextDouble() * 12;   // entre 14 y 26 bloques
            int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos.below()).isSolid()) continue;
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) continue;

            BeastEntity beast = NutrisafioMobs.BEAST.create(level);
            if (beast == null) return;
            beast.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360F, 0);
            if (!level.noCollision(beast)) continue;
            beast.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            beast.setPersistenceRequired();
            beast.addTag("sangre");
            level.addFreshEntity(beast);
            level.sendParticles(new DustParticleOptions(new Vector3f(0.7F, 0F, 0F), 2.0F),
                    x + 0.5, y + 1, z + 0.5, 40, 0.6, 0.8, 0.6, 0);
            level.playSound(null, x + 0.5, y, z + 0.5, SoundEvents.WOLF_HOWL, SoundSource.HOSTILE, 2.0F, 0.5F);
            return;
        }
    }
}
