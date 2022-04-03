package com.youarethomas.arborealis.util;

import com.google.common.collect.HashBasedTable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class TreeManagerRenderer {

    private static Map<RegistryKey<World>, Collection<BlockPos>> treeBlockPositions = new HashMap<>();
    private final static double PADDING = 0.01;

    public static void setBlockPositions(RegistryKey<World> worldKey, Collection<BlockPos> positions) {
        treeBlockPositions.put(worldKey, positions);
        System.out.println("Set positions to be: " + positions.size());
    }

    public static Collection<BlockPos> getBlockPositions(RegistryKey<World> worldKey) {
        return treeBlockPositions.get(worldKey);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera, World world) {
        for (BlockPos blockToHighlight: treeBlockPositions.get(world.getRegistryKey())) {
            matrices.push();
            drawBorderBlock(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), blockToHighlight, camera);
            matrices.pop();
        }
    }

    private void drawBorderBlock(MatrixStack matrices, VertexConsumer vertices, BlockPos pos, Camera camera) {
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
