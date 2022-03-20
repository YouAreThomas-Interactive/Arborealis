package com.youarethomas.arborealis.util;

import com.google.common.collect.HashBasedTable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Hashtable;

@Environment(EnvType.CLIENT)
public class TreeManagerRenderer {

    private final static double PADDING = 0.01;

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera) {
        Hashtable<BlockPos, TreeStructure> testTree = new Hashtable<>();
        TreeStructure struct = new TreeStructure();
        struct.logs.add(new BlockPos(162, 112, -170));
        testTree.put(new BlockPos(162, 112, -170), struct);

        TreeManager manager = new TreeManager(testTree); // Need to get this somehow

        for (TreeStructure tree: manager.getTreeStructures()) {
            for (BlockPos logPos : tree.logs) {
                matrices.push();
                drawBorderBlock(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), logPos, camera);
                matrices.pop();
            }

            for (BlockPos leafPos : tree.leaves) {
                matrices.push();
                drawBorderBlock(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), leafPos, camera);
                matrices.pop();
            }
        }

    }

    private void drawBorderBlock(MatrixStack matrices, VertexConsumer vertices, BlockPos pos, Camera camera) {
        System.out.println(camera.getPos());

        WorldRenderer.drawBox(
                matrices,
                vertices,
                pos.getX() - camera.getPos().x - PADDING,
                pos.getY() - camera.getPos().y - PADDING,
                pos.getZ() - camera.getPos().z - PADDING,
                pos.getX() - camera.getPos().x + 1 + PADDING,
                pos.getY() - camera.getPos().y + 1 + PADDING,
                pos.getZ() - camera.getPos().z + 1 + PADDING,
                1.0f, 1.0f, 1.0f, 1.0f);
    }
}
