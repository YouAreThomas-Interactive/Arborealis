package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;


public class HollowedLogEntityRenderer implements BlockEntityRenderer<HollowedLogEntity> {

    public HollowedLogEntityRenderer(BlockEntityRendererFactory.Context ctx) {    }

    @Override
    public void render(HollowedLogEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 12.0;
        matrices.translate(0.5, 0.35 + offset, 0.5);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.getWorld().getTime() + tickDelta) * 4));

        if (!entity.getStack(0).isOf(Arborealis.LIFE_CORE))
            MinecraftClient.getInstance().getItemRenderer().renderItem(entity.getStack(0), ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, null, 0);

        matrices.pop();
    }
}
