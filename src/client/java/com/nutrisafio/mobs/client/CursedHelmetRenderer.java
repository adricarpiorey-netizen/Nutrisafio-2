package com.nutrisafio.mobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nutrisafio.mobs.NutrisafioMobs;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Dibuja el Casco Maldito puesto: el casco normal + dos cuernos que salen de los costados
 * y se curvan hacia arriba (como en el icono).
 */
public class CursedHelmetRenderer implements ArmorRenderer {
    private static final ResourceLocation ARMOR_TEXTURE =
            NutrisafioMobs.id("textures/models/armor/cursed_layer_1.png");
    private static final ResourceLocation HORN_TEXTURE =
            NutrisafioMobs.id("textures/models/armor/cursed_horns.png");

    private HumanoidModel<LivingEntity> armorModel;
    private ModelPart horns;

    private static ModelPart createHorns() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Cuerno derecho (x negativo): sale del costado, sube y termina en punta
        root.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.5F, -8.5F, -1.0F, 3, 2, 2)
                .texOffs(0, 8).addBox(-8.5F, -11.5F, -1.0F, 2, 3, 2)
                .texOffs(0, 16).addBox(-8.0F, -13.5F, -0.5F, 1, 2, 1), PartPose.ZERO);
        // Cuerno izquierdo (espejo)
        root.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(16, 0).addBox(4.5F, -8.5F, -1.0F, 3, 2, 2)
                .texOffs(16, 8).addBox(6.5F, -11.5F, -1.0F, 2, 3, 2)
                .texOffs(16, 16).addBox(7.0F, -13.5F, -0.5F, 1, 2, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32).bakeRoot();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, ItemStack stack, LivingEntity entity,
                       EquipmentSlot slot, int light, HumanoidModel<LivingEntity> contextModel) {
        if (this.armorModel == null) {
            this.armorModel = new HumanoidModel<>(
                    Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
            this.horns = createHorns();
        }
        contextModel.copyPropertiesTo(this.armorModel);
        this.armorModel.setAllVisible(false);
        this.armorModel.head.visible = true;
        this.armorModel.hat.visible = true;
        ArmorRenderer.renderPart(poseStack, buffers, light, stack, this.armorModel, ARMOR_TEXTURE);

        // Cuernos pegados a la cabeza (se mueven con ella)
        poseStack.pushPose();
        this.armorModel.head.translateAndRotate(poseStack);
        VertexConsumer consumer = buffers.getBuffer(RenderType.armorCutoutNoCull(HORN_TEXTURE));
        this.horns.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
