package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.BeamEmittingBlockEntity;
import com.youarethomas.arborealis.block_entities.PrismBlockEntity;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class PrismBlockEntityRenderer extends BeamEmittingBlockEntityRenderer implements BlockEntityRenderer<PrismBlockEntity> {

    BlockModelRenderer modelRenderer;
    BakedModel coreModel = null;

    public PrismBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        modelRenderer = ctx.getRenderManager().getModelRenderer();
    }

    @Override
    public void render(PrismBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (coreModel == null)
            coreModel = BakedModelManagerHelper.getModel(MinecraftClient.getInstance().getBakedModelManager(), new Identifier(Arborealis.MOD_ID, "block/prism/prism_core"));

        if (entity.getLightLevel() > 0) {
            for (Direction dir : Direction.values()) {
                BeamEmittingBlockEntity.ProjectionBeam beam = entity.getBeam(dir);

                if (!beam.getShowBeam() || entity.getSideInput(dir) || beam.getThrowDistance() == 0)
                    continue;

                renderFullBeam(entity, matrices, vertexConsumers, dir);
            }
        }

        matrices.push();

        double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 12.0;
        matrices.translate(0.5d, offset, 0.5d);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.getWorld().getTime() + tickDelta) * 4));
        matrices.translate(-0.5d, 0, -0.5d);

        modelRenderer.render(entity.getWorld(), coreModel, entity.getCachedState(), entity.getPos(), matrices, vertexConsumers.getBuffer(RenderLayer.getCutout()), true, Arborealis.RANDOM, 0, 0);

        matrices.pop();
    }
}
