package com.youarethomas.arborealis.rendering;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class BeamRenderLayer extends RenderLayer{

    public static final Identifier BEAM_TEXTURE = new Identifier(Arborealis.MOD_ID, "textures/block/blank.png");
    public static net.minecraft.client.gl.ShaderProgram projectorBeamShader;
    protected static final ShaderProgram PROJECTOR_BEAM_SHADER = new ShaderProgram(BeamRenderLayer::getProjectorBeamShader);

    @Nullable
    public static net.minecraft.client.gl.ShaderProgram getProjectorBeamShader() {
        return projectorBeamShader;
    }

    private static final Function<Identifier, RenderLayer> BEAM_RENDER_LAYER = Util.memoize(texture -> {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(PROJECTOR_BEAM_SHADER).texture(new RenderPhase.Texture((Identifier)texture, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).build(true);
        return RenderLayer.of("beam_render_layer", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, false, true, multiPhaseParameters);
    });
    public static final RenderLayer BEAM_RENDER_LAYER_TEXTURED = getBeamRenderLayer(BEAM_TEXTURE);

    public static RenderLayer getBeamRenderLayer(Identifier texture) {
        return BEAM_RENDER_LAYER.apply(texture);
    }

    public BeamRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }
}
