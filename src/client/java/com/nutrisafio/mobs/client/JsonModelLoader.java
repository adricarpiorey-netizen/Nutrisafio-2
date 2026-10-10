package com.nutrisafio.mobs.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;

/**
 * Construye un ModelPart a partir del JSON exportado desde Blockbench (assets/nutrisafio_mobs/cpm/*.json).
 * Cada cara trae sus 4 vertices con su UV, asi se respetan las texturas "por cara" del modelo original.
 */
public final class JsonModelLoader {
    private JsonModelLoader() {}

    public static ModelPart load(String name) {
        String path = "/assets/nutrisafio_mobs/cpm/" + name + ".json";
        try (InputStream in = JsonModelLoader.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("No se encontro el modelo " + path);
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, ModelPart> roots = new LinkedHashMap<>();
            for (JsonElement p : json.getAsJsonArray("parts")) {
                JsonObject o = p.getAsJsonObject();
                roots.put(o.get("name").getAsString(), buildPart(o));
            }
            return new ModelPart(List.of(), roots);
        } catch (Exception e) {
            throw new RuntimeException("Error cargando el modelo " + name, e);
        }
    }

    private static ModelPart buildPart(JsonObject o) {
        List<ModelPart.Cube> cubes = new ArrayList<>();
        for (JsonElement c : o.getAsJsonArray("cubes")) {
            cubes.add(buildCube(c.getAsJsonArray()));
        }
        Map<String, ModelPart> children = new HashMap<>();
        for (JsonElement ch : o.getAsJsonArray("children")) {
            JsonObject co = ch.getAsJsonObject();
            children.put(co.get("name").getAsString(), buildPart(co));
        }
        ModelPart part = new ModelPart(cubes, children);
        JsonArray pv = o.getAsJsonArray("pivot");
        JsonArray rt = o.getAsJsonArray("rot");
        PartPose pose = PartPose.offsetAndRotation(
                pv.get(0).getAsFloat(), pv.get(1).getAsFloat(), pv.get(2).getAsFloat(),
                rt.get(0).getAsFloat(), rt.get(1).getAsFloat(), rt.get(2).getAsFloat());
        part.setInitialPose(pose);
        part.loadPose(pose);
        part.visible = o.get("visible").getAsBoolean();
        return part;
    }

    /** Crea un cubo normal de Minecraft y luego reemplaza sus caras por las del modelo. */
    private static ModelPart.Cube buildCube(JsonArray quads) {
        ModelPart.Cube cube = new ModelPart.Cube(0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, false, 1, 1,
                EnumSet.allOf(Direction.class));
        ModelPart.Polygon[] polys = cube.polygons;
        for (int i = 0; i < polys.length; i++) {
            ModelPart.Polygon poly = polys[i];
            if (i < quads.size()) {
                JsonObject q = quads.get(i).getAsJsonObject();
                JsonArray vs = q.getAsJsonArray("v");
                for (int k = 0; k < 4; k++) {
                    JsonArray v = vs.get(k).getAsJsonArray();
                    poly.vertices[k] = new ModelPart.Vertex(v.get(0).getAsFloat(), v.get(1).getAsFloat(),
                            v.get(2).getAsFloat(), v.get(3).getAsFloat(), v.get(4).getAsFloat());
                }
                JsonArray n = q.getAsJsonArray("n");
                poly.normal.set(n.get(0).getAsFloat(), n.get(1).getAsFloat(), n.get(2).getAsFloat());
            } else {
                // cara que no existe en el modelo: se vuelve un punto invisible
                for (int k = 0; k < 4; k++) {
                    poly.vertices[k] = new ModelPart.Vertex(0, 0, 0, 0, 0);
                }
            }
        }
        return cube;
    }
}
