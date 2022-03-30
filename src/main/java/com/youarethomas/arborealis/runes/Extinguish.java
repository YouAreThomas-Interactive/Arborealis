package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Extinguish extends Rune {

    boolean runeActive = false;
    final int SPEED = 500;

    @Override
    public boolean showRadiusEffect() {
        return true;
    }

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

            if (foundState.isOf(Blocks.FIRE) || foundState.isOf(Blocks.SOUL_FIRE)) {
                world.removeBlock(blockPos, false);
            }
        });
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {

    }
}
