package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class HollowedLogEntityRenderer implements BlockEntityRenderer<HollowedLogEntity> {

    private final ModelPart coreModel;
    private final SpriteIdentifier coreTexture = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "tree_core_block"));

    private static ItemStack stack = new ItemStack(Arborealis.TREE_CORE, 1);

    public HollowedLogEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        coreModel = ctx.getLayerModelPart(EntityModelLayers.BELL);
    }

    @Override
    public void render(HollowedLogEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        if (stack.isOf(Arborealis.TREE_CORE)) {
            coreModel.render(matrices, vertexConsumers.getBuffer(RenderLayer.getSolid()), light, overlay);
        } else {
            double offset = Math.sin((entity.getWorld().getTime() + tickDelta) / 8.0) / 12.0;
            matrices.translate(0.5, 0.4 + offset, 0.5);

            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((entity.getWorld().getTime() + tickDelta) * 4));

            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers, 0);
        }

        matrices.pop();
    }
}
