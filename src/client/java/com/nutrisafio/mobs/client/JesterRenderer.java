package com.nutrisafio.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nutrisafio.mobs.JesterEntity;
import com.nutrisafio.mobs.NutrisafioMobs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class JesterRenderer extends MobRenderer<JesterEntity, JesterModel> {
    private static final ResourceLocation TEXTURE = NutrisafioMobs.id("textures/entity/jester.png");

    public JesterRenderer(EntityRendererProvider.Context context) {
        super(context, new JesterModel(JsonModelLoader.load("jester")), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(JesterEntity entity) {
        return TEXTURE;
    }

    /** Se infla antes de explotar, igual que el creeper. */
    @Override
    protected void scale(JesterEntity entity, PoseStack poseStack, float partialTick) {
        float f = entity.getSwelling(partialTick);
        float wobble = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f *= f;
        f *= f;
        float xz = (1.0F + f * 0.4F) * wobble;
        float y = (1.0F + f * 0.1F) / wobble;
        poseStack.scale(xz, y, xz);
    }

    /** Parpadea en blanco antes de explotar. */
    @Override
    protected float getWhiteOverlayProgress(JesterEntity entity, float partialTick) {
        float f = entity.getSwelling(partialTick);
        return (int) (f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
    }
}
