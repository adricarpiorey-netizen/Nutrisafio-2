package com.nutrisafio.mobs;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.Optional;

/**
 * Bosque Maldito: una zona grande (~110 bloques) que aparece sola en partes NUEVAS del mundo.
 * Arboles muertos, tierra roja, huesos, charcos de sangre, tumbas y un altar con tesoro.
 * Buscala con: /locate structure nutrisafio_mobs:bosque_maldito
 */
public class BosqueMaldito extends Structure {

    public static final MapCodec<BosqueMaldito> CODEC = simpleCodec(BosqueMaldito::new);

    public static StructureType<BosqueMaldito> TYPE;
    public static StructurePieceType PIECE;

    /** Se llama desde NutrisafioMobs.onInitialize(). */
    public static void registrar() {
        TYPE = Registry.register(BuiltInRegistries.STRUCTURE_TYPE, NutrisafioMobs.id("bosque_maldito"), () -> CODEC);
        PIECE = Registry.register(BuiltInRegistries.STRUCTURE_PIECE, NutrisafioMobs.id("bosque_maldito_pieza"),
                (StructurePieceType.ContextlessType) BosqueMalditoPieza::new);
    }

    public BosqueMaldito(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        int x = ctx.chunkPos().getMiddleBlockX();
        int z = ctx.chunkPos().getMiddleBlockZ();
        int y = ctx.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                ctx.heightAccessor(), ctx.randomState());
        // No en el mar ni en lugares muy bajos
        if (y <= ctx.chunkGenerator().getSeaLevel()) return Optional.empty();
        long seed = ctx.seed() ^ ctx.chunkPos().toLong() * 0x5DEECE66DL;
        BlockPos centro = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(centro,
                builder -> builder.addPiece(new BosqueMalditoPieza(x, y, z, seed))));
    }

    @Override
    public StructureType<?> type() {
        return TYPE;
    }
}
