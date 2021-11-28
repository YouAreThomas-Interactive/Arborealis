package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AreaHarvest extends AbstractRune{

    boolean harvestSearch = false;
    final int SPEED = 2;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedWoodEntity be) {
        harvestSearch = true;

        BlockPos.iterateOutwards(pos, be.radius, be.radius, be.radius).forEach(blockPos -> {
            BlockState foundState = world.getBlockState(blockPos);

            // TODO: Replace with REPLACEABLE_PLANTS in 1.18
            if (foundState.isIn(BlockTags.FLOWERS) || foundState.isOf(Blocks.GRASS) || foundState.isOf(Blocks.TALL_GRASS)) {
                world.breakBlock(blockPos, true);
            }
        });
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedWoodEntity be) {
        harvestSearch = false;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedWoodEntity be) {
        // Iterate randomly through logs
        BlockPos.iterateRandomly(Arborealis.RANDOM, SPEED, pos, be.radius).forEach(blockPos -> {
            BlockState foundState = world.getBlockState(blockPos);

            if (foundState.isIn(BlockTags.CROPS)) {
                if (foundState.get(CropBlock.AGE) == CropBlock.MAX_AGE) {
                    world.breakBlock(blockPos, true);
                }
            } else if (foundState.isIn(BlockTags.FLOWERS) || foundState.isOf(Blocks.GRASS) || foundState.isOf(Blocks.TALL_GRASS)) {
                world.breakBlock(blockPos, true);
            }
        });
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedWoodEntity be) {

    }

    @Override
    public boolean showRadiusEffect() {
        return true;
    }
}
