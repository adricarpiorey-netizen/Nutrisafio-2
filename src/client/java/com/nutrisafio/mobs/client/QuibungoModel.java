package com.nutrisafio.mobs.client;

import com.nutrisafio.mobs.QuibungoEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Modelo del Quibungo (convertido desde el modelo CPM "Quibungo").
 * Todo el cuerpo cuelga de "body"; las partes que se animan se buscan por su ruta.
 */
public class QuibungoModel extends HierarchicalModel<QuibungoEntity> {
    private final ModelPart root;
    private final ModelPart head;      // cabeza con la boca
    private final ModelPart torso;     // torso con brazos y puas
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart[] armsLeft;
    private final ModelPart[] armsRight;

    public QuibungoModel(ModelPart root) {
        this.root = root;
        ModelPart body = root.getChild("body");
        this.head = body.getChild("c0");
        ModelPart lower = body.getChild("c1");
        ModelPart legs = lower.getChild("c0");
        this.legLeft = legs.getChild("c0");
        this.legRight = legs.getChild("c1");
        this.torso = lower.getChild("c1");
        this.armsLeft = new ModelPart[] {torso.getChild("c2"), torso.getChild("c3")};
        this.armsRight = new ModelPart[] {torso.getChild("c4"), torso.getChild("c5")};
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(QuibungoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        // Cabeza
        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD * 0.6F;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
        // Caminar pesado
        float swing = Mth.cos(limbSwing * 0.5F) * 0.9F * limbSwingAmount;
        this.legLeft.xRot += swing;
        this.legRight.xRot += -swing;
        this.torso.zRot += Mth.cos(limbSwing * 0.5F) * 0.06F * limbSwingAmount;
        // Los brazos se balancean
        for (int i = 0; i < 2; i++) {
            this.armsLeft[i].xRot += -swing * 0.6F;
            this.armsRight[i].xRot += swing * 0.6F;
        }
        // Respiracion
        float breathe = Mth.sin(ageInTicks * 0.06F) * 0.03F;
        this.torso.xRot += breathe;

        if (entity.isGrabbing()) {
            // Agarrando: brazos hacia adelante y la cabeza sacude de lado a lado
            for (int i = 0; i < 2; i++) {
                this.armsLeft[i].xRot += -1.1F;
                this.armsRight[i].xRot += -1.1F;
            }
            this.head.yRot += Mth.sin(ageInTicks * 1.3F) * 0.45F;
            this.head.xRot += 0.25F;
        } else if (entity.isAggressive()) {
            // Persiguiendo: se inclina hacia adelante
            this.torso.xRot += 0.12F;
        }
    }
}
