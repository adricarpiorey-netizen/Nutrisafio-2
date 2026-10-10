package com.nutrisafio.mobs.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/** Pantalla llena de confeti que cae, durante unos segundos. */
public final class ConfettiOverlay {
    private static final int PIECES = 450;
    private static final int[] COLORS = {
            0xFF3B3B, 0xFFD21A, 0x37CC4F, 0x3388FF, 0xE84CE8, 0xFF8C1A, 0xFFFFFF, 0x2EE6E6};

    private static int remaining = 0;
    private static int total = 1;
    private static long seed = 0;

    private ConfettiOverlay() {}

    public static void start(int ticks) {
        remaining = Math.max(remaining, ticks);
        total = Math.max(1, remaining);
        seed = System.nanoTime();
    }

    public static void tick() {
        if (remaining > 0) remaining--;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        if (remaining <= 0) return;
        int w = g.guiWidth();
        int h = g.guiHeight();
        float time = (total - remaining) + delta.getGameTimeDeltaPartialTick(false);
        // Se desvanece en el ultimo segundo
        float alpha = Mth.clamp(remaining / 20.0F, 0.0F, 1.0F);
        int a = (int) (alpha * 235) << 24;
        for (int i = 0; i < PIECES; i++) {
            long r = mix(seed + i * 0x9E3779B97F4A7C15L);
            float x0 = (r & 0xFFFF) / 65535.0F * w;
            float y0 = ((r >>> 16) & 0xFFFF) / 65535.0F * h;
            float speed = 1.5F + ((r >>> 32) & 0xFF) / 255.0F * 3.0F;
            int size = 2 + (int) ((r >>> 40) & 0x3);
            int color = COLORS[(int) ((r >>> 44) & 0x7)];
            float sway = Mth.sin(time * 0.2F + i) * 6.0F;
            int x = (int) (x0 + sway);
            int y = (int) ((y0 + time * speed) % (h + 10)) - 5;
            // piezas alargadas que "giran"
            boolean tall = ((int) (time / 4 + i) & 1) == 0;
            int pw = tall ? size : size * 2;
            int ph = tall ? size * 2 : size;
            g.fill(x, y, x + pw, y + ph, a | color);
        }
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
