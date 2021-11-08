package com.youarethomas.arborealis.models;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.models.model_utils.DynamicCuboid;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.util.TreeManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CarvedWoodModel implements UnbakedModel {

    private final Collection<DynamicCuboid> fixedCuboids = new ArrayList<>();

    private MeshBuilder builder;
    private QuadEmitter emitter;
    private Function<SpriteIdentifier, Sprite> textureGetter;

    private Sprite breakTextureSprite;

    public void addFixedCuboid(DynamicCuboid cuboid) {
        fixedCuboids.add(cuboid);
    }

    public Collection<SpriteIdentifier> getTextures() {

        Stream<SpriteIdentifier> spriteIDs = Stream.<SpriteIdentifier>builder().build();

        for (DynamicCuboid cuboid : fixedCuboids) {
            spriteIDs = Stream.concat(spriteIDs, cuboid.spriteIds.values().stream());
        }

        return spriteIDs.toList();
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return getTextures();
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        //breakTextureSprite = textureGetter.apply(breakTextureIdentifier);

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        builder = renderer.meshBuilder();
        emitter = builder.getEmitter();
        this.textureGetter = textureGetter;

        return new ModelBaseBaked();
    }

    @Environment(EnvType.CLIENT)
    public class ModelBaseBaked implements BakedModel, FabricBakedModel {

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            BlockEntity entity = blockView.getBlockEntity(pos);
            World world = MinecraftClient.getInstance().world;

            if (Objects.requireNonNull(world).isClient()) {
                if (entity instanceof CarvedWoodEntity) {
                    CarvedWoodEntity carvedEntity = (CarvedWoodEntity)entity;
                    // Bunch of ID stuff...
                    String logID = carvedEntity.getLogID();
                    String[] idParts = logID.split(":");
                    String log = "minecraft:block/oak_log";
                    String strippedLog = "minecraft:block/stripped_oak_log";
                    String logEnd = "minecraft:block/oak_log_top";

                    // ... made needlessly complicated due to pumpkins
                    if (Objects.equals(carvedEntity.getLogID(), "pumpkin")) {
                        log = "minecraft:block/pumpkin_side";
                        if (state.get(Properties.LIT)) {
                            strippedLog = "arborealis:block/pumpkin_side_lit";
                        } else {
                            strippedLog = "arborealis:block/pumpkin_side_carved";
                        }
                        logEnd = "minecraft:block/pumpkin_top";

                        loadFixedCuboids(log, strippedLog, logEnd);
                    } else if (idParts.length > 1) {
                        log = idParts[0] + ":block/" + idParts[1];
                        strippedLog = idParts[0] + ":block/stripped_" + idParts[1];
                        logEnd = idParts[0] + ":block/" + idParts[1] + "_top";

                        loadFixedCuboids(log, strippedLog, logEnd);
                    }

                    breakTextureSprite = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)).getSprite();

                    // Core
                    DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
                    core.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(strippedLog)));
                    core.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logEnd)));

                    // If the face has a rune, make it glow
                    for (Direction dir : Direction.values()) {
                        CarvedWoodEntity be = ((CarvedWoodEntity) entity);
                        int[] faceArray = be.getFaceArray(dir);

                        // Check if rune is valid and tree is natural
                        if (be.getFaceActive(dir) && RuneManager.isValidRune(faceArray) && TreeManager.getTreeStructureFromBlock(pos, world).isNatural()) {
                            AbstractRune rune = RuneManager.getRuneFromArray(faceArray);
                            if (rune != null) {
                                core.setSideOverlay(dir, rune.getColour());
                                core.applyTexture(dir, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("arborealis:rune/rune")));
                                core.setEmissive(dir, true);
                            } else {
                                core.setSideOverlay(dir, -1);
                            }
                        }
                    }

                    fixedCuboids.add(core);

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

                                cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                                cuboid.create(emitter, textureGetter);
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

                                cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                                cuboid.create(emitter, textureGetter);
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

                                cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                                cuboid.create(emitter, textureGetter);
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

                                cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                                cuboid.create(emitter, textureGetter);
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

                                cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logEnd)));
                                cuboid.create(emitter, textureGetter);
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

                                cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logEnd)));
                                cuboid.create(emitter, textureGetter);
                            }
                            bottomSideCount++;
                        }
                    }

                    //endregion

                    for (DynamicCuboid cuboid : fixedCuboids) {
                        cuboid.create(emitter, textureGetter);
                    }

                    // And send her off!
                    context.meshConsumer().accept(builder.build());
                }
            }
        }

        public void loadFixedCuboids(String log, String strippedLog, String logTop) {
            // Top
            /*DynamicCuboid top = new DynamicCuboid(0, 15, 0, 16, 1, 16);
            top.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            top.applyTexture(Direction.UP, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            addFixedCuboid(top);*/

            DynamicCuboid topNorth = new DynamicCuboid(0, 15, 0, 16, 1, 1);
            topNorth.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            topNorth.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(topNorth);

            DynamicCuboid topSouth = new DynamicCuboid(0, 15, 15, 16, 1, 1);
            topSouth.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            topSouth.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(topSouth);

            DynamicCuboid topEast = new DynamicCuboid(15, 15, 1, 1, 1, 14);
            topEast.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            topEast.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(topEast);

            DynamicCuboid topWest = new DynamicCuboid(0, 15, 1, 1, 1, 14);
            topWest.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            topWest.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(topWest);

            // Bottom
            /*DynamicCuboid bottom = new DynamicCuboid(0, 0, 0, 16, 1, 16);
            bottom.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            bottom.applyTexture(Direction.DOWN, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            addFixedCuboid(bottom);*/

            DynamicCuboid bottomNorth = new DynamicCuboid(0, 0, 0, 16, 1, 1);
            bottomNorth.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            bottomNorth.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(bottomNorth);

            DynamicCuboid bottomSouth = new DynamicCuboid(0, 0, 15, 16, 1, 1);
            bottomSouth.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            bottomSouth.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(bottomSouth);

            DynamicCuboid bottomEast = new DynamicCuboid(15, 0, 1, 1, 1, 14);
            bottomEast.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            bottomEast.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(bottomEast);

            DynamicCuboid bottomWest = new DynamicCuboid(0, 0, 1, 1, 1, 14);
            bottomWest.applyTextureTopAndBottom(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
            bottomWest.applyTextureSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(bottomWest);

            // Pillars
            DynamicCuboid pillar1 = new DynamicCuboid(0, 1, 0, 1, 14, 1);
            pillar1.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(pillar1);

            DynamicCuboid pillar2 = new DynamicCuboid(0, 1, 15, 1, 14, 1);
            pillar2.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(pillar2);

            DynamicCuboid pillar3 = new DynamicCuboid(15, 1, 15, 1, 14, 1);
            pillar3.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
            addFixedCuboid(pillar3);

            DynamicCuboid pillar4 = new DynamicCuboid(15, 1, 0, 1, 14, 1);
            pillar4.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
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
            return breakTextureSprite;
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
