package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CarvedWood extends Block implements BlockEntityProvider {

    public CarvedWood(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CarvedWoodEntity(pos, state);
    }


}
