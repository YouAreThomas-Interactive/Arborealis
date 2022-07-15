package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.mixins.AxeItemAccessor;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.Objects;

public class HollowedLogDModel extends DynamicModel {
    @Override
    public void createBlockQuads(CuboidBuilder builder, BlockRenderView renderView, BlockPos pos) {
        HollowedLogEntity be = (HollowedLogEntity)renderView.getBlockEntity(pos);

        // Get blocks to texture rip
        BlockState logState = be.getLogState();
        BlockState strippedState = Blocks.STRIPPED_OAK_LOG.getDefaultState();
        if (AxeItemAccessor.getStrippedBlocks().containsKey(logState.getBlock()))
            strippedState = AxeItemAccessor.getStrippedBlocks().get(logState.getBlock()).getStateWithProperties(logState);

        // Fetch the models pre-loaded in the client and retexture them
        BakedModel logOutside = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_outside"));
        builder.addBakedModel(logOutside, new CuboidBuilder.RetextureFromBlock(logState, true));

        BakedModel logInside = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/hollowed_log_inside"));
        builder.addBakedModel(logInside, new CuboidBuilder.RetextureFromBlock(strippedState, true));

        // Render a tree core, if there's a core in the block, and the tether
        if (be.getStack(0).isOf(Arborealis.TREE_CORE)) {
            BakedModel treeCore = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/tree_core"));
            builder.addBakedModel(treeCore);

            BakedModel coreTether = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/hollowed_log/core_tether"));
            builder.addBakedModel(coreTether, new CuboidBuilder.RetextureFromBlock(strippedState, true));
        }
    }

    @Override
    public void createItemQuads(CuboidBuilder builder, ItemStack itemStack) {
        // Not required.
    }
}
