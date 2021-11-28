package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class PlantTrees extends AbstractRune{

    boolean runeActive = false;
    final int SPEED = 10;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedWoodEntity be) {
        runeActive = true;
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedWoodEntity be) {
        runeActive = false;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedWoodEntity be) {
        int randomCheck = Arborealis.RANDOM.nextInt(SPEED);
        if (randomCheck == 1) {
            BlockPos.iterateRandomly(Arborealis.RANDOM, 1, pos, be.radius - 1).forEach(blockPos -> {
                if (!blockPos.isWithinDistance(pos, 4)) {
                    if (world.getBlockState(blockPos.down()).isIn(BlockTags.DIRT)) {
                        Iterable<BlockPos> spacesToCheck = BlockPos.iterate(blockPos, blockPos.offset(Direction.UP, 6));

                        boolean spaceFree = true;
                        for (BlockPos space : spacesToCheck) {
                            if (!world.getBlockState(space).isOf(Blocks.AIR)) {
                                spaceFree = false;
                            }
                        }

                        if (spaceFree) {
                            generateTree(world, blockPos, be);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedWoodEntity be) {

    }

    @Override
    public boolean showRadiusEffect() {
        return true;
    }

    public void generateTree(World world, BlockPos basePos, CarvedWoodEntity be) {
        int logCount = 1 + Arborealis.RANDOM.nextInt(3);

        for (int log = 0; log <= logCount; log++) {
            world.setBlockState(basePos.offset(Direction.UP, log), Registry.BLOCK.get(new Identifier(be.getLogID())).getDefaultState());

            if (log == logCount) {
                BlockPos topLogPos = basePos.offset(Direction.UP, log);

                for (int north = -1; north <= 1; north++) {
                    for (int east = -1; east <= 1; east++) {
                        if (!(north == 0 && east == 0))
                        world.setBlockState(topLogPos.offset(Direction.NORTH, north).offset(Direction.EAST, east), Blocks.OAK_LEAVES.getDefaultState());
                    }
                }

                BlockPos middleLeafPos = topLogPos.up();


                world.setBlockState(middleLeafPos, Blocks.OAK_LEAVES.getDefaultState());
                world.setBlockState(middleLeafPos.north(), Blocks.OAK_LEAVES.getDefaultState());
                world.setBlockState(middleLeafPos.south(), Blocks.OAK_LEAVES.getDefaultState());
                world.setBlockState(middleLeafPos.east(), Blocks.OAK_LEAVES.getDefaultState());
                world.setBlockState(middleLeafPos.west(), Blocks.OAK_LEAVES.getDefaultState());
                world.setBlockState(middleLeafPos.up(), Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, false));
            }
        }

    }
}
