package com.youarethomas.arborealis.models.model_utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.function.Function;

public class DynamicCuboid {

    private static final float PIXEL_SIZE = 0.0625f; // Which is 1/16

    public HashMap<Direction, SpriteIdentifier> spriteIds = new HashMap<>();

    private final float x;
    private final float y;
    private final float z;
    private final float xSize;
    private final float ySize;
    private final float zSize;

    private HashMap<Direction, Integer> overlays = new HashMap<>();
    private HashMap<Direction, Boolean> emissives = new HashMap<>();

    public DynamicCuboid(float x, float y, float z, float xSize, float ySize, float zSize) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        for (Direction direction : Direction.values()) {
            spriteIds.put(direction, null);
        }
    }

    public void setSideOverlay(Direction direction, int colour) {
        if (overlays.containsKey(direction)) {
            overlays.replace(direction, colour);
        } else {
            overlays.put(direction, colour);
        }
    }

    public void setEmissive(Direction direction, boolean isEmissive) {
        if (emissives.containsKey(direction)) {
            emissives.replace(direction, isEmissive);
        } else {
            emissives.put(direction, isEmissive);
        }
    }

    public void applyTextureToAll(SpriteIdentifier spriteIdentifier) {
        spriteIds.clear();

        for (Direction direction : Direction.values()) {
            spriteIds.put(direction, spriteIdentifier);
        }
    }

    public void applyTextureTopAndBottom(SpriteIdentifier spriteIdentifier) {
        spriteIds.replace(Direction.UP, spriteIdentifier);
        spriteIds.replace(Direction.DOWN, spriteIdentifier);
    }

    public void applyTextureSides(SpriteIdentifier spriteIdentifier) {
        spriteIds.replace(Direction.NORTH, spriteIdentifier);
        spriteIds.replace(Direction.EAST, spriteIdentifier);
        spriteIds.replace(Direction.SOUTH, spriteIdentifier);
        spriteIds.replace(Direction.WEST, spriteIdentifier);
    }

    public void applyTexture(Direction side, SpriteIdentifier spriteIdentifier) {
        spriteIds.replace(side, spriteIdentifier);
    }

    @Environment(EnvType.CLIENT)
    public void create(QuadEmitter emitter, Function<SpriteIdentifier, Sprite> textureGetter) {

        /* So emitters are kinda complicated:
           You're drawing a plane from the left-bottom, up to the right-top, so it's important that
           the left and bottom numbers are smaller than the right and top numbers, otherwise you'll
           get inverted planes (texture on the wrong side). */

        if (MinecraftClient.getInstance().world.isClient()) {
            for (Direction direction : Direction.values()) {
                // Overlay things
                int overlayColour = -1;
                if (overlays.containsKey(direction)) {
                    overlayColour = overlays.get(direction);
                }
                // Emissive things
                boolean isEmissive = false;
                if (emissives.containsKey(direction)) {
                    isEmissive = emissives.get(direction);
                }

                switch (direction) {
                    case NORTH -> {
                        emitter.square(direction, 1f - ((x + xSize) * PIXEL_SIZE), y * PIXEL_SIZE, 1f - (x * PIXEL_SIZE), (y + ySize) * PIXEL_SIZE, z * PIXEL_SIZE);
                        emitter.spriteBake(0, textureGetter.apply(spriteIds.get(Direction.NORTH)), MutableQuadView.BAKE_LOCK_UV);
                        emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);
                        if (isEmissive)
                            emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
                        emitter.emit();
                    }
                    case SOUTH -> {
                        emitter.square(direction, x * PIXEL_SIZE, y * PIXEL_SIZE, (x + xSize) * PIXEL_SIZE, (y + ySize) * PIXEL_SIZE, 1f - ((z + zSize) * PIXEL_SIZE));
                        emitter.spriteBake(0, textureGetter.apply(spriteIds.get(Direction.SOUTH)), MutableQuadView.BAKE_LOCK_UV);
                        emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);
                        if (isEmissive)
                            emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
                        emitter.emit();
                    }
                    case EAST -> {
                        emitter.square(direction, 1f - ((z + zSize) * PIXEL_SIZE), y * PIXEL_SIZE, 1f - (z * PIXEL_SIZE), (y + ySize) * PIXEL_SIZE, 1f - ((x + xSize) * PIXEL_SIZE));
                        emitter.spriteBake(0, textureGetter.apply(spriteIds.get(Direction.EAST)), MutableQuadView.BAKE_LOCK_UV);
                        emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);
                        if (isEmissive)
                            emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
                        emitter.emit();
                    }
                    case WEST -> {
                        emitter.square(direction, z * PIXEL_SIZE, y * PIXEL_SIZE, (z + zSize) * PIXEL_SIZE, (y + ySize) * PIXEL_SIZE, x * PIXEL_SIZE);
                        emitter.spriteBake(0, textureGetter.apply(spriteIds.get(Direction.WEST)), MutableQuadView.BAKE_LOCK_UV);
                        emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);
                        if (isEmissive)
                            emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
                        emitter.emit();
                    }
                    case UP -> {
                        emitter.square(direction, x * PIXEL_SIZE, 1f - ((z + zSize) * PIXEL_SIZE), (x + xSize) * PIXEL_SIZE, 1f - (z * PIXEL_SIZE), 1f - ((y + ySize) * PIXEL_SIZE));
                        emitter.spriteBake(0, textureGetter.apply(spriteIds.get(Direction.UP)), MutableQuadView.BAKE_LOCK_UV);
                        emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);
                        if (isEmissive)
                            emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
                        emitter.emit();
                    }
                    case DOWN -> {
                        emitter.square(direction, x * PIXEL_SIZE, z * PIXEL_SIZE, (x + xSize) * PIXEL_SIZE, (z + zSize) * PIXEL_SIZE, y * PIXEL_SIZE);
                        emitter.spriteBake(0, textureGetter.apply(spriteIds.get(Direction.DOWN)), MutableQuadView.BAKE_LOCK_UV);
                        emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);
                        if (isEmissive)
                            emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
                        emitter.emit();
                    }
                }
            }
        }
    }
}
