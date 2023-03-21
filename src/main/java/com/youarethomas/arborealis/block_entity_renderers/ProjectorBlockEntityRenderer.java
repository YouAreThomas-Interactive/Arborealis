package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.BeamEmittingBlockEntity;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.items.lenses.LensItem;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class ProjectorBlockEntityRenderer extends BeamEmittingBlockEntityRenderer implements BlockEntityRenderer<ProjectorBlockEntity> {
    public static final Identifier BEAM_TEXTURE = new Identifier(Arborealis.MOD_ID, "textures/block/blank.png");

    public ProjectorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {    }

    @Override
    public void render(ProjectorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getLightLevel() > 0) {
            matrices.push();
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

            float alphaStart = ((0.2f / 15f) * entity.getLightLevel()); // Create light level based on light level

            // Render the initial beam inside the projector block
            matrices.translate(6 * PIXEL_SIZE, 0.0f, 6 * PIXEL_SIZE);
            renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), 1, 0.905f, 0.619f, alphaStart, alphaStart, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 2), -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, PIXEL_SIZE * 7, PIXEL_SIZE * 7, 0, 1, 0, 1);

            matrices.pop();

            // Render stencil beams
            renderFullBeam(entity, matrices, vertexConsumers, facing);
        }
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
