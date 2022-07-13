package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.KeyboardMixinAccess;
import com.youarethomas.arborealis.rendering.BeamBufferBuilder;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import com.youarethomas.arborealis.util.TreeManagerRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow private MinecraftClient client;
    @Shadow private ClientWorld world;
    @Shadow private BufferBuilderStorage bufferBuilders;
    TreeManagerRenderer treeManagerRenderer = new TreeManagerRenderer();

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V", at = @At("HEAD"))
    private void treeManagerRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (((KeyboardMixinAccess)(Object)client.keyboard).getRenderTreeOutlines())
            treeManagerRenderer.render(matrices, bufferBuilders.getOutlineVertexConsumers(), camera, world);
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TexturedRenderLayers;getEntityTranslucentCull()Lnet/minecraft/client/render/RenderLayer;", shift = At.Shift.AFTER))
    private void projectorBeamRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        VertexConsumerProvider.Immediate immediate = bufferBuilders.getEffectVertexConsumers();

        immediate.draw(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED);
    }
}
