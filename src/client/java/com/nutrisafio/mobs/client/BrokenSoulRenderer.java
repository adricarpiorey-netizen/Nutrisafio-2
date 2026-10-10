package com.nutrisafio.mobs.client;

import com.nutrisafio.mobs.BrokenSoulEntity;
import com.nutrisafio.mobs.NutrisafioMobs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BrokenSoulRenderer extends MobRenderer<BrokenSoulEntity, BrokenSoulModel> {
    private static final ResourceLocation TEXTURE = NutrisafioMobs.id("textures/entity/broken_soul.png");

    public BrokenSoulRenderer(EntityRendererProvider.Context context) {
        super(context, new BrokenSoulModel(JsonModelLoader.load("broken_soul")), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BrokenSoulEntity entity) {
        return TEXTURE;
    }
}
