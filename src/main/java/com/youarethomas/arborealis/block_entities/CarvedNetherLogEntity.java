package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CarvedNetherLogEntity extends CarvedLogEntity {

    public CarvedNetherLogEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_NETHER_LOG_ENTITY, pos, state);
    }
}
