package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixins.AxeItemAccessor;
import com.youarethomas.arborealis.models.model_utils.DynamicBakedModel;
import com.youarethomas.arborealis.models.model_utils.DynamicCuboid;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import com.youarethomas.arborealis.runes.Rune;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.Objects;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class CarvedLogDModel extends DynamicModel {

    @Override
    public void createBlockQuads(DynamicModelBuilder builder, BlockRenderView renderView, BlockPos pos) {
        CarvedLogEntity be = (CarvedLogEntity)renderView.getBlockEntity(pos);

        if (be == null) {
            System.out.println("CarvedLogEntity was null");
            return;
        }

        BlockState logState = be.getLogState();

        // Frame
        BakedModel carvedLogFrame = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/carved_log/carved_log_frame"));
        builder.addBakedModel(carvedLogFrame, new DynamicModelBuilder.RetextureFromBlock(logState, true));

        // Core
        DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
        if (be.getLogState().isOf(Blocks.PUMPKIN)) {
            core.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "block/pumpkin_side_carved")));
        } else {
            Block strippedLog = AxeItemAccessor.getStrippedBlocks().get(logState.getBlock());
            if (strippedLog != null)
                core.applyTexturesFromBlock(strippedLog.getStateWithProperties(logState));
        }

        // If the face has a rune, make it glow
        boolean hasLightRune = false;
        Rune rune = null;
        for (Direction dir : Direction.values()) {
            // Check if rune is valid and tree is natural

            if (be.isFaceCatalysed(dir)) {
                rune = be.getFaceRune(dir);

                if (rune != null) {
                    if (Objects.equals(rune.name, "light"))
                        hasLightRune = true;

                    if (be.areRunesActive()) {
                        core.setSideOverlay(dir, rune.getIntColour());
                    } else {
                        core.setSideOverlay(dir, 0x545454);
                    }
                    core.applyTexture(dir, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                } else {
                    core.setSideOverlay(dir, -1);
                }
            }

            // Make face blue if it's in the correct position relative to a warp core
            if (dir.getHorizontal() != -1) {
                BlockPos warpCorePos = be.getPos().offset(Direction.DOWN, 2).offset(dir, 2);

                if (be.getWorld().getBlockState(warpCorePos).isOf(Arborealis.WARP_CORE)) {
                    core.applyTexture(dir, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                    core.setSideOverlay(dir, 0x64D4CD);
                }
            }

            // If block has glow ink applied, make the face emissive
            if (be.isFaceEmissive(dir)) {
                core.setEmissive(dir, true);
            }
        }

        if (be.getLogState().isOf(Blocks.PUMPKIN) && hasLightRune) {
            core.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "block/pumpkin_side_lit")));
            core.setAllSideOverlays(-1);
        }

        builder.addCuboid(core);

        //region Carved Face Rendering

        int planColor = 0x2bff95;

        // Carved side north
        int northSideCount = 0;
        for (int y = 11; y >= 3; y -= 2) {
            for (int x = 11; x >= 3; x -= 2) {
                if (northSideCount < be.getFaceArray(Direction.NORTH).length) {
                    int carveState = be.getFaceArray(Direction.NORTH)[northSideCount];

                    // Where a state of 1 means carved - do not render anything
                    if (carveState != 1) {
                        DynamicCuboid cuboid;

                        cuboid = new DynamicCuboid(x, y, 0, 2, 2, 1);
                        cuboid.applyTexturesFromBlock(logState);

                        // 2 means highlighted
                        if (carveState == 2) {
                            cuboid.setSideOverlay(Direction.NORTH, planColor);
                        } else if (carveState == 3 && rune != null && be.areRunesActive()) {
                            cuboid.applyTexture(Direction.NORTH, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                            cuboid.setSideOverlay(Direction.NORTH, rune.getIntColour());
                        }

                        builder.addCuboid(cuboid);
                    }
                    northSideCount++;
                }
            }
        }

        // Carved side east
        int eastSideCount = 0;
        for (int y = 11; y >= 3; y -= 2) {
            for (int z = 11; z >= 3; z -= 2) {
                if (eastSideCount < be.getFaceArray(Direction.EAST).length) {
                    int carveState = be.getFaceArray(Direction.EAST)[eastSideCount];

                    if (carveState != 1) {
                        DynamicCuboid cuboid;

                        cuboid = new DynamicCuboid(15, y, z, 1, 2, 2);
                        cuboid.applyTexturesFromBlock(logState);

                        if (carveState == 2) {
                            cuboid.setSideOverlay(Direction.EAST, planColor);
                        } else if (carveState == 3 && rune != null && be.areRunesActive()) {
                            cuboid.applyTexture(Direction.EAST, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                            cuboid.setSideOverlay(Direction.EAST, rune.getIntColour());
                        }

                        builder.addCuboid(cuboid);
                    }
                    eastSideCount++;
                }
            }
        }

        // Carved side south
        int southSideCount = 0;
        for (int y = 11; y >= 3; y -= 2) {
            for (int x = 3; x <= 11; x += 2) {
                if (southSideCount < be.getFaceArray(Direction.SOUTH).length) {
                    int carveState = be.getFaceArray(Direction.SOUTH)[southSideCount];

                    if (carveState != 1) {
                        DynamicCuboid cuboid;

                        cuboid = new DynamicCuboid(x, y, 15, 2, 2, 1);
                        cuboid.applyTexturesFromBlock(logState);

                        if (carveState == 2) {
                            cuboid.setSideOverlay(Direction.SOUTH, planColor);
                        } else if (carveState == 3 && rune != null && be.areRunesActive()) {
                            cuboid.applyTexture(Direction.SOUTH, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                            cuboid.setSideOverlay(Direction.SOUTH, rune.getIntColour());
                        }

                        builder.addCuboid(cuboid);
                    }
                    southSideCount++;
                }
            }
        }

        // Carved side west
        int westSideCount = 0;
        for (int y = 11; y >= 3; y -= 2) {
            for (int z = 3; z <= 11; z += 2) {
                if (westSideCount < be.getFaceArray(Direction.WEST).length) {
                    int carveState = be.getFaceArray(Direction.WEST)[westSideCount];

                    if (carveState != 1) {
                        DynamicCuboid cuboid;

                        cuboid = new DynamicCuboid(0, y, z, 1, 2, 2);
                        cuboid.applyTexturesFromBlock(logState);

                        if (carveState == 2) {
                            cuboid.setSideOverlay(Direction.WEST, planColor);
                        } else if (carveState == 3 && rune != null && be.areRunesActive()) {
                            cuboid.applyTexture(Direction.WEST, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                            cuboid.setSideOverlay(Direction.WEST, rune.getIntColour());
                        }

                        builder.addCuboid(cuboid);
                    }
                    westSideCount++;
                }
            }
        }

        // Carved side top
        int topSideCount = 0;
        for (int x = 11; x >= 3; x -= 2) {
            for (int z = 3; z <= 11; z += 2) {
                if (topSideCount < be.getFaceArray(Direction.UP).length) {
                    int carveState = be.getFaceArray(Direction.UP)[topSideCount];

                    if (carveState != 1) {
                        DynamicCuboid cuboid;

                        cuboid = new DynamicCuboid(x, 15, z, 2, 1, 2);
                        cuboid.applyTexturesFromBlock(logState);

                        if (carveState == 2) {
                            cuboid.setSideOverlay(Direction.UP, planColor);
                        } else if (carveState == 3 && rune != null && be.areRunesActive()) {
                            cuboid.applyTexture(Direction.UP, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                            cuboid.setSideOverlay(Direction.UP, rune.getIntColour());
                        }

                        builder.addCuboid(cuboid);
                    }
                    topSideCount++;
                }
            }
        }

        // Carved side top
        int bottomSideCount = 0;
        for (int x = 11; x >= 3; x -= 2) {
            for (int z = 3; z <= 11; z += 2) {
                if (bottomSideCount < be.getFaceArray(Direction.UP).length) {
                    int carveState = be.getFaceArray(Direction.DOWN)[bottomSideCount];

                    if (carveState != 1) {
                        DynamicCuboid cuboid;

                        cuboid = new DynamicCuboid(x, 0, z, 2, 1, 2);
                        cuboid.applyTexturesFromBlock(logState);

                        if (carveState == 2) {
                            cuboid.setSideOverlay(Direction.DOWN, planColor);
                        } else if (carveState == 3 && rune != null && be.areRunesActive()) {
                            cuboid.applyTexture(Direction.DOWN, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                            cuboid.setSideOverlay(Direction.DOWN, rune.getIntColour());
                        }

                        builder.addCuboid(cuboid);
                    }
                    bottomSideCount++;
                }
            }
        }

        //endregion
    }

    @Override
    public void createItemQuads(DynamicModelBuilder builder, ItemStack itemStack) {

    }
}
