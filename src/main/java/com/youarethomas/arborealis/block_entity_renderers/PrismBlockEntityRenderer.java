package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.block_entities.PrismBlockEntity;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

public class PrismBlockEntityRenderer extends LightBlockEntityRenderer implements BlockEntityRenderer<PrismBlockEntity> {

    public PrismBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {    }

    @Override
    public void render(PrismBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        for (Direction dir : Direction.values()) {
            matrices.push();
            // Default is SOUTH.
            float zAngle = 0.0f;
            float xAngle = (float)(Math.PI / 2.0);

            switch (dir) {
                case NORTH -> zAngle = (float) Math.PI;
                case SOUTH -> zAngle = 0.0f;
                case WEST -> zAngle = (float) (Math.PI / 2.0f);
                case EAST -> zAngle = -(float) (Math.PI / 2.0f);
                case UP -> xAngle = 0.0f;
                case DOWN -> xAngle = (float)Math.PI;
            }

            matrices.translate(0.5, 0.5, 0.5);
            matrices.multiply(Quaternion.fromEulerXyz(xAngle, 0.0f, zAngle));
            matrices.multiply(Quaternion.fromEulerXyz(0.0f, -(float)(Math.PI / 2.0), 0.0f));
            matrices.translate(-0.5 + PIXEL_SIZE * 2, 0.5, -0.5 + PIXEL_SIZE * 2);

            matrices.push();
            matrices.translate(6 * PIXEL_SIZE, 0.0f, 6 * PIXEL_SIZE);
            renderBeamSegment(matrices, vertexConsumers.getBuffer(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED), 1, 0.905f, 0.619f, 1f, 0.0f, -(PIXEL_SIZE * 2), 4, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, -(PIXEL_SIZE * 7), -(PIXEL_SIZE * 7), PIXEL_SIZE * 7, PIXEL_SIZE * 7, PIXEL_SIZE * 7, 0, 1, 0, 1);
            matrices.pop();

            matrices.pop();
        }
    }
}
