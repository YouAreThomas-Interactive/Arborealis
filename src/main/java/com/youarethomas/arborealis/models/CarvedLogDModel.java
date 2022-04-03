package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixins.AxeItemAccessor;
import com.youarethomas.arborealis.models.model_utils.DynamicCuboid;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.RuneManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class CarvedLogDModel extends DynamicModel {

    @Override
    public void createBlockQuads(CuboidBuilder builder, BlockRenderView renderView, BlockPos pos) {
        CarvedLogEntity be = (CarvedLogEntity)renderView.getBlockEntity(pos);

        if (be == null) {
            System.out.println("CarvedLogEntity was null");
            return;
        }

        BlockState logState = be.getLogState();

        // Frame
        BakedModel carvedLogFrame = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/carved_log/carved_log_frame"));
        builder.addBakedModel(carvedLogFrame, new CuboidBuilder.RetextureFromBlock(logState, true));

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
        for (Direction dir : Direction.values()) {
            // Check if rune is valid and tree is natural

            if (be.isFaceCatalysed(dir)) {
                Rune rune = be.getFaceRune(dir);

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

        // Carved side north
        int northSideCount = 0;
        for (int y = 13; y >= 1; y -= 2) {
            for (int x = 13; x >= 1; x -= 2) {
                int carveState = be.getFaceArray(Direction.NORTH)[northSideCount];

                // Where a state of 1 means carved - do not render anything
                if (carveState != 1) {
                    DynamicCuboid cuboid;

                    cuboid = new DynamicCuboid(x, y, 0, 2, 2, 1);
                    // 2 means highlighted
                    if (carveState == 2) {
                        cuboid.setSideOverlay(Direction.NORTH, 0x2bff95);
                    }

                    cuboid.applyTexturesFromBlock(logState);
                    builder.addCuboid(cuboid);
                }
                northSideCount++;
            }
        }

        // Carved side east
        int eastSideCount = 0;
        for (int y = 13; y >= 1; y -= 2) {
            for (int z = 13; z >= 1; z -= 2) {
                int carveState = be.getFaceArray(Direction.EAST)[eastSideCount];

                if (carveState != 1) {
                    DynamicCuboid cuboid;

                    cuboid = new DynamicCuboid(15, y, z, 1, 2, 2);
                    if (carveState == 2) {
                        cuboid.setSideOverlay(Direction.EAST, 0x2bff95);
                    }

                    cuboid.applyTexturesFromBlock(logState);
                    builder.addCuboid(cuboid);
                }
                eastSideCount++;
            }
        }

        // Carved side south
        int southSideCount = 0;
        for (int y = 13; y >= 1; y -= 2) {
            for (int x = 1; x <= 13; x += 2) {
                int carveState = be.getFaceArray(Direction.SOUTH)[southSideCount];

                if (carveState != 1) {
                    DynamicCuboid cuboid;

                    cuboid = new DynamicCuboid(x, y, 15, 2, 2, 1);
                    if (carveState == 2) {
                        cuboid.setSideOverlay(Direction.SOUTH, 0x2bff95);
                    }

                    cuboid.applyTexturesFromBlock(logState);
                    builder.addCuboid(cuboid);
                }
                southSideCount++;
            }
        }

        // Carved side west
        int westSideCount = 0;
        for (int y = 13; y >= 1; y -= 2) {
            for (int z = 1; z <= 13; z += 2) {
                int carveState = be.getFaceArray(Direction.WEST)[westSideCount];

                if (carveState != 1) {
                    DynamicCuboid cuboid;

                    cuboid = new DynamicCuboid(0, y, z, 1, 2, 2);
                    if (carveState == 2) {
                        cuboid.setSideOverlay(Direction.WEST, 0x2bff95);
                    }

                    cuboid.applyTexturesFromBlock(logState);
                    builder.addCuboid(cuboid);
                }
                westSideCount++;
            }
        }

        // Carved side top
        int topSideCount = 0;
        for (int x = 13; x >= 1; x -= 2) {
            for (int z = 1; z <= 13; z += 2) {
                int carveState = be.getFaceArray(Direction.UP)[topSideCount];

                if (carveState != 1) {
                    DynamicCuboid cuboid;

                    cuboid = new DynamicCuboid(x, 15, z, 2, 1, 2);
                    if (carveState == 2) {
                        cuboid.setSideOverlay(Direction.UP, 0x2bff95);
                    }

                    cuboid.applyTexturesFromBlock(logState);
                    builder.addCuboid(cuboid);
                }
                topSideCount++;
            }
        }

        // Carved side top
        int bottomSideCount = 0;
        for (int x = 13; x >= 1; x -= 2) {
            for (int z = 1; z <= 13; z += 2) {
                int carveState = be.getFaceArray(Direction.DOWN)[bottomSideCount];

                if (carveState != 1) {
                    DynamicCuboid cuboid;

                    cuboid = new DynamicCuboid(x, 0, z, 2, 1, 2);
                    if (carveState == 2) {
                        cuboid.setSideOverlay(Direction.DOWN, 0x2bff95);
                    }

                    cuboid.applyTexturesFromBlock(logState);
                    builder.addCuboid(cuboid);
                }
                bottomSideCount++;
            }
        }

        //endregion
    }

    @Override
    public void createItemQuads(CuboidBuilder builder, ItemStack itemStack) {

    }
}
