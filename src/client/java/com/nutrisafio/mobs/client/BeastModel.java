package com.nutrisafio.mobs.client;

import com.nutrisafio.mobs.BeastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Modelo del Beast (convertido desde el modelo CPM "beast"). Camina en cuatro patas. */
public class BeastModel extends HierarchicalModel<BeastEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart frontRight;
    private final ModelPart frontLeft;
    private final ModelPart backRight;
    private final ModelPart backLeft;

    public BeastModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.frontRight = root.getChild("right_arm");
        this.frontLeft = root.getChild("left_arm");
        this.backRight = root.getChild("right_leg");
        this.backLeft = root.getChild("left_leg");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    /** Las caras las dibuja JsonModelLoader (funciona tambien con Sodium). */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        JsonModelLoader.render(this.root, poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void setupAnim(BeastEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        // Cabeza
        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD * 0.8F;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.6F;
        // Trote: patas en diagonal se mueven juntas
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.1F * limbSwingAmount;
        this.frontRight.xRot += swing;
        this.backLeft.xRot += swing;
        this.frontLeft.xRot += -swing;
        this.backRight.xRot += -swing;
        // El cuerpo sube y baja al correr
        float bob = Math.abs(Mth.sin(limbSwing * 0.6662F)) * limbSwingAmount * 0.8F;
        this.body.y -= bob;
        this.head.y -= bob;
        // Respiracion / gruñido
        this.head.zRot += Mth.sin(ageInTicks * 0.1F) * 0.03F;
    }
}
