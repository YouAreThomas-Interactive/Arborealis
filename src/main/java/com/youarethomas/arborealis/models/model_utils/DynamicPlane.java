package com.youarethomas.arborealis.models.model_utils;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

public class DynamicPlane {
    private static final float PIXEL_SIZE = 0.0625f; // Which is 1/16

    private Direction facing;
    private float left;
    private float bottom;
    private float right;
    private float top;
    private float depth;

    private Sprite texture;
    private int overlayColour = -1;
    private boolean isEmissive = false;

    /**
     * A helper class to create planes in block models
     * @param facing Direction the normal of the plane will point out towards (visible when looking from opposite direction).
     * @param left Pixels in from the left.
     * @param bottom Pixels in from the bottom.
     * @param right Pixels in from the right.
     * @param top Pixels in from the top.
     * @param depth Pixels in from the side of the block specified in 'facing'.
     */
    public DynamicPlane(Direction facing, float left, float bottom, float right, float top, float depth) {
        this.facing = facing;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.top = top;
        this.depth = depth;
    }

    public void applyTexture(Sprite texture) { this.texture = texture; }

    public void setEmissive(boolean isEmissive) { this.isEmissive = isEmissive; }

    public void setOverlayColour(int overlayColour) { this.overlayColour = overlayColour; }

    public void create(QuadEmitter emitter) {
        // Create the square itself
        emitter.square(facing, left * PIXEL_SIZE, 1F - bottom * PIXEL_SIZE, right * PIXEL_SIZE, 1F - top * PIXEL_SIZE, depth * PIXEL_SIZE);

        // Assign a texture if provided
        if (texture != null)
            emitter.spriteBake(0, texture, MutableQuadView.BAKE_LOCK_UV);

        // Colour it
        emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);

        // Make it emissive
        if (isEmissive)
            emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());

        // And send it off
        emitter.emit();
    }
}
