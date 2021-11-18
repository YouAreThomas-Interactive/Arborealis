package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.blocks.CarvedWood;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Light extends AbstractRune {

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedWoodEntity be) {
        world.setBlockState(pos, world.getBlockState(pos).with(CarvedWood.LIT, true));
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedWoodEntity be) {
        world.setBlockState(pos, world.getBlockState(pos).with(CarvedWood.LIT, false));
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedWoodEntity be) {

    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedWoodEntity be) {

    }
}
