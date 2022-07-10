package com.youarethomas.arborealis.models.model_utils;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.data.client.Model;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import java.util.List;

public class DynamicEntityModel {
    public static void renderBox(MatrixStack matrices, VertexConsumer vertices, int x, int y, int z, int xSize, int ySize, int zSize, int r, int b, int g, int a) {

    }

    public static void renderJsonModel(Identifier identifier, MatrixStack matrices, VertexConsumer vertices, int light, float r, float b, float g, int overlay) {
        BakedModel model = BakedModelManagerHelper.getModel(MinecraftClient.getInstance().getBakedModelManager(), identifier);
        MatrixStack.Entry entry = matrices.peek();
        for (Direction dir : Direction.values()) {
            List<BakedQuad> quads = model.getQuads(null, dir, Arborealis.RANDOM);
            for (BakedQuad bakedQuad : quads) {
                vertices.quad(entry, bakedQuad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, r, g, b, new int[]{light, light, light, light}, overlay, true);
            }
        }
    }

    private static void renderBeamFace(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float red, float green, float blue, float alpha, int yOffset, int height, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, height, x1, z1, u2, v1);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, yOffset, x1, z1, u2, v2);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, yOffset, x2, z2, u1, v2);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, height, x2, z2, u1, v1);
    }

    private static void renderBeamVertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v) {
        vertices.vertex(positionMatrix, x, (float)y, z).color(red, green, blue, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
    }
}
