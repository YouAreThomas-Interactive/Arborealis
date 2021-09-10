package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class CarvedWoodEntityRenderer implements BlockEntityRenderer<CarvedWoodEntity> {

    public CarvedWoodEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(CarvedWoodEntity carvedWoodEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        // Get the log texture
        String logID = carvedWoodEntity.getLogID();
        String[] idParts = logID.split(":");
        String log = "minecraft:block/oak_log";

        if (logID.contains("minecraft")) {
            log = idParts[0] + ":block/" + idParts[1];
        }
        // Apply log texture
        SpriteIdentifier spriteIdentifier = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log));

        //System.out.println(StringUtils.join(ArrayUtils.toObject(newEntity.getFaceArray(Direction.NORTH)), ", "));

        // North face
        if (carvedWoodEntity.getFaceArray(Direction.NORTH).length > 0) {
        int northFaceCount = 48;
        for (int y = 12; y >= 0; y -= 2) {
            for (int x = 12; x >= 0; x -= 2) {
                    int carveState = carvedWoodEntity.getFaceArray(Direction.NORTH)[northFaceCount];

                    // Where a state of 1 means carved - do not render anything
                    if (carveState != 1) {
                        ModelData modelData = new ModelData();
                        ModelPartData modelPartData = modelData.getRoot();
                        int lightNorth = WorldRenderer.getLightmapCoordinates(carvedWoodEntity.getWorld(), carvedWoodEntity.getPos().north());
                        System.out.println(lightNorth);
                        boolean highlight = false;

                        modelPartData.addChild("north-%s".formatted(northFaceCount), ModelPartBuilder.create().uv(x, y).cuboid(x-15, y-15, 0, 2, 2, 1), ModelTransform.NONE);

                        // 2 means highlighted
                        if (carveState == 2) {
                            highlight = true;
                        }

                        renderPart(matrices, vertexConsumers, modelPartData.createPart(16, 16), spriteIdentifier, lightNorth, overlay, highlight, Direction.NORTH);
                    }
                    northFaceCount--;
                }
            }
        }

        // South face
        if (carvedWoodEntity.getFaceArray(Direction.SOUTH).length > 0) {
            int southFaceCount = 48;
            for (int y = 12; y >= 0; y -= 2) {
                for (int x = 12; x >= 0; x -= 2) {
                    int carveState = carvedWoodEntity.getFaceArray(Direction.SOUTH)[southFaceCount];

                    // Where a state of 1 means carved - do not render anything
                    if (carveState != 1) {
                        ModelData modelData = new ModelData();
                        ModelPartData modelPartData = modelData.getRoot();
                        int lightNorth = WorldRenderer.getLightmapCoordinates(carvedWoodEntity.getWorld(), carvedWoodEntity.getPos().south());
                        boolean highlight = false;

                        modelPartData.addChild("south-%s".formatted(southFaceCount), ModelPartBuilder.create().uv(x, y).cuboid(x-15, y-15, 0, 2, 2, 1), ModelTransform.NONE);

                        // 2 means highlighted
                        if (carveState == 2) {
                            highlight = true;
                        }

                        renderPart(matrices, vertexConsumers, modelPartData.createPart(16, 16), spriteIdentifier, lightNorth, overlay, highlight, Direction.SOUTH);
                    }
                    southFaceCount--;
                }
            }
        }
    }

    private void renderPart(MatrixStack matrix, VertexConsumerProvider vertexConsumers, ModelPart modelPart, SpriteIdentifier sprite, int light, int overlay, boolean highlight, Direction direction) {
        matrix.push();

        matrix.scale(-1f, -1f, 1f);
        if (direction == Direction.SOUTH) {
            matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180F));
            matrix.translate(1, 0, -1);
        }

        VertexConsumer vertexConsumer = sprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);
        if (highlight) {
            modelPart.render(matrix, vertexConsumer, light, overlay, 255.0f / 255.0f,  170.0F / 255.0f, 0.0f / 255.0f, 1f);
        } else {
            modelPart.render(matrix, vertexConsumer, light, overlay);
        }

        matrix.pop();
    }
}
