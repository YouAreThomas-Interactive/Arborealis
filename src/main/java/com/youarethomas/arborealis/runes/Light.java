package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.blocks.CarvedLog;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Light extends Rune {

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        world.setBlockState(pos, world.getBlockState(pos).with(CarvedLog.LIT, true));
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        world.setBlockState(pos, world.getBlockState(pos).with(CarvedLog.LIT, false));
    }
}
