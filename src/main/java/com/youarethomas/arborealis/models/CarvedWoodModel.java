package com.youarethomas.arborealis.models;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.models.utils.DynamicCuboid;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
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

    private SpriteIdentifier breakTextureIdentifier;
    private Sprite breakTextureSprite;

    public CarvedWoodModel() {
        setBreakTexture(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:block/oak_log")));
    }

    public void loadFixedCuboids(String log, String strippedLog, String logTop) {

        // Core
        DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
        core.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(strippedLog)));
        addFixedCuboid(core);

        // Top
        DynamicCuboid top = new DynamicCuboid(0, 15, 0, 16, 1, 16);
        top.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
        top.applyTexture(Direction.UP, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
        addFixedCuboid(top);

        // Bottom
        DynamicCuboid bottom = new DynamicCuboid(0, 0, 0, 16, 1, 16);
        bottom.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
        bottom.applyTexture(Direction.DOWN, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
        addFixedCuboid(bottom);

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

    public void addFixedCuboid(DynamicCuboid cuboid) {
        fixedCuboids.add(cuboid);
    }

    public void setBreakTexture(SpriteIdentifier spriteIdentifier) {
        breakTextureIdentifier = spriteIdentifier;
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

        breakTextureSprite = textureGetter.apply(breakTextureIdentifier);

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        builder = renderer.meshBuilder();
        emitter = builder.getEmitter();
        this.textureGetter = textureGetter;

        return new ModelBaseBaked();
    }

    public class ModelBaseBaked implements BakedModel, FabricBakedModel {

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            BlockEntity entity = blockView.getBlockEntity(pos);

            if (entity instanceof CarvedWoodEntity) {
                // Bunch of ID stuff...
                String logID = ((CarvedWoodEntity) entity).getLogID();
                String[] idParts = logID.split(":");
                String log = "minecraft:block/oak_log";

                // ... made needlessly complicated due to pumpkins
                if (Objects.equals(((CarvedWoodEntity) entity).getLogID(), "pumpkin")) {
                    log = "minecraft:block/pumpkin_side";
                    String strippedLog = "arborealis:block/pumpkin_side_carved";
                    String logTop = "minecraft:block/pumpkin_top";

                    loadFixedCuboids(log, strippedLog, logTop);
                } else if (idParts.length > 1) {
                    log = idParts[0] + ":block/" + idParts[1];
                    String strippedLog = idParts[0] + ":block/stripped_" + idParts[1];
                    String logTop = idParts[0] + ":block/" + idParts[1] + "_top";

                    loadFixedCuboids(log, strippedLog, logTop);
                }

                // Render the fixed cuboids for the frame
                for (DynamicCuboid cuboid : fixedCuboids) {
                    cuboid.create(emitter, textureGetter);
                }

                //region Carved Face Rendering

                // Carved side north
                int northSideCount = 0;
                for (int y = 1; y <= 13; y += 2) {
                    for (int x = 13; x >= 1; x -= 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.NORTH)[northSideCount];

                        // Where a state of 1 means carved - do not render anything
                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(x, y, 0, 2, 2, 1);
                            // 2 means highlighted
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.NORTH, 0xffa200);
                            }

                            cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                            cuboid.create(emitter, textureGetter);
                        }
                        northSideCount++;
                    }
                }

                // Carved side east
                int eastSideCount = 0;
                for (int y = 1; y <= 13; y += 2) {
                    for (int z = 13; z >= 1; z -= 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.EAST)[eastSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(15, y, z, 1, 2, 2);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.EAST, 0xffa200);
                            }

                            cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                            cuboid.create(emitter, textureGetter);
                        }
                        eastSideCount++;
                    }
                }

                // Carved side south
                int southSideCount = 0;
                for (int y = 1; y <= 13; y += 2) {
                    for (int x = 1; x <= 13; x += 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.SOUTH)[southSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(x, y, 15, 2, 2, 1);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.SOUTH, 0xffa200);
                            }

                            cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                            cuboid.create(emitter, textureGetter);
                        }
                        southSideCount++;
                    }
                }

                // Carved side west
                int westSideCount = 0;
                for (int y = 1; y <= 13; y += 2) {
                    for (int z = 1; z <= 13; z += 2) {
                        int carveState = ((CarvedWoodEntity) entity).getFaceArray(Direction.WEST)[westSideCount];

                        if (carveState != 1) {
                            DynamicCuboid cuboid;

                            cuboid = new DynamicCuboid(0, y, z, 1, 2, 2);
                            if (carveState == 2) {
                                cuboid.setSideOverlay(Direction.WEST, 0xffa200);
                            }

                            cuboid.applyTextureToAll(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
                            cuboid.create(emitter, textureGetter);
                        }
                        westSideCount++;
                    }
                }

                //endregion

                // And send her off!
                context.meshConsumer().accept(builder.build());
            }
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
        public Sprite getSprite() {
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
