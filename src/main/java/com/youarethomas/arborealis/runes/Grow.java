package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Grow extends AbstractRune{

    boolean runeActive = false;
    final int SPEED = 100;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        runeActive = true;
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        runeActive = false;
    }

    public boolean showRadiusEffect() {
        return true;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {
        if (runeActive) {
            // Iterate randomly through logs
            BlockPos.iterateRandomly(Arborealis.RANDOM, SPEED, pos, be.radius).forEach(blockPos -> {
                BlockState foundState = world.getBlockState(blockPos);

                if (foundState.hasRandomTicks()) {
                    foundState.randomTick((ServerWorld) world, blockPos, Arborealis.RANDOM);
                }
            });
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {

    }
}
