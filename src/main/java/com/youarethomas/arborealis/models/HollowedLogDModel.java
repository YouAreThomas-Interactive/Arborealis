package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.mixins.AxeItemAccessor;
import com.youarethomas.arborealis.models.model_utils.DynamicBakedModel;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class HollowedLogDModel extends DynamicModel {
    @Override
    public void createBlockQuads(DynamicModelBuilder builder, BlockRenderView renderView, BlockPos pos) {
        HollowedLogEntity be = (HollowedLogEntity)renderView.getBlockEntity(pos);

        // Get blocks to texture rip
        BlockState logState = be.getLogState();
        BlockState strippedState = Blocks.STRIPPED_OAK_LOG.getDefaultState();
        if (AxeItemAccessor.getStrippedBlocks().containsKey(logState.getBlock()))
            strippedState = AxeItemAccessor.getStrippedBlocks().get(logState.getBlock()).getStateWithProperties(logState);

        // Fetch the models pre-loaded in the client and retexture them
        BakedModel logOutside = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_outside"));
        builder.addBakedModel(logOutside, new DynamicModelBuilder.RetextureFromBlock(logState, true));

        BakedModel logInside = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_inside"));
        builder.addBakedModel(logInside, new DynamicModelBuilder.RetextureFromBlock(strippedState, true));

        // Render a tree core, if there's a core in the block, and the tether
        if (be.getStack(0).isOf(Arborealis.LIFE_CORE)) {
            BakedModel treeCore = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/life_core"));
            builder.addBakedModel(treeCore);

            BakedModel coreTether = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/core_tether"));
            builder.addBakedModel(coreTether, new DynamicModelBuilder.RetextureFromBlock(strippedState, true));
        }
    }

    @Override
    public void createItemQuads(DynamicModelBuilder builder, ItemStack itemStack) {
        // Not required.
    }
}
