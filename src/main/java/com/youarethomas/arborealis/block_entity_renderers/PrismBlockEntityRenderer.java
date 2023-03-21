package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.block_entities.PrismBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class PrismBlockEntityRenderer extends BeamEmittingBlockEntityRenderer implements BlockEntityRenderer<PrismBlockEntity> {

    public PrismBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {    }

    @Override
    public void render(PrismBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getLightLevel() > 0) {
            for (Direction dir : Direction.values()) {
                if (!entity.getBeamActive(dir) || entity.getThrowDistance(dir) == 0)
                    continue;

                renderFullBeam(entity, matrices, vertexConsumers, dir);
            }
        }
    }
}
