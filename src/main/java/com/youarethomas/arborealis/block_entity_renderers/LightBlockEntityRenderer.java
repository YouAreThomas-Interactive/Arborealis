package com.youarethomas.arborealis.block_entity_renderers;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public abstract class LightBlockEntityRenderer {
    final float PIXEL_SIZE = 0.0625f;

    void renderBeamSegment(MatrixStack matrices, VertexConsumer vertices, float r, float g, float b, float alphaStart, float alphaEnd, float yOffset, float ySize, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float u2, float v1, float v2) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();

        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x1, z1, x2, z2, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x4, z4, x3, z3, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x2, z2, x4, z4, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x3, z3, x1, z1, u1, u2, v1, v2);
    }

    void renderBeamFace(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float r, float g, float b, float alphaStart, float alphaEnd, float yOffset, float ySize, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaStart, yOffset, x1, z1, u2, v2);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaEnd, ySize, x1, z1, u2, v1);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaEnd, ySize, x2, z2, u1, v1);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaStart, yOffset, x2, z2, u1, v2);
    }

    void renderBeamVertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float r, float g, float b, float a, float y, float x, float z, float u, float v) {
        vertices.vertex(positionMatrix, x, y, z).color(r, g, b, a).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normalMatrix, 0.0f, 1.0f, 0.0f).next();
    }
}
