package com.nutrisafio.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nutrisafio.mobs.NutrisafioMobs;
import com.nutrisafio.mobs.QuibungoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class QuibungoRenderer extends MobRenderer<QuibungoEntity, QuibungoModel> {
    private static final ResourceLocation TEXTURE = NutrisafioMobs.id("textures/entity/quibungo.png");
    /** El modelo original mide mas de 5 bloques; lo dejamos en unos 3.5 bloques de alto. */
    public static final float SCALE = 0.65F;

    public QuibungoRenderer(EntityRendererProvider.Context context) {
        super(context, new QuibungoModel(JsonModelLoader.load("quibungo")), 1.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(QuibungoEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(QuibungoEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
