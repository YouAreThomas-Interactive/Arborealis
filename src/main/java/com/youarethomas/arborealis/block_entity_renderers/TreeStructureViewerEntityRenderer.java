package com.youarethomas.arborealis.block_entity_renderers;

import com.youarethomas.arborealis.block_entities.TreeStructureViewerEntity;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class TreeStructureViewerEntityRenderer implements BlockEntityRenderer<TreeStructureViewerEntity> {

    private final static double PADDING = 0.01;

    public TreeStructureViewerEntityRenderer(BlockEntityRendererFactory.Context ctx){}

    @Override
    public void render(TreeStructureViewerEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        System.out.println(entity.manager);
        if(entity.manager != null) {
            TreeStructure structure = entity.manager.getTreeStructure(entity.getPos().down(), entity.getWorld());

            for (BlockPos logPos : structure.logs) {
                drawBorderBlock(entity, matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), logPos);
            }

            for (BlockPos leafPos : structure.leaves) {
                drawBorderBlock(entity, matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), leafPos);
            }
        }
    }

    private void drawBorderBlock(TreeStructureViewerEntity entity, MatrixStack matrices, VertexConsumer vertices, BlockPos pos) {
        Vec3i relativePos = pos.subtract(entity.getPos());
        WorldRenderer.drawBox(
                matrices,
                vertices,
                relativePos.getX() - PADDING,
                relativePos.getY() - PADDING,
                relativePos.getZ() - PADDING,
                relativePos.getX() + 1 + PADDING,
                relativePos.getY() + 1 + PADDING,
                relativePos.getZ() + 1 + PADDING,
                1.0f, 1.0f, 1.0f, 1.0f);
    }
}
