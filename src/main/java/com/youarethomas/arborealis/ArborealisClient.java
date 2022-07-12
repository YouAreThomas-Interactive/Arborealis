package com.youarethomas.arborealis;

import com.google.common.collect.Lists;
import com.youarethomas.arborealis.block_entity_renderers.HollowedLogEntityRenderer;
import com.youarethomas.arborealis.block_entity_renderers.ProjectorBlockEntityRenderer;
import com.youarethomas.arborealis.block_entity_renderers.WarpCoreEntityRenderer;
import com.youarethomas.arborealis.models.CarvedLogDModel;
import com.youarethomas.arborealis.models.CarvedStencilDModel;
import com.youarethomas.arborealis.models.HollowedLogDModel;
import com.youarethomas.arborealis.models.WarpCoreDModel;
import com.youarethomas.arborealis.models.model_utils.DynamicModelRegistry;
import com.youarethomas.arborealis.particles.WarpTreeParticle;
import com.youarethomas.arborealis.gui.StencilBagScreen;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.DyeableItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class ArborealisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RuneManager.registerRunes();

        // Register pumpkin texture - super important for mod functionality x
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
            registry.register(new Identifier(Arborealis.MOD_ID, "block/invisible"));
            registry.register(new Identifier(Arborealis.MOD_ID, "block/pumpkin_side_carved"));
            registry.register(new Identifier(Arborealis.MOD_ID, "block/pumpkin_side_lit"));
            registry.register(new Identifier(Arborealis.MOD_ID, "rune/rune"));
            registry.register(new Identifier(Arborealis.MOD_ID, "item/stencil_carved"));
            registry.register(new Identifier(Arborealis.MOD_ID, "invisible"));
            registry.register(new Identifier(Arborealis.MOD_ID, "particle/warp_tree_particle"));
        });

        // Load models
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/carved_log/carved_log_frame"))));
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/warp_core_frame"))));

        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_outside"))));
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_inside"))));
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/tree_core"))));
        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/core_tether"))));

        ModelLoadingRegistry.INSTANCE.registerModelProvider(((manager, out) -> out.accept(new Identifier(Arborealis.MOD_ID, "block/projector/beam_full"))));

        // Model registration
        DynamicModelRegistry.register(new CarvedLogDModel(), new Identifier(Arborealis.MOD_ID, "block/carved_log"));
        DynamicModelRegistry.register(new HollowedLogDModel(), new Identifier(Arborealis.MOD_ID, "block/hollowed_log"));
        DynamicModelRegistry.register(new WarpCoreDModel(), new Identifier(Arborealis.MOD_ID, "block/warp_core"));
        DynamicModelRegistry.register(new WarpCoreDModel(), new Identifier(Arborealis.MOD_ID, "item/warp_core"));
        DynamicModelRegistry.register(new CarvedStencilDModel(), new ModelIdentifier("arborealis:item/stencil_carved#inventory"));

        DynamicModelRegistry.registerModels();

        // Screen Handlers
        ScreenRegistry.register(Arborealis.STENCIL_BAG_SCREEN_HANDLER, StencilBagScreen::new);

        // Block Entity Renderers
        BlockEntityRendererRegistry.register(Arborealis.HOLLOWED_LOG_ENTITY, HollowedLogEntityRenderer::new);
        BlockEntityRendererRegistry.register(Arborealis.WARP_CORE_ENTITY, WarpCoreEntityRenderer::new);
        BlockEntityRendererRegistry.register(Arborealis.PROJECTOR_ENTITY, ProjectorBlockEntityRenderer::new);

        // Colour providers
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableItem)stack.getItem()).getColor(stack), Arborealis.STENCIL_BAG);

        // Render Layers
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.HOLLOWED_LOG, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.WARP_SAPLING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Arborealis.WOODEN_BUCKET, RenderLayer.getTranslucent());

        // Particles
        ParticleFactoryRegistry.getInstance().register(Arborealis.WARP_TREE_PARTICLE, WarpTreeParticle.Factory::new);

        // Networking
        ClientPlayNetworking.registerGlobalReceiver(ArborealisConstants.TREE_MAP_INIT, TreeManager::treeMapInit);
        ClientPlayNetworking.registerGlobalReceiver(ArborealisConstants.TREE_MAP_UPDATE, TreeManager::treeMapUpdate);
        ClientPlayNetworking.registerGlobalReceiver(ArborealisConstants.CLIENT_RUNE_PUSH, RuneManager::clientRunePush);
    }
}
