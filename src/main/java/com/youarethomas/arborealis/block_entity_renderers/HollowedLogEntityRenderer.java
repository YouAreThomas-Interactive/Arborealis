package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

public class HollowedLogEntityRenderer implements BlockEntityRenderer<HollowedLogEntity> {

    private static ItemStack stack = new ItemStack(Arborealis.TREE_CORE, 1);

    public HollowedLogEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(HollowedLogEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        // Calculate the current offset in the y value
        double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 12.0;

        // Move the item
        matrices.translate(0.5, 0.4 + offset, 0.5);

        // Rotate the item
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((entity.getWorld().getTime() + tickDelta) * 4));

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);

        // Mandatory call after GL calls
        matrices.pop();
    }
}
