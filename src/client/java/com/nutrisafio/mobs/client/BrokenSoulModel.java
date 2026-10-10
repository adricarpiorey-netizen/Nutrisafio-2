package com.nutrisafio.mobs.client;

import com.nutrisafio.mobs.BrokenSoulEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Modelo del Broken Soul (convertido desde el modelo CPM "broken soul v1"). */
public class BrokenSoulModel extends HierarchicalModel<BrokenSoulEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public BrokenSoulModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(BrokenSoulEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD;
        // Camina lento y rigido, como un Enderman
        float swing = Mth.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.rightLeg.xRot += swing;
        this.leftLeg.xRot += -swing;
        this.rightArm.xRot += -swing * 0.5F;
        this.leftArm.xRot += swing * 0.5F;
        // Brazos que cuelgan y se mecen un poco
        float sway = Mth.sin(ageInTicks * 0.05F) * 0.04F;
        this.rightArm.zRot += sway;
        this.leftArm.zRot -= sway;
        // Enojado: estira los brazos hacia adelante y la cabeza tiembla
        if (entity.isAggressive()) {
            this.rightArm.xRot += -1.3F;
            this.leftArm.xRot += -1.3F;
            this.head.zRot += Mth.sin(ageInTicks * 1.5F) * 0.05F;
        }
    }
}
