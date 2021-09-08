package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class CarvedWoodEntityRenderer implements BlockEntityRenderer<CarvedWoodEntity> {

    public CarvedWoodEntityRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(CarvedWoodEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        // Get the log texture
        String logID = entity.getLogID();
        String[] idParts = logID.split(":");
        String log = "minecraft:block/oak_log";

        if (logID.contains("minecraft")) {
            System.out.println("ID Found!");
            log = idParts[0] + ":block/" + idParts[1];
        }
        // Apply log texture
        SpriteIdentifier spriteIdentifier = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log));

        //System.out.println(StringUtils.join(ArrayUtils.toObject(entity.faceNorth), ", "));

        // North face
        if (entity.faceNorth.length > 0) {
        int northFaceCount = 48;
        for (int y = 12; y >= 0; y -= 2) {
            for (int x = 12; x >= 0; x -= 2) {
                    int carveState = entity.faceNorth[northFaceCount];

                    // Where a state of 1 means carved - do not render anything
                    if (carveState != 1) {
                        ModelData modelData = new ModelData();
                        ModelPartData modelPartData = modelData.getRoot();
                        int lightNorth = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().north());
                        boolean highlight = false;

                        modelPartData.addChild("north-%s".formatted(northFaceCount), ModelPartBuilder.create().uv(x, y).cuboid(x-15, y-15, 0, 2, 2, 1), ModelTransform.NONE);

                        // 2 means highlighted
                        if (carveState == 2) {
                            highlight = true;
                        }

                        renderPart(matrices, vertexConsumers, modelPartData.createPart(16, 16), spriteIdentifier, lightNorth, overlay, highlight);
                    }
                    northFaceCount--;
                }
            }
        }
    }

    private void renderPart(MatrixStack matrix, VertexConsumerProvider vertexConsumers, ModelPart modelPart, SpriteIdentifier sprite, int light, int overlay, boolean highlight) {
        matrix.push();

        matrix.scale(-1f, -1f, 1f);

        VertexConsumer vertexConsumer = sprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);
        if (highlight) {
            modelPart.render(matrix, vertexConsumer, light, overlay, 255.0f / 255.0f, 200.0f / 255.0f, 145.0f / 255.0f, 0.1f);
        } else {
            modelPart.render(matrix, vertexConsumer, light, overlay);
        }

        matrix.pop();
    }
}
