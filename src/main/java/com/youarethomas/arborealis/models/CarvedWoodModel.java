package com.youarethomas.arborealis.models;

import com.mojang.datafixers.util.Pair;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
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
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CarvedWoodModel implements UnbakedModel {

    public static final CarvedWoodModel INSTANCE = new CarvedWoodModel();

    private Collection<DynamicCuboid> fixedCuboids = new ArrayList<>();

    private MeshBuilder builder;
    private QuadEmitter emitter;
    private Function<SpriteIdentifier, Sprite> textureGetter;

    private Mesh mesh;

    private SpriteIdentifier breakTextureIdentifier;
    private Sprite breakTextureSprite;

    public CarvedWoodModel() {
        setBreakTexture(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("minecraft:block/oak_log")));
    }

    public void loadFixedCuboids(String logID) {
        String[] idParts = logID.split(":");

        String log = idParts[0] + ":block/" + idParts[1];
        String strippedLog = idParts[0] + ":block/stripped_" + idParts[1];
        String logTop = idParts[0] + ":block/" + idParts[1] + "_top";

        DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
        core.applyTextureToAllSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(strippedLog)));
        addFixedCuboid(core);

        DynamicCuboid top = new DynamicCuboid(0, 15, 0, 16, 1, 16);
        top.applyTextureToAllSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
        top.applyTexture(Direction.UP, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
        addFixedCuboid(top);

        DynamicCuboid bottom = new DynamicCuboid(0, 0, 0, 16, 1, 16);
        bottom.applyTextureToAllSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
        bottom.applyTexture(Direction.DOWN, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
        addFixedCuboid(bottom);
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

                String logID = ((CarvedWoodEntity) entity).getLogID();

                if (logID.contains("minecraft")) {
                    System.out.println("ID Found!");
                    loadFixedCuboids(logID);
                } else {
                    loadFixedCuboids("minecraft:oak_log");
                }

                for (DynamicCuboid cuboid : fixedCuboids) {
                    cuboid.create(emitter, textureGetter);
                }

                mesh = builder.build();

                context.meshConsumer().accept(mesh);
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

    public static enum VariantProvider implements ModelVariantProvider {
        INSTANCE;

        @Override
        public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {

            if (modelId.getNamespace().equals(Arborealis.MOD_ID) && modelId.getPath().equals("carved_wood_model")) {
                return CarvedWoodModel.INSTANCE;
            } else {
                return null;
            }
        }
    }
}
