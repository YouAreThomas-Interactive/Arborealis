package com.youarethomas.arborealis.mixins;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Debug(export = true)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow private Map<String, Shader> shaders;

    @ModifyVariable(method = "loadShaders(Lnet/minecraft/resource/ResourceManager;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), ordinal = 1)
    private List<Pair<Shader, Consumer<Shader>>> loadArborealisShader(List<Pair<Shader, Consumer<Shader>>> list, ResourceManager manager) throws IOException {
        list.add(Pair.of(new Shader(manager, "rendertype_projector_beam", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), shader -> {
            BeamRenderLayer.projectorBeamShader = shader;
        }));
        return list;
    }
}
