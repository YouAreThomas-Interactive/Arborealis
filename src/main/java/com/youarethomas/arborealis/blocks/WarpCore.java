package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.block_entities.WarpCoreEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class WarpCore extends Block implements BlockEntityProvider {

    public WarpCore(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WarpCoreEntity(pos, state);
    }
}
