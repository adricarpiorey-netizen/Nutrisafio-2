package com.nutrisafio.mobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Mensaje del servidor al jugador: "muestra confeti en la pantalla durante X ticks". */
public record ConfettiPayload(int ticks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfettiPayload> TYPE =
            new CustomPacketPayload.Type<>(NutrisafioMobs.id("confeti"));

    public static final StreamCodec<ByteBuf, ConfettiPayload> CODEC =
            ByteBufCodecs.VAR_INT.map(ConfettiPayload::new, ConfettiPayload::ticks);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
