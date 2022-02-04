package com.youarethomas.arborealis.models.model_utils;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.MinecraftClient;
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
import org.lwjgl.system.CallbackI;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class DynamicModel implements UnbakedModel {
    private static final ThreadLocal<MeshBuilder> MESH_BUILDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().meshBuilder());

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return Collections.emptyList();
    }

    public abstract void createBlockQuads(CuboidBuilder builder, BlockRenderView renderView, BlockPos pos);

    public abstract void createItemQuads(CuboidBuilder builder, ItemStack itemStack);

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return new BakedDynamicModel();
    }

    public class BakedDynamicModel implements BakedModel, FabricBakedModel {

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            CuboidBuilder blockBuilder = new CuboidBuilder();
            createBlockQuads(blockBuilder, blockView, pos);

            loadModel(blockBuilder, context);
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            CuboidBuilder itemBuilder = new CuboidBuilder();
            createItemQuads(itemBuilder, stack);

            loadModel(itemBuilder, context);
        }

        private void loadModel(CuboidBuilder builder, RenderContext context) {

            for (var model : builder.models.get().entrySet()) {
                if (model.getValue() != null) {
                    context.pushTransform(model.getValue());
                    context.fallbackConsumer().accept(model.getKey());
                    context.popTransform();
                } else {
                    context.fallbackConsumer().accept(model.getKey());
                }
            }

            for (DynamicCuboid cuboid : builder.cuboids.get()) {
                cuboid.create(MESH_BUILDER.get().getEmitter());
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
            return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "invisible")).getSprite();
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

    protected class CuboidBuilder {
        ThreadLocal<Collection<DynamicCuboid>> cuboids = ThreadLocal.withInitial(ArrayList::new);
        ThreadLocal<Map<BakedModel, RenderContext.QuadTransform>> models = ThreadLocal.withInitial(HashMap::new);

        public BakedModel getModel(Identifier identifier) {
            return BakedModelManagerHelper.getModel(MinecraftClient.getInstance().getBakedModelManager(), identifier);
        }

        public void addCuboid(DynamicCuboid cuboid) {
            cuboids.get().add(cuboid);
        }

        public void addBakedModel(BakedModel model) {
            models.get().put(model, null);
        }

        public void addBakedModel(BakedModel model, RenderContext.QuadTransform transform) {
            models.get().put(model, transform);
        }

        public static class RetextureFromBlock implements RenderContext.QuadTransform
        {
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
                    if (quads.size() > 0) {
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
    }
}
