package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.rendering.BeamBufferBuilder;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BufferBuilderStorage.class)
public class BufferBuilderStorageMixin {
    @Inject(method = "method_22999(Lit/unimi/dsi/fastutil/objects/Object2ObjectLinkedOpenHashMap;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/TexturedRenderLayers;getShieldPatterns()Lnet/minecraft/client/render/RenderLayer;"))
    private void beamEntityBuilderPut(Object2ObjectLinkedOpenHashMap<RenderLayer, BeamBufferBuilder> map, CallbackInfo ci) {
        map.put(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED, new BeamBufferBuilder(BeamRenderLayer.BEAM_RENDER_LAYER_TEXTURED.getExpectedBufferSize()));
    }
}
