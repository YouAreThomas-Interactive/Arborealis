package com.youarethomas.arborealis.block_entity_renderers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.items.InfusionLensItem;
import com.youarethomas.arborealis.items.StencilCarved;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class ProjectorBlockEntityRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {
    public static final Identifier BEAM_TEXTURE = new Identifier(Arborealis.MOD_ID, "textures/block/blank.png");
    private static final float PIXEL_SIZE = 0.0625f;

    public ProjectorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {    }

    @Override
    public void render(ProjectorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        if (entity.getLightLevel() > 0) {
            Direction facing = entity.getCachedState().get(HorizontalFacingBlock.FACING);
            // Default is SOUTH.
            float angle = 0.0f;

            switch (facing) {
                case NORTH -> angle = (float)Math.PI;
                case WEST -> angle = (float)(Math.PI/2.0f);
                case EAST -> angle = -(float)(Math.PI/2.0f);
            }

            matrices.translate(0.5, 0.5, 0.5);
            matrices.multiply(Quaternion.fromEulerXyz((float)(Math.PI / 2.0), 0.0f, angle));
            matrices.multiply(Quaternion.fromEulerXyz(0.0f, -(float)(Math.PI / 2.0), 0.0f));
            matrices.translate(-0.5 + PIXEL_SIZE * 2, 0.5, -0.5 + PIXEL_SIZE * 2);

            ItemStack itemStack = entity.getStack(0);

            float alphaStart = ((0.2f / 15f) * entity.getLightLevel()); // Create light level based on light level
            float alphaEnd = alphaStart - ((0.2f / 15f) * entity.getThrowDistance());

            // Render the initial beam inside the projector block
            matrices.push();
            matrices.translate(6 * PIXEL_SIZE, 0.0f, 6 * PIXEL_SIZE);
            renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), 1, 0.905f, 0.619f, alphaStart, alphaStart, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 2), -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, PIXEL_SIZE * 7, PIXEL_SIZE * 7, 0, 1, 0, 1);
            matrices.pop();

            // Render stencil beams
            if (itemStack.isOf(Arborealis.CARVED_STENCIL)) {
                NbtCompound nbt = itemStack.getNbt();
                if (nbt != null && nbt.contains("pattern")) {
                    int[] pattern = nbt.getIntArray("pattern");

                    for(int ii = 0; ii < 49; ii++) {
                        matrices.push();
                        matrices.translate((float)(ii / 7) * PIXEL_SIZE * 2, 0.0f, (ii % 7) * PIXEL_SIZE * 2);
                        if(pattern[ii] == 2) renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), 1, 0.905f, 0.619f, alphaStart, alphaEnd, -PIXEL_SIZE, entity.getThrowDistance(), -PIXEL_SIZE, -PIXEL_SIZE, PIXEL_SIZE, -PIXEL_SIZE, -PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE, 0, 1, 0, 1);
                        matrices.pop();
                    }
                }
            } else if (itemStack.isOf(Arborealis.INFUSION_LENS)) {
                // Render the slightly smaller beam of that lens' colour
                matrices.push();
                matrices.translate(6 * PIXEL_SIZE, 0.0f, 6 * PIXEL_SIZE);
                ArborealisUtil.Colour lensColour = ((InfusionLensItem) itemStack.getItem()).getLensColor();
                renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), lensColour.red / 255f, lensColour.green / 255f, lensColour.blue / 255f, alphaStart, alphaEnd, -PIXEL_SIZE, entity.getThrowDistance(), -(PIXEL_SIZE * 6), -(PIXEL_SIZE * 6), PIXEL_SIZE * 6, -(PIXEL_SIZE * 6), -(PIXEL_SIZE * 6), PIXEL_SIZE * 6, PIXEL_SIZE * 6, PIXEL_SIZE * 6, 0, 1, 0, 1);
                matrices.pop();
            } else if (itemStack.isEmpty()) {
                // Render the default full
                matrices.push();
                matrices.translate(6 * PIXEL_SIZE, 0.0f, 6 * PIXEL_SIZE);
                renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), 1, 0.905f, 0.619f, alphaStart, alphaEnd, -PIXEL_SIZE, entity.getThrowDistance(), -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, PIXEL_SIZE * 7, PIXEL_SIZE * 7, 0, 1, 0, 1);
                matrices.pop();
            }
        }

        matrices.pop();
    }

    private static void renderBeamSegment(MatrixStack matrices, VertexConsumer vertices, float r, float g, float b, float alphaStart, float alphaEnd, float yOffset, float ySize, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float u2, float v1, float v2) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();

        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x1, z1, x2, z2, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x4, z4, x3, z3, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x2, z2, x4, z4, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, r, g, b, alphaStart, alphaEnd, yOffset, ySize, x3, z3, x1, z1, u1, u2, v1, v2);
    }

    private static void renderBeamFace(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float r, float g, float b, float alphaStart, float alphaEnd, float yOffset, float ySize, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaStart, yOffset, x1, z1, u2, v2);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaEnd, ySize, x1, z1, u2, v1);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaEnd, ySize, x2, z2, u1, v1);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, r, g, b, alphaStart, yOffset, x2, z2, u1, v2);
    }

    private static void renderBeamVertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float r, float g, float b, float a, float y, float x, float z, float u, float v) {
        vertices.vertex(positionMatrix, x, y, z).color(r, g, b, a).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(normalMatrix, 0.0f, 1.0f, 0.0f).next();
    }

    @Override
    public boolean rendersOutsideBoundingBox(ProjectorBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getRenderDistance() {
        return 256;
    }
}
