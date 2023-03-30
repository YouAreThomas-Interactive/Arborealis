package com.youarethomas.arborealis.mixins;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.rendering.BeamRenderLayer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Debug(export = true)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyVariable(method = "loadPrograms",at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), ordinal = 1)
    private List<Pair<ShaderProgram, Consumer<ShaderProgram>>> loadArborealisShader(List<Pair<ShaderProgram, Consumer<ShaderProgram>>> list2, ResourceFactory factory) throws IOException {
        list2.add(Pair.of(new ShaderProgram(factory, "rendertype_projector_beam", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL), shader -> {
            BeamRenderLayer.projectorBeamShader = shader;
        }));
        return list2;
    }
}
