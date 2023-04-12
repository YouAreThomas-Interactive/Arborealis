package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.block_entities.BeamEmittingBlockEntity;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public abstract class BeamEmittingBlockEntityRenderer {
    final float PIXEL_SIZE = 0.0625f;

    protected void renderFullBeam(BeamEmittingBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, Direction direction) {
        matrices.push();

        // Default is SOUTH.
        float zAngle = 0.0f;
        float xAngle = (float)(Math.PI / 2.0);

        switch (direction) {
            case NORTH -> zAngle = (float) Math.PI;
            case SOUTH -> zAngle = 0.0f;
            case WEST -> zAngle = (float) (Math.PI / 2.0f);
            case EAST -> zAngle = -(float) (Math.PI / 2.0f);
            case UP -> xAngle = 0.0f;
            case DOWN -> xAngle = (float)Math.PI;
        }

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(new Quaternionf().rotationXYZ(xAngle, 0.0f, zAngle));
        matrices.multiply(new Quaternionf().rotationXYZ(0.0f, -(float)(Math.PI / 2.0), 0.0f));
        matrices.translate(-0.5 + PIXEL_SIZE * 2, 0.5, -0.5 + PIXEL_SIZE * 2);

        float alphaStart = ((0.2f / 15f) * entity.getLightLevel()); // Create light level based on light level
        float alphaEnd = alphaStart - ((0.2f / 15f) * entity.getThrowDistance(direction));

        // Render stencil beams
        if (entity.getBeamModifier() == BeamEmittingBlockEntity.BeamModifier.STENCIL) {
            for(int ii = 0; ii < 25; ii++) {
                matrices.push();
                matrices.translate((float)(ii / 5) * PIXEL_SIZE * 2 + (PIXEL_SIZE * 2), 0.0f, (ii % 5) * PIXEL_SIZE * 2 + (PIXEL_SIZE * 2));
                if(entity.getStencilPattern() != null && entity.getStencilPattern().length == 25 && entity.getStencilPattern()[ii] == 2)
                    renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), 1, 0.905f, 0.619f, alphaStart, alphaEnd, -PIXEL_SIZE, entity.getThrowDistance(direction), -PIXEL_SIZE, -PIXEL_SIZE, PIXEL_SIZE, -PIXEL_SIZE, -PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE, 0, 1, 0, 1);
                matrices.pop();
            }
        } else if (entity.getBeamModifier().ordinal() > BeamEmittingBlockEntity.BeamModifier.STENCIL.ordinal()) {
            // Render the slightly smaller beam of that lens' colour
            matrices.push();
            matrices.translate(6 * PIXEL_SIZE, 0.0f, 6 * PIXEL_SIZE);
            ArborealisUtil.Colour lensColour = entity.getBeamColour();
            if (lensColour != null) renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), lensColour.red / 255f, lensColour.green / 255f, lensColour.blue / 255f, alphaStart, alphaEnd, -PIXEL_SIZE, entity.getThrowDistance(direction), -(PIXEL_SIZE * 6), -(PIXEL_SIZE * 6), PIXEL_SIZE * 6, -(PIXEL_SIZE * 6), -(PIXEL_SIZE * 6), PIXEL_SIZE * 6, PIXEL_SIZE * 6, PIXEL_SIZE * 6, 0, 1, 0, 1);
            matrices.pop();
        } else if (entity.getBeamModifier() == BeamEmittingBlockEntity.BeamModifier.NONE) {
            // Render the default full
            matrices.push();
            matrices.translate(6 * PIXEL_SIZE, 0.0f, 6 * PIXEL_SIZE);
            renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), 1, 0.905f, 0.619f, alphaStart, alphaEnd, -(PIXEL_SIZE * 2), entity.getThrowDistance(direction), -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, PIXEL_SIZE * 7, PIXEL_SIZE * 7, 0, 1, 0, 1);
            matrices.pop();
        }

        matrices.pop();
    }

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
