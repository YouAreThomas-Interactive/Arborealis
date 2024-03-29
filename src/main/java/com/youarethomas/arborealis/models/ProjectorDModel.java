package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.models.model_utils.DynamicBakedModel;
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
    public void createBlockQuads(DynamicModelBuilder builder, BlockRenderView renderView, BlockPos pos) {
        Direction facing = renderView.getBlockState(pos).get(HorizontalFacingBlock.FACING);

        BakedModel projectorBase = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/projector/projector_base"));
        builder.addBakedModel(projectorBase);

        ProjectorBlockEntity pbe = (ProjectorBlockEntity) renderView.getBlockEntity(pos);
        ItemStack itemStack = pbe.getStack(0);

        if (itemStack.isOf(Arborealis.CARVED_STENCIL)) {
            NbtCompound nbt = itemStack.getNbt();
            if (nbt != null && nbt.contains("pattern")) {
                int[] pattern = nbt.getIntArray("pattern");

                // TODO: currently doesn't render out borders - waiting for new projector model
                int segmentCount = 0;
                for (int y = 9; y >= 1; y -= 2) {
                    for (int x = 1; x <= 9; x += 2) {
                        int carvingSegment = pattern[segmentCount];

                        if (carvingSegment == 0) {
                            DynamicCuboid cuboid = new DynamicCuboid(x + 2, y + 2, 1, 2, 2, 1);
                            cuboid.applyTexturesFromBlock(Blocks.STRIPPED_OAK_LOG.getDefaultState());

                            builder.addCuboid(cuboid, new DynamicModelBuilder.RotateToFacing(facing));
                        }

                        segmentCount++;
                    }
                }
            }
        } else if (itemStack.isOf(Arborealis.INFUSION_LENS)) {
            BakedModel lensModel = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/infusion_lens"));
            builder.addBakedModel(lensModel);
        } else if (itemStack.isOf(Arborealis.IMPLOSION_LENS)) {
            BakedModel lensModel = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/implosion_lens"));
            builder.addBakedModel(lensModel);
        }
    }

    @Override
    public void createItemQuads(DynamicModelBuilder builder, ItemStack itemStack) {
        BakedModel projectorBase = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/projector/projector_base"));
        builder.addBakedModel(projectorBase);
    }

    @Override
    public SpriteIdentifier getParticleSpriteId() {
        return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "block/projector_base"));
    }
}
