package com.nutrisafio.mobs.client;

import com.nutrisafio.mobs.JesterEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Modelo del Jester (convertido desde el modelo CPM "Pierrot FC"). */
public class JesterModel extends HierarchicalModel<JesterEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public JesterModel(ModelPart root) {
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
    public void setupAnim(JesterEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        // La cabeza mira hacia donde mira el mob
        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD;
        // Caminar: piernas y brazos se balancean
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.2F * limbSwingAmount;
        this.rightLeg.xRot += swing;
        this.leftLeg.xRot += -swing;
        this.rightArm.xRot += -swing * 0.8F;
        this.leftArm.xRot += swing * 0.8F;
        // Brazos que se mueven como bailando, de forma juguetona
        float dance = Mth.sin(ageInTicks * 0.15F) * 0.12F;
        this.rightArm.zRot += dance;
        this.leftArm.zRot += dance;
        // Cuando esta por explotar, abre los brazos
        float s = entity.getSwelling(ageInTicks - entity.tickCount);
        if (s > 0) {
            this.rightArm.zRot += s * 1.2F;
            this.leftArm.zRot -= s * 1.2F;
        }
    }
}
