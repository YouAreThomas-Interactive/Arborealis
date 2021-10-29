package com.youarethomas.arborealis;

import com.youarethomas.arborealis.block_entity_renderers.HollowedLogEntityRenderer;
import com.youarethomas.arborealis.models.model_utils.DynamicModelRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ArborealisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DynamicModelRegistry.registerModels();

        // Block Entity Renderers
        BlockEntityRendererRegistry.INSTANCE.register(Arborealis.HOLLOWED_LOG_ENTITY, HollowedLogEntityRenderer::new);

        // Render Layers
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.HOLLOWED_LOG, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.WOODEN_BUCKET, RenderLayer.getTranslucent());

        // Register pumpkin texture - super important for mod functionality x
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier("arborealis:block/pumpkin_side_carved"));
            registry.register(new Identifier("arborealis:block/pumpkin_side_lit"));
            registry.register(new Identifier("arborealis:rune/rune"));
        });
    }
}
