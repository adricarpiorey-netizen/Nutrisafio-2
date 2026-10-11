package com.nutrisafio.mobs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Construye el Bosque Maldito. Cada pedazo del mundo (chunk) solo coloca lo que le toca,
 * pero todo sale de la misma "semilla", asi que encaja perfecto entre chunks.
 */
public class BosqueMalditoPieza extends StructurePiece {

    private static final int R = 56;          // radio maximo de la zona
    private static final int NADA = Integer.MIN_VALUE;
    private static final ResourceKey<LootTable> BOTIN_ALTAR =
            ResourceKey.create(Registries.LOOT_TABLE, NutrisafioMobs.id("chests/bosque_maldito"));
    private static final ResourceKey<LootTable> BOTIN_TUMBA =
            ResourceKey.create(Registries.LOOT_TABLE, NutrisafioMobs.id("chests/tumba_maldita"));

    private final int cx, cy, cz;
    private final long seed;
    private final double fase1, fase2;

    public BosqueMalditoPieza(int cx, int cy, int cz, long seed) {
        super(BosqueMaldito.PIECE, 0, new BoundingBox(cx - R, cy - 40, cz - R, cx + R, cy + 60, cz + R));
        this.cx = cx; this.cy = cy; this.cz = cz; this.seed = seed;
        this.fase1 = (seed & 0xFFFF) / 65535.0 * Math.PI * 2;
        this.fase2 = ((seed >>> 16) & 0xFFFF) / 65535.0 * Math.PI * 2;
    }

    public BosqueMalditoPieza(CompoundTag tag) {
        super(BosqueMaldito.PIECE, tag);
        this.cx = tag.getInt("cx"); this.cy = tag.getInt("cy"); this.cz = tag.getInt("cz");
        this.seed = tag.getLong("semilla");
        this.fase1 = (seed & 0xFFFF) / 65535.0 * Math.PI * 2;
        this.fase2 = ((seed >>> 16) & 0xFFFF) / 65535.0 * Math.PI * 2;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("cx", cx); tag.putInt("cy", cy); tag.putInt("cz", cz);
        tag.putLong("semilla", seed);
    }

    // ------------------------------------------------------------------ utilidades

    /** Borde irregular de la zona (no es un circulo perfecto). */
    private double borde(int x, int z) {
        double ang = Math.atan2(z - cz, x - cx);
        return R - 9 + 5 * Math.sin(3 * ang + fase1) + 3 * Math.sin(5 * ang + fase2);
    }

    private double dist(int x, int z) {
        double dx = x - cx, dz = z - cz;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static long mezclar(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }

    /** Numero "al azar" 0..999 que siempre da lo mismo para la misma columna. */
    private int azar(int x, int z, int sal) {
        long v = mezclar(seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL) ^ (sal * 0x165667B19E3779F9L));
        return (int) Long.remainderUnsigned(v, 1000);
    }

    private static boolean esDecoracion(BlockState s) {
        return s.is(Blocks.BONE_BLOCK) || s.is(Blocks.COBWEB) || s.is(BlockTags.FENCES) || s.is(BlockTags.WALLS)
                || s.is(Blocks.SKELETON_SKULL) || s.is(Blocks.WITHER_SKELETON_SKULL) || s.is(BlockTags.CAMPFIRES)
                || s.is(Blocks.CHAIN) || s.is(Blocks.SOUL_LANTERN) || s.is(Blocks.RED_MUSHROOM) || s.is(Blocks.BROWN_MUSHROOM)
                || s.is(Blocks.SPAWNER);
    }

    /** Altura del suelo real (ignora hojas, troncos, plantas y decoracion). NADA si hay agua. */
    private int suelo(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        int min = Math.max(level.getMinBuildHeight(), top - 60);
        for (int y = top; y >= min; y--) {
            BlockState s = level.getBlockState(p.set(x, y, z));
            if (s.isAir()) continue;
            if (!s.getFluidState().isEmpty() || s.is(BlockTags.ICE)) return NADA;
            if (s.is(BlockTags.LEAVES) || s.is(BlockTags.LOGS) || s.canBeReplaced() || esDecoracion(s)) continue;
            return y;
        }
        return NADA;
    }

