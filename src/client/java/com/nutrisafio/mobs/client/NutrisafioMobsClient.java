package com.nutrisafio.mobs.client;

import com.nutrisafio.mobs.ConfettiPayload;
import com.nutrisafio.mobs.NutrisafioMobs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class NutrisafioMobsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(NutrisafioMobs.JESTER, JesterRenderer::new);
        EntityRendererRegistry.register(NutrisafioMobs.BEAST, BeastRenderer::new);
        EntityRendererRegistry.register(NutrisafioMobs.BROKEN_SOUL, BrokenSoulRenderer::new);
        EntityRendererRegistry.register(NutrisafioMobs.QUIBUNGO, QuibungoRenderer::new);

        // Casco Maldito con cuernos
        ArmorRenderer.register(new CursedHelmetRenderer(), NutrisafioMobs.CURSED_HELMET);

        ClientPlayNetworking.registerGlobalReceiver(ConfettiPayload.TYPE,
                (payload, context) -> context.client().execute(() -> ConfettiOverlay.start(payload.ticks())));
        ClientTickEvents.END_CLIENT_TICK.register(client -> ConfettiOverlay.tick());
        HudRenderCallback.EVENT.register(ConfettiOverlay::render);
    }
}
