package com.youarethomas.arborealis.entity_renderer;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;

@Environment(EnvType.CLIENT)
public class CarvedWoodEntityRenderer implements BlockEntityRenderer<CarvedWoodEntity> {

    private static final SpriteIdentifier[] SPRITE_IDS = new SpriteIdentifier[]{
            new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:block/test_block"))
    };

    public CarvedWoodEntityRenderer(Context ctx) {
    }

    @Override
    public void render(CarvedWoodEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        ModelPart part = getTexturedModelData().createModel();
        CarvedWoodModel carvedWoodModel = new CarvedWoodModel(part);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(carvedWoodModel.getLayer(new Identifier("arborealis", "textures/block/carved_wood")));
        carvedWoodModel.root.render(matrices, vertexConsumer, light, overlay);

        matrices.pop();
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("core", ModelPartBuilder.create().uv(0, 0).cuboid(0F, 0F, 0F, 16F, 16F, 16F), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 16, 16);
    }

    @Environment(EnvType.CLIENT)
    public static final class CarvedWoodModel extends Model {
        public final ModelPart root;

        public CarvedWoodModel(ModelPart root) {
            super(RenderLayer::getEntitySolid);
            this.root = root;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
            this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }


    }
}
