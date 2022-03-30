package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Harvest extends Rune {

    boolean runeActive = false;
    final int SPEED = 10;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        runeActive = true;

        // Call a tick immediately when the rune is carved
        onServerTick(world, pos, be);
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

            if (foundState.isIn(BlockTags.CROPS)) {
                if (foundState.isOf(Blocks.BEETROOTS)) {
                    if (foundState.get(BeetrootsBlock.AGE) == BeetrootsBlock.field_31013) {
                        world.breakBlock(blockPos, true);
                    }
                } else {
                    if (foundState.get(CropBlock.AGE) == CropBlock.MAX_AGE) {
                        world.breakBlock(blockPos, true);
                    }
                }

            } else if (foundState.isIn(BlockTags.REPLACEABLE_PLANTS) ||  foundState.isIn(BlockTags.FLOWERS)) {
                world.breakBlock(blockPos, true);
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
}