    private void poner(WorldGenLevel level, BoundingBox caja, int x, int y, int z, BlockState s) {
        BlockPos p = new BlockPos(x, y, z);
        if (caja.isInside(p)) level.setBlock(p, s, 2);
    }

    private static boolean sueloNatural(BlockState s) {
        return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(Blocks.GRAVEL) || s.is(Blocks.SNOW_BLOCK)
                || s.is(Blocks.CLAY) || s.is(Blocks.MUD);
    }

    // ------------------------------------------------------------------ generacion

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
                            RandomSource random, BoundingBox caja, ChunkPos chunkPos, BlockPos pivot) {
        try {
            terreno(level, caja);
            altar(level, caja, random);
            RandomSource r = RandomSource.create(seed);
            for (int i = 0; i < 26; i++) arbolMuerto(level, caja, r);
            for (int i = 0; i < 12; i++) puaDeHueso(level, caja, r);
            for (int i = 0; i < 10; i++) posteCalavera(level, caja, r);
            for (int i = 0; i < 7; i++) charcoSangre(level, caja, r);
            for (int i = 0; i < 5; i++) fogata(level, caja, r);
            for (int i = 0; i < 5; i++) troncoCaido(level, caja, r);
            for (int i = 0; i < 3; i++) tumba(level, caja, r, random);
        } catch (Exception e) {
            // Nunca tumbar el servidor por un error al generar
            org.slf4j.LoggerFactory.getLogger("nutrisafio_mobs").warn("Error generando Bosque Maldito", e);
        }
    }

    /** Pinta el suelo, mata los arboles normales y pone plantas secas. */
    private void terreno(WorldGenLevel level, BoundingBox caja) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockState tronco = Blocks.DARK_OAK_LOG.defaultBlockState();
        for (int x = caja.minX(); x <= caja.maxX(); x++) {
            for (int z = caja.minZ(); z <= caja.maxZ(); z++) {
                double d = dist(x, z), b = borde(x, z);
                if (d > b) continue;
                // En el borde se mezcla poco a poco con el bioma normal
                if (b - d < 6 && azar(x, z, 1) >= (int) ((b - d) / 6.0 * 1000)) continue;
                int g = suelo(level, x, z);
                if (g == NADA) continue;
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int y = top; y > g; y--) {
                    BlockState s = level.getBlockState(p.set(x, y, z));
                    if (s.is(BlockTags.LEAVES)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    } else if (s.is(BlockTags.LOGS) && !s.is(Blocks.DARK_OAK_LOG)) {
                        BlockState n = tronco;
                        if (s.hasProperty(RotatedPillarBlock.AXIS)) n = n.setValue(RotatedPillarBlock.AXIS, s.getValue(RotatedPillarBlock.AXIS));
                        level.setBlock(p, n, 2);
                    } else if (s.canBeReplaced() && s.getFluidState().isEmpty() && !s.isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                BlockState piso = level.getBlockState(p.set(x, g, z));
                int a = azar(x, z, 2);
                BlockState nuevo = null;
                if (sueloNatural(piso)) {
                    if (a < 330) nuevo = Blocks.COARSE_DIRT.defaultBlockState();
                    else if (a < 560) nuevo = Blocks.PODZOL.defaultBlockState();
                    else if (a < 720) nuevo = Blocks.SOUL_SOIL.defaultBlockState();
                    else if (a < 880) nuevo = Blocks.CRIMSON_NYLIUM.defaultBlockState();
                    else if (a < 950) nuevo = Blocks.ROOTED_DIRT.defaultBlockState();
                    else nuevo = Blocks.NETHER_WART_BLOCK.defaultBlockState();
                    level.setBlock(p, nuevo, 2);
                }
                // Plantas y detalles encima
                BlockPos arriba = p.above();
                if (!level.getBlockState(arriba).isAir()) continue;
                int c = azar(x, z, 3);
                if (nuevo != null && nuevo.is(Blocks.CRIMSON_NYLIUM) && c < 250) level.setBlock(arriba, Blocks.CRIMSON_ROOTS.defaultBlockState(), 2);
                else if (c < 30) level.setBlock(arriba, Blocks.DEAD_BUSH.defaultBlockState(), 2);
                else if (c < 38) level.setBlock(arriba, Blocks.COBWEB.defaultBlockState(), 2);
                else if (c < 44) level.setBlock(arriba, Blocks.RED_MUSHROOM.defaultBlockState(), 2);
                else if (c < 47) level.setBlock(arriba, Blocks.SKELETON_SKULL.defaultBlockState().setValue(SkullBlock.ROTATION, azar(x, z, 4) % 16), 2);
            }
        }
    }

    /** Elige un lugar al azar dentro de la zona (lejos del altar). Siempre usa los mismos numeros. */
    private int[] lugar(RandomSource r, int margen) {
        double ang = r.nextDouble() * Math.PI * 2;
        double rad = 13 + r.nextDouble() * (R - 13);
        int x = cx + (int) Math.round(Math.cos(ang) * rad);
        int z = cz + (int) Math.round(Math.sin(ang) * rad);
        boolean ok = dist(x, z) <= borde(x, z) - margen;
        return new int[]{x, z, ok ? 1 : 0};
    }

    private void arbolMuerto(WorldGenLevel level, BoundingBox caja, RandomSource r) {
        int[] l = lugar(r, 4);
        int h = 6 + r.nextInt(5);
        int ramas = 2 + r.nextInt(3);
        int[] by = new int[ramas], len = new int[ramas], extra = new int[ramas];
        Direction[] dir = new Direction[ramas];
        for (int i = 0; i < ramas; i++) {
            by[i] = h / 2 + r.nextInt(Math.max(1, h / 2));
            dir[i] = Direction.from2DDataValue(r.nextInt(4));
            len[i] = 2 + r.nextInt(2);
            extra[i] = r.nextInt(10);
        }
        if (l[2] == 0) return;
        int x = l[0], z = l[1];
        if (!caja.intersects(x - 5, z - 5, x + 5, z + 5)) return;
        int g = suelo(level, x, z);
        if (g == NADA) return;
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState();
        for (int i = 1; i <= h; i++) poner(level, caja, x, g + i, z, log);
        for (int i = 0; i < ramas; i++) {
            Direction d = dir[i];
            BlockState lado = log.setValue(RotatedPillarBlock.AXIS, d.getAxis());
            int bx = x, bz = z, y = g + by[i];
            for (int k = 0; k < len[i]; k++) {
                bx += d.getStepX(); bz += d.getStepZ();
                poner(level, caja, bx, y, bz, lado);
            }
            poner(level, caja, bx, y + 1, bz, log);
            if (extra[i] < 3) {
                poner(level, caja, bx + d.getStepX(), y, bz + d.getStepZ(), Blocks.COBWEB.defaultBlockState());
            } else if (extra[i] < 5) {
                poner(level, caja, bx, y - 1, bz, Blocks.CHAIN.defaultBlockState());
                poner(level, caja, bx, y - 2, bz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }
    }

    private void puaDeHueso(WorldGenLevel level, BoundingBox caja, RandomSource r) {
        int[] l = lugar(r, 3);
        int h = 3 + r.nextInt(4);
        Direction d = Direction.from2DDataValue(r.nextInt(4));
        boolean doble = r.nextBoolean();
        if (l[2] == 0 || !caja.intersects(l[0] - 2, l[1] - 2, l[0] + 2, l[1] + 2)) return;
        int g = suelo(level, l[0], l[1]);
        if (g == NADA) return;
        BlockState hueso = Blocks.BONE_BLOCK.defaultBlockState();
        for (int i = 1; i <= h; i++) poner(level, caja, l[0], g + i, l[1], hueso);
        if (doble) {
            int x2 = l[0] + d.getStepX(), z2 = l[1] + d.getStepZ();
            int g2 = suelo(level, x2, z2);
            if (g2 != NADA) for (int i = 1; i <= h - 2; i++) poner(level, caja, x2, g2 + i, z2, hueso);
        }
    }

    private void posteCalavera(WorldGenLevel level, BoundingBox caja, RandomSource r) {
        int[] l = lugar(r, 3);
        int rot = r.nextInt(16);
        boolean wither = r.nextInt(100) < 15;
        if (l[2] == 0 || !caja.intersects(l[0], l[1], l[0], l[1])) return;
        int g = suelo(level, l[0], l[1]);
        if (g == NADA) return;
        poner(level, caja, l[0], g + 1, l[1], Blocks.NETHER_BRICK_FENCE.defaultBlockState());
        poner(level, caja, l[0], g + 2, l[1], Blocks.NETHER_BRICK_FENCE.defaultBlockState());
        BlockState calavera = (wither ? Blocks.WITHER_SKELETON_SKULL : Blocks.SKELETON_SKULL).defaultBlockState()
                .setValue(SkullBlock.ROTATION, rot);
        poner(level, caja, l[0], g + 3, l[1], calavera);
    }

    private void charcoSangre(WorldGenLevel level, BoundingBox caja, RandomSource r) {
        int[] l = lugar(r, 5);
        int rad = 2 + r.nextInt(2);
        if (l[2] == 0 || !caja.intersects(l[0] - rad, l[1] - rad, l[0] + rad, l[1] + rad)) return;
        for (int dx = -rad; dx <= rad; dx++) {
            for (int dz = -rad; dz <= rad; dz++) {
                double dd = Math.sqrt(dx * dx + dz * dz);
                if (dd > rad + 0.3) continue;
                int x = l[0] + dx, z = l[1] + dz;
                if (!caja.isInside(new BlockPos(x, caja.minY(), z))) continue;
                int g = suelo(level, x, z);
                if (g == NADA) continue;
                BlockState s = dd <= rad - 1 ? Blocks.REDSTONE_BLOCK.defaultBlockState() : Blocks.NETHER_WART_BLOCK.defaultBlockState();
                poner(level, caja, x, g, z, s);
                BlockState arriba = level.getBlockState(new BlockPos(x, g + 1, z));
                if (arriba.canBeReplaced() || arriba.is(Blocks.SKELETON_SKULL)) poner(level, caja, x, g + 1, z, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private void fogata(WorldGenLevel level, BoundingBox caja, RandomSource r) {
        int[] l = lugar(r, 4);
        if (l[2] == 0 || !caja.intersects(l[0] - 2, l[1] - 2, l[0] + 2, l[1] + 2)) return;
        int g = suelo(level, l[0], l[1]);
        if (g == NADA) return;
        poner(level, caja, l[0], g + 1, l[1], Blocks.SOUL_CAMPFIRE.defaultBlockState());
        BlockState hueso = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        int g1 = suelo(level, l[0] + 2, l[1]);
        if (g1 != NADA) poner(level, caja, l[0] + 2, g1 + 1, l[1], hueso);
        int g2 = suelo(level, l[0] - 2, l[1]);
        if (g2 != NADA) poner(level, caja, l[0] - 2, g2 + 1, l[1], hueso);
    }

    private void troncoCaido(WorldGenLevel level, BoundingBox caja, RandomSource r) {
        int[] l = lugar(r, 7);
        int largo = 4 + r.nextInt(3);
        Direction d = Direction.from2DDataValue(r.nextInt(4));
        if (l[2] == 0 || !caja.intersects(l[0] - 7, l[1] - 7, l[0] + 7, l[1] + 7)) return;
        BlockState log = Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, d.getAxis());
        for (int i = 0; i < largo; i++) {
            int x = l[0] + d.getStepX() * i, z = l[1] + d.getStepZ() * i;
            if (!caja.isInside(new BlockPos(x, caja.minY(), z))) continue;
            int g = suelo(level, x, z);
            if (g != NADA) poner(level, caja, x, g + 1, z, log);
        }
    }

    private void tumba(WorldGenLevel level, BoundingBox caja, RandomSource r, RandomSource botin) {
        int[] l = lugar(r, 6);
        if (l[2] == 0 || !caja.intersects(l[0] - 2, l[1] - 2, l[0] + 2, l[1] + 2)) return;
        int x = l[0], z = l[1];
        int g = suelo(level, x, z);
        if (g == NADA) return;
        poner(level, caja, x, g + 1, z, Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState());
        poner(level, caja, x, g + 2, z, Blocks.SKELETON_SKULL.defaultBlockState());
        for (int i = 1; i <= 2; i++) poner(level, caja, x + i, g, z, Blocks.COARSE_DIRT.defaultBlockState());
        BlockPos cofre = new BlockPos(x + 1, g, z);
        if (caja.isInside(cofre)) {
            level.setBlock(cofre, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST), 2);
            RandomizableContainer.setBlockEntityLootTable(level, botin, cofre, BOTIN_TUMBA);
        }
    }

    /** Altar en ruinas en el centro: tesoro + dos generadores de Beasts. */
    private void altar(WorldGenLevel level, BoundingBox caja, RandomSource random) {
        if (!caja.intersects(cx - 8, cz - 8, cx + 8, cz + 8)) return;
        int g = suelo(level, cx, cz);
        if (g == NADA) g = cy - 1;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double dd = Math.sqrt(dx * dx + dz * dz);
                if (dd > 7.2) continue;
                int x = cx + dx, z = cz + dz;
                BlockState piso;
                if (dd > 6.2) piso = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
                else if (azar(x, z, 9) < 180) piso = Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
                else if (dd < 2.5) piso = Blocks.RED_NETHER_BRICKS.defaultBlockState();
                else piso = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
                poner(level, caja, x, g, z, piso);
                for (int y = g + 1; y <= g + 6; y++) poner(level, caja, x, y, z, Blocks.AIR.defaultBlockState());
                for (int y = g - 1; y >= g - 6; y--) {
                    BlockState s = level.getBlockState(p.set(x, y, z));
                    if (!(s.isAir() || s.canBeReplaced() || !s.getFluidState().isEmpty())) break;
                    poner(level, caja, x, y, z, Blocks.BLACKSTONE.defaultBlockState());
                }
            }
        }
        // Pilares rotos
        int[][] pil = {{4, 4}, {-4, 4}, {4, -4}, {-4, -4}};
        for (int[] q : pil) {
            int h = 2 + azar(cx + q[0], cz + q[1], 10) % 4;
            for (int y = 1; y <= h; y++) poner(level, caja, cx + q[0], g + y, cz + q[1], Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            if (h >= 4) poner(level, caja, cx + q[0], g + h + 1, cz + q[1], Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
            else poner(level, caja, cx + q[0], g + h + 1, cz + q[1], Blocks.SOUL_LANTERN.defaultBlockState());
        }
        // Calaveras alrededor
        int[][] cal = {{0, 3}, {0, -3}, {3, 0}, {-3, 0}};
        for (int[] q : cal) poner(level, caja, cx + q[0], g + 1, cz + q[1], Blocks.WITHER_SKELETON_SKULL.defaultBlockState()
                .setValue(SkullBlock.ROTATION, azar(cx + q[0], cz + q[1], 11) % 16));
        // Tesoro
        BlockPos cofre = new BlockPos(cx, g + 1, cz);
        if (caja.isInside(cofre)) {
            level.setBlock(cofre, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
            RandomizableContainer.setBlockEntityLootTable(level, random, cofre, BOTIN_ALTAR);
        }
        // Generadores de Beasts (solo funcionan en la oscuridad)
        for (int s : new int[]{6, -6}) {
            BlockPos gen = new BlockPos(cx + s, g + 1, cz);
            if (caja.isInside(gen)) {
                level.setBlock(gen, Blocks.SPAWNER.defaultBlockState(), 2);
                if (level.getBlockEntity(gen) instanceof SpawnerBlockEntity sp) sp.setEntityId(NutrisafioMobs.BEAST, random);
            }
        }
    }
}
