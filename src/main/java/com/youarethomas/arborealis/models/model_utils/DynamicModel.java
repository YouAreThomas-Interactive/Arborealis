package com.youarethomas.arborealis.models.model_utils;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class DynamicModel implements UnbakedModel {
    private static final ThreadLocal<MeshBuilder> MESH_BUILDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().meshBuilder());

    // The minecraft default block and item models
    private static final Identifier DEFAULT_BLOCK_MODEL = new Identifier("minecraft:block/block");
    private static final Identifier DEFAULT_ITEM_MODEL = new Identifier("minecraft:item/generated");

    // A blank invisible texture
    private static final SpriteIdentifier INVISIBLE_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "invisible"));

    private ModelTransformation transformation;

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Arrays.asList(DEFAULT_BLOCK_MODEL);
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) { return Arrays.asList(INVISIBLE_TEXTURE); }

    public abstract void createBlockQuads(CuboidBuilder builder, BlockRenderView renderView, BlockPos pos);

    public abstract void createItemQuads(CuboidBuilder builder, ItemStack itemStack);

    public boolean renderItemAsBlock() { return true; }

    public SpriteIdentifier getParticleSpriteId() { return INVISIBLE_TEXTURE; }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        // If marked to render as block, use default block transformation. Otherwise, use item transformation
        JsonUnbakedModel defaultTransform;
        if (renderItemAsBlock()) {
            defaultTransform = (JsonUnbakedModel) loader.getOrLoadModel(DEFAULT_BLOCK_MODEL);
        } else {
            defaultTransform = (JsonUnbakedModel) loader.getOrLoadModel(DEFAULT_ITEM_MODEL);
        }

        transformation = defaultTransform.getTransformations();

        return new BakedDynamicModel();
    }

    public class BakedDynamicModel implements BakedModel, FabricBakedModel {

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            CuboidBuilder builder = new CuboidBuilder();
            createBlockQuads(builder, blockView, pos);

            // Render out the pre-baked models, doing the retexture after the rotation transform
            for (var model : builder.models.get().entrySet()) {
                if (model.getValue() != null) {
                    context.pushTransform(model.getValue()); // Retexture transform
                    if (state.contains(HorizontalFacingBlock.FACING)) context.pushTransform(new CuboidBuilder.RotateToFacing(state.get(HorizontalFacingBlock.FACING))); // Rotation transform

                    context.fallbackConsumer().accept(model.getKey());

                    if (state.contains(HorizontalFacingBlock.FACING)) context.popTransform();
                    context.popTransform();
                } else {
                    context.fallbackConsumer().accept(model.getKey());
                }
            }

            loadDynamicModels(builder, context);
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            CuboidBuilder builder = new CuboidBuilder();
            createItemQuads(builder, stack);

            // Render out the pre-baked models
            for (var model : builder.models.get().entrySet()) {
                if (model.getValue() != null) {
                    context.pushTransform(model.getValue());
                    context.fallbackConsumer().accept(model.getKey());
                    context.popTransform();
                } else {
                    context.fallbackConsumer().accept(model.getKey());
                }
            }

            loadDynamicModels(builder, context);
        }

        private void loadDynamicModels(CuboidBuilder builder, RenderContext context) {
            for (DynamicCuboid cuboid : builder.cuboids.get()) {
                cuboid.create(MESH_BUILDER.get().getEmitter());
            }

            for (DynamicPlane plane : builder.planes.get()) {
                plane.create(MESH_BUILDER.get().getEmitter());
            }

            context.meshConsumer().accept(MESH_BUILDER.get().build());
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
            // Particle sprite is handled by turning the block back into the original log before breaking. This invisible texture controls things like the running texture... or lack thereof
            return getParticleSpriteId().getSprite();
        }

        @Override
        public ModelTransformation getTransformation() {
            return transformation;
        }

        @Override
        public ModelOverrideList getOverrides() {
            return ModelOverrideList.EMPTY;
        }
    }

    protected class CuboidBuilder {
        ThreadLocal<Collection<DynamicCuboid>> cuboids = ThreadLocal.withInitial(ArrayList::new);
        ThreadLocal<Collection<DynamicPlane>> planes = ThreadLocal.withInitial(ArrayList::new);
        ThreadLocal<Map<BakedModel, RenderContext.QuadTransform>> models = ThreadLocal.withInitial(HashMap::new);

        public BakedModel getModel(Identifier identifier) {
            return BakedModelManagerHelper.getModel(MinecraftClient.getInstance().getBakedModelManager(), identifier);
        }

        public BakedModel getModelFromItem(Item item) {
            return MinecraftClient.getInstance().getItemRenderer().getModels().getModel(item);
        }

        public void addCuboid(DynamicCuboid cuboid) {
            cuboids.get().add(cuboid);
        }

        public void addPlane(DynamicPlane plane) {planes.get().add(plane); }

        public void addBakedModel(BakedModel model) {
            models.get().put(model, null);
        }

        public void addBakedModel(BakedModel model, RenderContext.QuadTransform transform) {
            models.get().put(model, transform);
        }

        public static class RetextureFromBlock implements RenderContext.QuadTransform {
            private final BlockState blockState;
            private boolean lockTextures = false;
            HashMap<Direction, Sprite> spriteIds = new HashMap<>();

            /**
             * Rip the textures off the given block.
             * @param blockState The block to take the textures from.
             * @param lockTextures If true, textures are locked despite the block's rotation.
             */
            public RetextureFromBlock(BlockState blockState, boolean lockTextures)
            {
                this.blockState = blockState;
                this.lockTextures = lockTextures;
                BakedModel woodModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(blockState);

                for (Direction dir : Direction.values()) {
                    List<BakedQuad> quads = woodModel.getQuads(blockState, dir, Arborealis.RANDOM);
                    if (quads != null && quads.size() > 0) {
                        Sprite sprite = quads.get(0).getSprite();
                        spriteIds.put(dir, sprite);
                    }
                }
            }

            @Override
            public boolean transform(MutableQuadView quadView)
            {
                BakeTexture(quadView, quadView.nominalFace(), blockState);
                return true;
            }

            private void BakeTexture(MutableQuadView quadView, Direction direction, BlockState blockState) {
                if (spriteIds.containsKey(direction) && spriteIds.get(direction) != null) {
                    // Sprite rotation, courtesy of bitwise voo-doo
                    if (blockState != null && blockState.contains(PillarBlock.AXIS) && lockTextures) {
                        Direction.Axis axisInfo = blockState.get(PillarBlock.AXIS);

                        if (axisInfo == Direction.Axis.Z) {
                            // North-south (rotate east/west)
                            switch (direction) {
                                case EAST ->  quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_90);
                                case WEST -> quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_270);
                                case DOWN -> quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_180);
                                default -> quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
                            }
                        } else if (axisInfo == Direction.Axis.X) {
                            // East-west (rotate north/south, up/down)
                            switch (direction) {
                                case UP, DOWN, SOUTH -> quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_90);
                                case NORTH -> quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV | MutableQuadView.BAKE_ROTATE_270);
                                default -> quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
                            }
                        } else {
                            quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
                        }
                    } else {
                        quadView.spriteBake(0, spriteIds.get(direction), MutableQuadView.BAKE_LOCK_UV);
                    }
                }
            }
        }

        public static class RotateToFacing implements RenderContext.QuadTransform {
            private final Direction facing;

            public RotateToFacing(Direction facing) {
                this.facing = facing;
            }

            @Override
            public boolean transform(MutableQuadView quad) {
                BakedQuad bakedQuad = quad.toBakedQuad(0, null, false);

                for (int v = 0; v < 4; v++) {
                    float x = Float.intBitsToFloat(bakedQuad.getVertexData()[(v * 8)]);
                    float y = Float.intBitsToFloat(bakedQuad.getVertexData()[1 + (v * 8)]);
                    float z = Float.intBitsToFloat(bakedQuad.getVertexData()[2 + (v * 8)]);

                    float newX = x;
                    float newZ = z;

                    switch (facing) {
                        case SOUTH -> {
                            newX = -(x - 0.5f) + 0.5f;
                            newZ = -(z - 0.5f) + 0.5f;
                        }
                        case WEST -> {
                            newX = z;
                            newZ = -(x - 0.5f) + 0.5f;
                        }
                        case EAST -> {
                            newX = -(z - 0.5f) + 0.5f;
                            newZ = x;
                        }
                    }

                    quad.pos(v, newX, y, newZ);
                    quad.nominalFace(quad.lightFace()); // Random bullshit go! No but seriously this fixes the lighting on south facing rotations... idk why
                }

                return true;
            }
        }
    }
}
