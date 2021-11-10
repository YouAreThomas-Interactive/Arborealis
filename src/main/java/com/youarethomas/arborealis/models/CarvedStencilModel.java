package com.youarethomas.arborealis.models;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CarvedStencilModel implements UnbakedModel {

    private MeshBuilder builder;
    private QuadEmitter emitter;
    private Function<SpriteIdentifier, Sprite> textureGetter;

    private final SpriteIdentifier STENCIL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "item/stencil_carved"));

    @Override
    public Collection<Identifier> getModelDependencies() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return ImmutableSet.of(STENCIL_TEXTURE);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        builder = renderer.meshBuilder();
        emitter = builder.getEmitter();
        this.textureGetter = textureGetter;

        return new Baked();
    }

    @Environment(EnvType.CLIENT)
    public class Baked implements BakedModel, FabricBakedModel {

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            // no.
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(Arborealis.BLANK_STENCIL);

            final float PXL_SIZE = 0.0625f;

            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("pattern")) {
                int[] pattern = nbt.getIntArray("pattern");

                for (int y = 1; y < 8; y++) {
                    for (int x = 1; x < 8; x++) {
                        int carveState = pattern[(x - 1) + ((y - 1) * 7)];

                        if (carveState != 0) {
                            emitter.square(Direction.SOUTH, (x + 3) * PXL_SIZE, 1F - (y + 4) * PXL_SIZE, (x + 4) * PXL_SIZE, 1F - (y + 3) * PXL_SIZE, 7.4F * PXL_SIZE);
                            emitter.spriteBake(0, textureGetter.apply(STENCIL_TEXTURE), MutableQuadView.BAKE_LOCK_UV);
                            emitter.spriteColor(0, -1, -1, -1, -1);
                            emitter.emit();
                        }
                    }
                }
            }

            context.meshConsumer().accept(builder.build());
            context.fallbackConsumer().accept(model);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return null;
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
            return STENCIL_TEXTURE.getSprite();
        }

        @Override
        public ModelTransformation getTransformation() {
            Transformation tpLeft = new Transformation(new Vec3f(0, 0, 0), new Vec3f(0, 3F/16F, 1F/16F), new Vec3f(0.55F, 0.55F, 0.55F));
            Transformation tpRight = new Transformation(new Vec3f(0, 0, 0), new Vec3f(0, 3F/16F, 1F/16F), new Vec3f(0.55F, 0.55F, 0.55F));

            Transformation fpLeft = new Transformation(new Vec3f(0, -90, 25), new Vec3f(1.13F/16F, 3.2F/16F, 1.13F/16F), new Vec3f(0.68F, 0.68F, 0.68F));
            Transformation fpRight = new Transformation(new Vec3f(0, -90, 25), new Vec3f(1.13F/16F, 3.2F/16F, 1.13F/16F), new Vec3f(0.68F, 0.68F, 0.68F));

            Transformation head = new Transformation(new Vec3f(0, 180, 0), new Vec3f(0, 13/16F, 7/16F), new Vec3f(1, 1, 1));
            Transformation gui = new Transformation(new Vec3f(0, 0, 0), new Vec3f(0, 0, 0), new Vec3f(1, 1, 1));
            Transformation ground = new Transformation(new Vec3f(0, 0, 0), new Vec3f(0, 2/16F, 0), new Vec3f(0.5F, 0.5F, 0.5F));
            Transformation fixed = new Transformation(new Vec3f(0, 180, 0), new Vec3f(0, 0, 0), new Vec3f(1, 1, 1));

            return new ModelTransformation(tpLeft, tpRight, fpLeft, fpRight, head, gui, ground, fixed);
        }

        @Override
        public ModelOverrideList getOverrides() {
            return ModelOverrideList.EMPTY;
        }
    }
}
