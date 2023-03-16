package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.items.lenses.LensItem;
import com.youarethomas.arborealis.models.model_utils.DynamicCuboid;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class ProjectorDModel extends DynamicModel {
    @Override
    public void createBlockQuads(CuboidBuilder builder, BlockRenderView renderView, BlockPos pos) {
        Direction facing = renderView.getBlockState(pos).get(HorizontalFacingBlock.FACING);

        BakedModel projectorBase = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/projector/projector_base"));
        builder.addBakedModel(projectorBase);

        ProjectorBlockEntity pbe = (ProjectorBlockEntity) renderView.getBlockEntity(pos);
        ItemStack itemStack = pbe.getStack(0);

        if (itemStack.isOf(Arborealis.CARVED_STENCIL)) {
            NbtCompound nbt = itemStack.getNbt();
            if (nbt != null && nbt.contains("pattern")) {
                int[] pattern = nbt.getIntArray("pattern");

                int segmentCount = 0;
                for (int y = 13; y >= 1; y -= 2) {
                    for (int x = 1; x <= 13; x += 2) {
                        int carvingSegment = pattern[segmentCount];

                        if (carvingSegment == 0) {
                            DynamicCuboid cuboid = new DynamicCuboid(x, y, 1, 2, 2, 1);
                            cuboid.applyTexturesFromBlock(Blocks.STRIPPED_OAK_LOG.getDefaultState());

                            builder.addCuboid(cuboid, new CuboidBuilder.RotateToFacing(facing));
                        }

                        segmentCount++;
                    }
                }
            }
        } else if (itemStack.getItem() instanceof LensItem) {
            // Loads lens model by name
            String[] lensPieces = itemStack.getItem().getTranslationKey().split("\\.");
            if (lensPieces.length > 0) {
                BakedModel lensModel = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/" + lensPieces[lensPieces.length - 1]));
                builder.addBakedModel(lensModel);
            }
        }
    }

    @Override
    public void createItemQuads(CuboidBuilder builder, ItemStack itemStack) {
        BakedModel projectorBase = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/projector/projector_base"));
        builder.addBakedModel(projectorBase);
    }

    @Override
    public SpriteIdentifier getParticleSpriteId() {
        return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "block/projector_base"));
    }
}
