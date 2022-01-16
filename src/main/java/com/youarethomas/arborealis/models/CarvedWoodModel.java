package com.youarethomas.arborealis.models;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.mixins.AxeItemAccessor;
import com.youarethomas.arborealis.models.model_utils.DynamicCuboid;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.util.TreeManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class CarvedWoodModel implements UnbakedModel {
    private static final ThreadLocal<Collection<DynamicCuboid>> CUBOIDS = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<MeshBuilder> MESH_BUILDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().meshBuilder());

    public void addFixedCuboid(DynamicCuboid cuboid) {
        CUBOIDS.get().add(cuboid);
    }

    public Collection<SpriteIdentifier> getTextures() {
        List<SpriteIdentifier> spriteIds = new ArrayList<>();

        for (DynamicCuboid cuboid : CUBOIDS.get()) {
            for (int sprite = 0; sprite < cuboid.spriteIds.size(); sprite++) {
                spriteIds.add(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, cuboid.spriteIds.get(sprite).getId()));
            }
        }

        return spriteIds;
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return getTextures();
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return new Baked();
    }

    public class Baked implements BakedModel, FabricBakedModel {

        @Environment(EnvType.CLIENT)
        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            BlockEntity entity = blockView.getBlockEntity(pos);

            if (entity instanceof CarvedWoodEntity be) {
                BlockState logState = be.getLogState();

                // ... made needlessly complicated due to pumpkins
                loadFixedCuboids(logState);

                // Core
                DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
                Block strippedLog = AxeItemAccessor.getStrippedBlocks().get(logState.getBlock());
                if (strippedLog != null)
                    core.applyTexturesFromBlock(strippedLog.getStateWithProperties(logState));

                // If the face has a rune, make it glow
                for (Direction dir : Direction.values()) {
                    int[] faceArray = be.getFaceArray(dir);

                    // Check if rune is valid and tree is natural
                    if (be.getFaceCatalysed(dir) && RuneManager.isValidRune(faceArray) && TreeManager.getTreeStructureFromBlock(pos, MinecraftClient.getInstance().world).isNatural()) {
                        AbstractRune rune = RuneManager.getRuneFromArray(faceArray);
                        if (rune != null) {
                            if (be.getRunesActive()) {
                                core.setSideOverlay(dir, rune.getIntColour());
                            } else {
                                core.setSideOverlay(dir, 0x545454);
                            }
                            core.applyTexture(dir, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                        } else {
                            core.setSideOverlay(dir, -1);
                        }
                    }

                    if (be.getFaceGlow(dir)) {
                        core.setEmissive(dir, true);
                    }
                }

                CUBOIDS.get().add(core);

                //region Carved Face Rendering

                // Carved side north
                int northSideCount = 0;
                for (int y = 13; y >= 1; y -= 2) {
                    for (int x = 13; x >= 1; x -= 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.NORTH)[northSideCount];

                        // Where a state of 1 means carved - do not render anything
                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(x, y, 0, 2, 2, 1);
                            // 2 means highlighted
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.NORTH, 0x2bff95);
                            }

                            cuboid.applyTexturesFromBlock(logState);
                            cuboid.create(MESH_BUILDER.get().getEmitter(), logState);
                        }
                        northSideCount++;
                    }
                }

                // Carved side east
                int eastSideCount = 0;
                for (int y = 13; y >= 1; y -= 2) {
                    for (int z = 13; z >= 1; z -= 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.EAST)[eastSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(15, y, z, 1, 2, 2);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.EAST, 0x2bff95);
                            }

                            cuboid.applyTexturesFromBlock(logState);
                            cuboid.create(MESH_BUILDER.get().getEmitter(), logState);
                        }
                        eastSideCount++;
                    }
                }

                // Carved side south
                int southSideCount = 0;
                for (int y = 13; y >= 1; y -= 2) {
                    for (int x = 1; x <= 13; x += 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.SOUTH)[southSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(x, y, 15, 2, 2, 1);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.SOUTH, 0x2bff95);
                            }

                            cuboid.applyTexturesFromBlock(logState);
                            cuboid.create(MESH_BUILDER.get().getEmitter(), logState);
                        }
                        southSideCount++;
                    }
                }

                // Carved side west
                int westSideCount = 0;
                for (int y = 13; y >= 1; y -= 2) {
                    for (int z = 1; z <= 13; z += 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.WEST)[westSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(0, y, z, 1, 2, 2);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.WEST, 0x2bff95);
                            }

                            cuboid.applyTexturesFromBlock(logState);
                            cuboid.create(MESH_BUILDER.get().getEmitter(), logState);
                        }
                        westSideCount++;
                    }
                }

                // Carved side top
                int topSideCount = 0;
                for (int x = 13; x >= 1; x -= 2) {
                    for (int z = 1; z <= 13; z += 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.UP)[topSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(x, 15, z, 2, 1, 2);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.UP, 0x2bff95);
                            }

                            cuboid.applyTexturesFromBlock(logState);
                            cuboid.create(MESH_BUILDER.get().getEmitter(), logState);
                        }
                        topSideCount++;
                    }
                }

                // Carved side top
                int bottomSideCount = 0;
                for (int x = 13; x >= 1; x -= 2) {
                    for (int z = 1; z <= 13; z += 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.DOWN)[bottomSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(x, 0, z, 2, 1, 2);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.DOWN, 0x2bff95);
                            }

                            cuboid.applyTexturesFromBlock(logState);
                            cuboid.create(MESH_BUILDER.get().getEmitter(), logState);
                        }
                        bottomSideCount++;
                    }
                }

                //endregion

                for (DynamicCuboid cuboid : CUBOIDS.get()) {
                    cuboid.create(MESH_BUILDER.get().getEmitter(), logState);
                }

                // And send her off!
                context.meshConsumer().accept(MESH_BUILDER.get().build());
            }
        }

        public void loadFixedCuboids(BlockState blockState) {
            // Top
            DynamicCuboid topNorth = new DynamicCuboid(0, 15, 0, 16, 1, 1);
            topNorth.applyTexturesFromBlock(blockState);
            addFixedCuboid(topNorth);

            DynamicCuboid topSouth = new DynamicCuboid(0, 15, 15, 16, 1, 1);
            topSouth.applyTexturesFromBlock(blockState);
            addFixedCuboid(topSouth);

            DynamicCuboid topEast = new DynamicCuboid(15, 15, 1, 1, 1, 14);
            topEast.applyTexturesFromBlock(blockState);
            addFixedCuboid(topEast);

            DynamicCuboid topWest = new DynamicCuboid(0, 15, 1, 1, 1, 14);
            topWest.applyTexturesFromBlock(blockState);
            addFixedCuboid(topWest);

            // Bottom
            DynamicCuboid bottomNorth = new DynamicCuboid(0, 0, 0, 16, 1, 1);
            bottomNorth.applyTexturesFromBlock(blockState);
            addFixedCuboid(bottomNorth);

            DynamicCuboid bottomSouth = new DynamicCuboid(0, 0, 15, 16, 1, 1);
            bottomSouth.applyTexturesFromBlock(blockState);
            addFixedCuboid(bottomSouth);

            DynamicCuboid bottomEast = new DynamicCuboid(15, 0, 1, 1, 1, 14);
            bottomEast.applyTexturesFromBlock(blockState);
            addFixedCuboid(bottomEast);

            DynamicCuboid bottomWest = new DynamicCuboid(0, 0, 1, 1, 1, 14);
            bottomWest.applyTexturesFromBlock(blockState);
            addFixedCuboid(bottomWest);

            // Pillars
            DynamicCuboid pillar1 = new DynamicCuboid(0, 1, 0, 1, 14, 1);
            pillar1.applyTexturesFromBlock(blockState);
            addFixedCuboid(pillar1);

            DynamicCuboid pillar2 = new DynamicCuboid(0, 1, 15, 1, 14, 1);
            pillar2.applyTexturesFromBlock(blockState);
            addFixedCuboid(pillar2);

            DynamicCuboid pillar3 = new DynamicCuboid(15, 1, 15, 1, 14, 1);
            pillar3.applyTexturesFromBlock(blockState);
            addFixedCuboid(pillar3);

            DynamicCuboid pillar4 = new DynamicCuboid(15, 1, 0, 1, 14, 1);
            pillar4.applyTexturesFromBlock(blockState);
            addFixedCuboid(pillar4);
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            // No. I don't think I will
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return null;
        }

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean hasDepth() {
            return false;
        }

        @Override
        public boolean isSideLit() {
            return false;
        }

        @Override
        public boolean isBuiltin() {
            return false;
        }

        @Override
        public Sprite getParticleSprite() {
            return null; // Particle sprite is handled by turning the block back into the original log before breaking
        }

        @Override
        public ModelTransformation getTransformation() {
            return ModelTransformation.NONE;
        }

        @Override
        public ModelOverrideList getOverrides() {
            return ModelOverrideList.EMPTY;
        }
    }
}
