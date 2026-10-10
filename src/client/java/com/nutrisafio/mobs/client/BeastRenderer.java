package com.nutrisafio.mobs.client;

import com.nutrisafio.mobs.BeastEntity;
import com.nutrisafio.mobs.NutrisafioMobs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BeastRenderer extends MobRenderer<BeastEntity, BeastModel> {
    private static final ResourceLocation TEXTURE = NutrisafioMobs.id("textures/entity/beast.png");

    public BeastRenderer(EntityRendererProvider.Context context) {
        super(context, new BeastModel(JsonModelLoader.load("beast")), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(BeastEntity entity) {
        return TEXTURE;
    }
}
