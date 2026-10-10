package com.nutrisafio.mobs.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Carga los modelos exportados desde Blockbench (assets/nutrisafio_mobs/cpm/*.json).
 *
 * Las piezas (ModelPart) solo guardan la posicion y rotacion, para poder animarlas.
 * Las caras las dibujamos nosotros mismos, una por una, con su textura "por cara".
 * Asi el modelo se ve igual con o sin mods de rendimiento (como Sodium).
 */
public final class JsonModelLoader {
    private JsonModelLoader() {}

    /** Una pieza del modelo: su ModelPart (para animar) y sus caras. */
    static final class Node {
        final ModelPart part;
        final float[][] quads;   // cada cara: 4 x (x,y,z,u,v) + normal (nx,ny,nz) = 23 numeros
        final List<Node> children = new ArrayList<>();

        Node(ModelPart part, float[][] quads) {
            this.part = part;
            this.quads = quads;
        }
    }

    private static final Map<ModelPart, List<Node>> MODELS = new IdentityHashMap<>();

    public static ModelPart load(String name) {
        String path = "/assets/nutrisafio_mobs/cpm/" + name + ".json";
        try (InputStream in = JsonModelLoader.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("No se encontro el modelo " + path);
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, ModelPart> roots = new LinkedHashMap<>();
            List<Node> nodes = new ArrayList<>();
            for (JsonElement p : json.getAsJsonArray("parts")) {
                JsonObject o = p.getAsJsonObject();
                Node node = buildNode(o);
                roots.put(o.get("name").getAsString(), node.part);
                nodes.add(node);
            }
            ModelPart root = new ModelPart(List.of(), roots);
            MODELS.put(root, nodes);
            return root;
        } catch (Exception e) {
            throw new RuntimeException("Error cargando el modelo " + name, e);
        }
    }

    private static Node buildNode(JsonObject o) {
        List<float[]> quads = new ArrayList<>();
        for (JsonElement c : o.getAsJsonArray("cubes")) {
            for (JsonElement qe : c.getAsJsonArray()) {
                JsonObject q = qe.getAsJsonObject();
                float[] data = new float[23];
                JsonArray vs = q.getAsJsonArray("v");
                for (int k = 0; k < 4; k++) {
                    JsonArray v = vs.get(k).getAsJsonArray();
                    for (int j = 0; j < 5; j++) data[k * 5 + j] = v.get(j).getAsFloat();
                }
                JsonArray n = q.getAsJsonArray("n");
                data[20] = n.get(0).getAsFloat();
                data[21] = n.get(1).getAsFloat();
                data[22] = n.get(2).getAsFloat();
                quads.add(data);
            }
        }
        Map<String, ModelPart> childParts = new HashMap<>();
        List<Node> childNodes = new ArrayList<>();
        for (JsonElement ch : o.getAsJsonArray("children")) {
            JsonObject co = ch.getAsJsonObject();
            Node child = buildNode(co);
            childParts.put(co.get("name").getAsString(), child.part);
            childNodes.add(child);
        }
        ModelPart part = new ModelPart(List.of(), childParts);
        JsonArray pv = o.getAsJsonArray("pivot");
        JsonArray rt = o.getAsJsonArray("rot");
        PartPose pose = PartPose.offsetAndRotation(
                pv.get(0).getAsFloat(), pv.get(1).getAsFloat(), pv.get(2).getAsFloat(),
                rt.get(0).getAsFloat(), rt.get(1).getAsFloat(), rt.get(2).getAsFloat());
        part.setInitialPose(pose);
        part.loadPose(pose);
        part.visible = o.get("visible").getAsBoolean();
        Node node = new Node(part, quads.toArray(new float[0][]));
        node.children.addAll(childNodes);
        return node;
    }

    /** Dibuja el modelo completo (llamado desde renderToBuffer de cada modelo). */
    public static void render(ModelPart root, PoseStack poseStack, VertexConsumer buffer,
                              int light, int overlay, int color) {
        List<Node> nodes = MODELS.get(root);
        if (nodes == null || !root.visible) return;
        poseStack.pushPose();
        root.translateAndRotate(poseStack);
        for (Node node : nodes) renderNode(node, poseStack, buffer, light, overlay, color);
        poseStack.popPose();
    }

    private static void renderNode(Node node, PoseStack poseStack, VertexConsumer buffer,
                                   int light, int overlay, int color) {
        if (!node.part.visible) return;
        poseStack.pushPose();
        node.part.translateAndRotate(poseStack);
        if (node.quads.length > 0) {
            Matrix4f pose = poseStack.last().pose();
            Matrix3f normalMat = poseStack.last().normal();
            Vector3f pos = new Vector3f();
            Vector3f nrm = new Vector3f();
            for (float[] q : node.quads) {
                normalMat.transform(nrm.set(q[20], q[21], q[22])).normalize();
                for (int k = 0; k < 4; k++) {
                    int b = k * 5;
                    pose.transformPosition(q[b] / 16.0F, q[b + 1] / 16.0F, q[b + 2] / 16.0F, pos);
                    buffer.addVertex(pos.x(), pos.y(), pos.z(), color, q[b + 3], q[b + 4],
                            overlay, light, nrm.x(), nrm.y(), nrm.z());
                }
            }
        }
        for (Node child : node.children) renderNode(child, poseStack, buffer, light, overlay, color);
        poseStack.popPose();
    }
}
