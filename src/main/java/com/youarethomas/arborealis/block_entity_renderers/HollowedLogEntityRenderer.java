package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;


public class HollowedLogEntityRenderer implements BlockEntityRenderer<HollowedLogEntity> {

    public HollowedLogEntityRenderer(BlockEntityRendererFactory.Context ctx) {    }

    @Override
    public void render(HollowedLogEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 12.0;
        matrices.translate(0.5, 0.4 + offset, 0.5);

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((entity.getWorld().getTime() + tickDelta) * 4));

        if (!entity.getStack(0).isOf(Arborealis.TREE_CORE))
            MinecraftClient.getInstance().getItemRenderer().renderItem(entity.getStack(0), ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);

        matrices.pop();
    }
}
