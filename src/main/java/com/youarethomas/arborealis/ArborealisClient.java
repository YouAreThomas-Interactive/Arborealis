package com.youarethomas.arborealis;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.entity_renderer.CarvedWoodEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class ArborealisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.CARVED_WOOD, RenderLayer.getTranslucent());
        BlockEntityRendererRegistry.INSTANCE.register(Arborealis.CARVED_WOOD_ENTITY, CarvedWoodEntityRenderer::new);
    }
}
