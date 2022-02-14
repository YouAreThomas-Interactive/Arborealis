package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class WarpCoreEntity extends BlockEntity {

    public WarpCoreEntity(BlockPos pos, BlockState state) {
        super(Arborealis.WARP_CORE_ENTITY, pos, state);
    }
}
