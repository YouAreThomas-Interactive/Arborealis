package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Load extends Rune {


    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        // Add rune to force loaded chunks
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        // Remove from unloaded chunks
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {
        // Draw chunk outline


    }
}
