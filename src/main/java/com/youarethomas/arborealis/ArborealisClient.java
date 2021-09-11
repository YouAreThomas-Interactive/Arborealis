package com.youarethomas.arborealis;

import com.youarethomas.arborealis.models.utils.DynamicModelRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ArborealisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DynamicModelRegistry.registerModels();

        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.HOLLOWED_LOG, RenderLayer.getCutout());

        // Register pumpkin texture - super important for mod functionality x
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier("arborealis:block/pumpkin_side_carved"));
        });
    }
}
