package com.youarethomas.arborealis.models;

import com.google.common.collect.Iterables;
import com.ibm.icu.impl.locale.XCldrStub;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
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

public class DynamicModel implements UnbakedModel {

    private Collection<DynamicCuboid> cuboids = new ArrayList<>();

    private Mesh mesh;
    private QuadEmitter emitter;

    private SpriteIdentifier breakTextureIdentifier;
    private Sprite breakTextureSprite;

    public DynamicModel() {
        //loadModel();
    }

    public void loadModel() {}

    public void addCuboid(DynamicCuboid cuboid) {
        cuboids.add(cuboid);
    }

    public void setBreakTexture(SpriteIdentifier spriteIdentifier) {
        breakTextureIdentifier = spriteIdentifier;
    }

    public Collection<SpriteIdentifier> getTextures() {

        Stream<SpriteIdentifier> spriteIDs = Stream.<SpriteIdentifier>builder().build();

        for (DynamicCuboid cuboid : cuboids) {
            spriteIDs = Stream.concat(spriteIDs, cuboid.spriteIds.values().stream());
        }

        return spriteIDs.toList();
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        // TODO: Bring in a JSON model or existing model as a template
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
        MeshBuilder builder = renderer.meshBuilder();
        emitter = builder.getEmitter();

        for (DynamicCuboid cuboid : cuboids) {
            cuboid.create(emitter, textureGetter);
        }

        mesh = builder.build();

        return new ModelBaseBaked();
    }



    public class ModelBaseBaked implements BakedModel, FabricBakedModel {

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            context.meshConsumer().accept(mesh);
        }

        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

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
