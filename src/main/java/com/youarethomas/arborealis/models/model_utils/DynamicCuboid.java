package com.youarethomas.arborealis.models.model_utils;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class DynamicCuboid {
    private static final float PIXEL_SIZE = 0.0625f; // Which is 1/16

    public HashMap<Direction, Sprite> spriteIds = new HashMap<>();

    private final float x;
    private final float y;
    private final float z;
    private final float xSize;
    private final float ySize;
    private final float zSize;

    private HashMap<Direction, Integer> overlays = new HashMap<>();
    private HashMap<Direction, Boolean> emissives = new HashMap<>();

    private BlockState blockState = null;

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

    public void setAllSideOverlays(int colour) {
        for (Direction direction : Direction.values()) {
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
            spriteIds.put(direction, spriteIdentifier.getSprite());
        }
    }

    public void applyTexture(Direction side, SpriteIdentifier spriteIdentifier) {
        spriteIds.replace(side, spriteIdentifier.getSprite());
    }

    public void applyTexturesFromBlock(BlockState blockState) {
        BakedModel woodModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(blockState);
        this.blockState = blockState;

        spriteIds.clear();
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = woodModel.getQuads(blockState, direction, Arborealis.RANDOM);
            if (quads != null && quads.size() > 0) {
                Sprite sprite = quads.get(0).getSprite();
                spriteIds.put(direction, sprite);
            }
        }
    }

    public void create(QuadEmitter emitter) {
        /* So emitters are kinda complicated:
           You're drawing a plane from the left-bottom, up to the right-top, so it's important that
           the left and bottom numbers are smaller than the right and top numbers, otherwise you'll
           get inverted planes (texture on the wrong side). */

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
                case NORTH -> emitter.square(direction, 1f - ((x + xSize) * PIXEL_SIZE), y * PIXEL_SIZE, 1f - (x * PIXEL_SIZE), (y + ySize) * PIXEL_SIZE, z * PIXEL_SIZE);
                case SOUTH -> emitter.square(direction, x * PIXEL_SIZE, y * PIXEL_SIZE, (x + xSize) * PIXEL_SIZE, (y + ySize) * PIXEL_SIZE, 1f - ((z + zSize) * PIXEL_SIZE));
                case EAST  -> emitter.square(direction, 1f - ((z + zSize) * PIXEL_SIZE), y * PIXEL_SIZE, 1f - (z * PIXEL_SIZE), (y + ySize) * PIXEL_SIZE, 1f - ((x + xSize) * PIXEL_SIZE));
                case WEST  -> emitter.square(direction, z * PIXEL_SIZE, y * PIXEL_SIZE, (z + zSize) * PIXEL_SIZE, (y + ySize) * PIXEL_SIZE, x * PIXEL_SIZE);
                case UP    -> emitter.square(direction, x * PIXEL_SIZE, 1f - ((z + zSize) * PIXEL_SIZE), (x + xSize) * PIXEL_SIZE, 1f - (z * PIXEL_SIZE), 1f - ((y + ySize) * PIXEL_SIZE));
                case DOWN  -> emitter.square(direction, x * PIXEL_SIZE, z * PIXEL_SIZE, (x + xSize) * PIXEL_SIZE, (z + zSize) * PIXEL_SIZE, y * PIXEL_SIZE);
            }

            BakeTexture(emitter, direction, blockState);
            emitter.spriteColor(0, overlayColour, overlayColour, overlayColour, overlayColour);
            if (isEmissive)
                emitter.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(0, true).find());
            emitter.emit();
        }
    }

    private void BakeTexture(QuadEmitter emitter, Direction direction, BlockState blockState) {
        if (spriteIds.containsKey(direction) && spriteIds.get(direction) != null) {
            // Sprite rotation, courtesy of bitwise voo-doo
            if (blockState != null && blockState.contains(PillarBlock.AXIS)) {
                Direction.Axis axisInfo = blockState.get(PillarBlock.AXIS);

                if (axisInfo == Direction.Axis.Z) {
                    // North-south (rotate east/west)
                    switch (direction) {
                        case EAST ->  emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_90);
                        case WEST -> emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_270);
                        case DOWN -> emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_180);
                        default -> emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
                    }
                } else if (axisInfo == Direction.Axis.X) {
                    // East-west (rotate north/south, up/down)
                    switch (direction) {
                        case UP, DOWN, SOUTH -> emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_90);
                        case NORTH -> emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_270);
                        default -> emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
                    }
                } else {
                    emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
                }
            } else {
                emitter.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
            }
        }
    }
}
