package com.youarethomas.arborealis.models;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
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

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestModel implements UnbakedModel {
    @Override
    public Collection<Identifier> getModelDependencies() {
        return null;
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return null;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        return new Baked();
    }

    public static final class Baked implements BakedModel, FabricBakedModel {

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            // Get the hollowed log model
            BakedModelManager manager = MinecraftClient.getInstance().getBakedModelManager();
            BakedModel baseModel = BakedModelManagerHelper.getModel(manager, new Identifier(Arborealis.MOD_ID, "block/hollowed_log_oak"));

            // Get the block model
            BlockState logState = Blocks.EMERALD_BLOCK.getDefaultState();
            BakedModel woodModel = MinecraftClient.getInstance().getBlockRenderManager().getModel(logState);

            // Apply the texture from the block to the hollowed log model quads
            for (Direction dir : Direction.values()) {
                List<BakedQuad> woodQuads = woodModel.getQuads(logState, dir, randomSupplier.get());
                Sprite faceSprite = woodQuads.get(0).getSprite();
                context.pushTransform(new RetextureTransform(faceSprite, dir));

                context.popTransform();
            }

            emitQuads(blockView, pos, randomSupplier, context, state, baseModel);
        }

        public void emitQuads(BlockRenderView blockView, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context, BlockState state, BakedModel model) {
            if (model instanceof FabricBakedModel)
                ((FabricBakedModel) model).emitBlockQuads(blockView, state, pos, randomSupplier, context);
            else
                context.fallbackConsumer().accept(model);
        }

        private static class RetextureTransform implements RenderContext.QuadTransform
        {
            private final Sprite newTexture;
            private final Direction direction;

            private RetextureTransform(Sprite newTexture, Direction direction)
            {
                this.newTexture = newTexture;
                this.direction = direction;
            }

            @Override
            public boolean transform(MutableQuadView quadView)
            {
                //quadView.spriteBake(0, newTexture, MutableQuadView.BAKE_LOCK_UV).colorIndex(-1);
                quadView.nominalFace(direction).spriteBake(0, newTexture, MutableQuadView.BAKE_LOCK_UV).colorIndex(-1);
                return true;
            }
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return null;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
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
            return null;
        }

        @Override
        public ModelTransformation getTransformation() {
            return null;
        }

        @Override
        public ModelOverrideList getOverrides() {
            return null;
        }
    }
}
