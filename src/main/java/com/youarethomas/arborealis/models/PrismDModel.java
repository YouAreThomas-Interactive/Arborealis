package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.PrismBlockEntity;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class PrismDModel extends DynamicModel {
    @Override
    public void createBlockQuads(DynamicModelBuilder builder, BlockRenderView renderView, BlockPos pos) {
        BakedModel prismBase = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/prism/prism_base"));
        builder.addBakedModel(prismBase);

        PrismBlockEntity prismBlockEntity = (PrismBlockEntity) renderView.getBlockEntity(pos);

        if (prismBlockEntity != null) {
            for (Direction dir : Direction.values()) {
                if (prismBlockEntity.getFaceClosed(dir)) {
                    BakedModel prismCover = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/prism/prism_cover"));
                    builder.addBakedModel(prismCover, new DynamicModelBuilder.RotateToFacing(dir));
                }
            }
        }
    }

    @Override
    public void createItemQuads(DynamicModelBuilder builder, ItemStack itemStack) {

    }
}
