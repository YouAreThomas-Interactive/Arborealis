package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlantCrops extends Rune {

    boolean runeActive = false;
    final int SPEED = 20;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        runeActive = true;
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        runeActive = false;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {
        // Iterate randomly through logs
        BlockPos.iterateRandomly(Arborealis.RANDOM, SPEED, pos, be.radius).forEach(blockPos -> {
            BlockState foundState = world.getBlockState(blockPos);

            if (foundState.isOf(Blocks.FARMLAND) && world.getBlockState(blockPos.up()).isOf(Blocks.AIR)) {
                world.setBlockState(blockPos.up(), getCrop().getDefaultState());
            }
        });
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {

    }

    @Override
    public boolean showRadiusEffect() {
        return true;
    }

    private Block getCrop()
    {
        int chance = Arborealis.RANDOM.nextInt(100);

        if ((chance -= 80) < 0) return Blocks.WHEAT;
        if ((chance -= 10) < 0) return Blocks.POTATOES;
        if ((chance -= 5) < 0) return Blocks.BEETROOTS;
        return Blocks.CARROTS;

    }
}
