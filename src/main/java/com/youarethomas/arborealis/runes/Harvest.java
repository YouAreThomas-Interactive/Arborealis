package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Harvest extends Rune {
    final int SPEED = 10;

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {
        // Iterate randomly through logs
        BlockPos.iterateRandomly(Arborealis.RANDOM, SPEED, pos, be.radius).forEach(blockPos -> {
            BlockState foundState = world.getBlockState(blockPos);

            if (foundState.isIn(BlockTags.CROPS)) {
                if (foundState.isOf(Blocks.BEETROOTS)) {
                    if (foundState.get(BeetrootsBlock.AGE) == BeetrootsBlock.BEETROOTS_MAX_AGE) {
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
    public boolean showRadiusEffect() {
        return true;
    }
}
