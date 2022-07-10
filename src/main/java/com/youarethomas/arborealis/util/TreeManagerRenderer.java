package com.youarethomas.arborealis.util;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.*;

public class TreeManagerRenderer {

    private static Map<RegistryKey<World>, Hashtable<String, TreeStructure>> treeStructureMappings = new HashMap<>();
    private final static double PADDING = 0.001;

    public static void initTreeStructure(RegistryKey<World> worldKey, Hashtable<String, TreeStructure> treeStructureMapping) {
        treeStructureMappings.put(worldKey, treeStructureMapping);
        System.out.println("Initialised trees structures for " + worldKey.toString() + ": " + treeStructureMapping.size());
    }

    public static void updateTreeStructures(RegistryKey<World> worldKey, List<String> removedIDs, Hashtable<String, TreeStructure> newStructures) {
        for(String removedID : removedIDs) {
            treeStructureMappings.get(worldKey).remove(removedID);
        }
        treeStructureMappings.get(worldKey).putAll(newStructures);

        System.out.println("Number of removed structures: " + removedIDs.size());
        System.out.println("Number of added structures: " + newStructures.size());
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera, World world) {
        if (treeStructureMappings.get(world.getRegistryKey()) != null) {
            Random ran = new Random(0);
            for (TreeStructure treeStructure : treeStructureMappings.get(world.getRegistryKey()).values()) {
                float base_r = ran.nextFloat();
                float base_g = ran.nextFloat();
                float base_b = ran.nextFloat();

                for (BlockPos blockToHighlight : treeStructure.leaves) {
                    matrices.push();
                    // Leaf blocks are brighter than the logs.
                    drawBorderBlock(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), blockToHighlight, camera,
                            0.60f * base_r + 0.40f,
                            0.60f * base_g + 0.40f,
                            0.60f * base_b + 0.40f);
                    matrices.pop();
                }

                for (BlockPos blockToHighlight : treeStructure.logs) {
                    matrices.push();
                    drawBorderBlock(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), blockToHighlight, camera,
                            0.40f * base_r + 0.20f,
                            0.40f * base_g + 0.20f,
                            0.40f * base_b + 0.20f);
                    matrices.pop();
                }
            }
        }
    }

    private void drawBorderBlock(MatrixStack matrices, VertexConsumer vertices, BlockPos pos, Camera camera, float r, float g, float b) {
        WorldRenderer.drawBox(
                matrices,
                vertices,
                pos.getX() - camera.getPos().x - PADDING,
                pos.getY() - camera.getPos().y - PADDING,
                pos.getZ() - camera.getPos().z - PADDING,
                pos.getX() - camera.getPos().x + 1 + PADDING,
                pos.getY() - camera.getPos().y + 1 + PADDING,
                pos.getZ() - camera.getPos().z + 1 + PADDING,
                r, g, b, 0.5f);
    }
}
