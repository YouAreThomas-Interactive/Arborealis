package com.youarethomas.arborealis;

import com.youarethomas.arborealis.block_entity_renderers.HollowedLogEntityRenderer;
import com.youarethomas.arborealis.models.model_utils.DynamicModelRegistry;
import gui.StencilBagScreen;
import gui.StencilBagScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.DyeableItem;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ArborealisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Load models
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/carved_log/carved_log_frame"))));

        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_outside"))));
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_inside"))));
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/tree_core"))));

        DynamicModelRegistry.registerModels();

        // Screen Handlers
        ScreenRegistry.register(Arborealis.STENCIL_BAG_SCREEN_HANDLER, StencilBagScreen::new);

        // Block Entity Renderers
        BlockEntityRendererRegistry.register(Arborealis.HOLLOWED_LOG_ENTITY, HollowedLogEntityRenderer::new);

        // Colour providers
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack), Arborealis.STENCIL_BAG);

        // Render Layers
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.HOLLOWED_LOG, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.WOODEN_BUCKET, RenderLayer.getTranslucent());

        // Register pumpkin texture - super important for mod functionality x
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier("arborealis:block/invisible"));
            registry.register(new Identifier("arborealis:block/pumpkin_side_carved"));
            registry.register(new Identifier("arborealis:block/pumpkin_side_lit"));
            registry.register(new Identifier("arborealis:rune/rune"));
            registry.register(new Identifier("arborealis:item/stencil_carved"));
            registry.register(new Identifier("arborealis:invisible"));
        });
    }
}
