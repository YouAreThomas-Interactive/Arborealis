package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PlantTrees extends Rune {

    private enum TreePosition {
        NORTH (1, 0),
        EAST (0, 1),
        SOUTH (-1, 0),
        WEST (0, -1),
        NORTH_EAST (0.6f, 0.6f),
        SOUTH_EAST (-0.6f, 0.6f),
        NORTH_WEST (-0.6f, -0.6f),
        SOUTH_WEST(0.6f, -0.6f);

        private static final List<TreePosition> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
        private final float X;
        private final float Z;

        TreePosition(float x, float y) {
            X = x;
            Z = y;
        }

        public static TreePosition getRandomPosition() {
            return VALUES.get(Arborealis.RANDOM.nextInt(VALUES.size()));
        }
    }

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

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {
        int randomCheck = Arborealis.RANDOM.nextInt(SPEED);

        if (randomCheck == 1) {
            TreePosition treePos = TreePosition.getRandomPosition();

            // Get the spot around the tree
            int treeDistance = be.radius - 4;
            BlockPos treeBasePos = new BlockPos(Vec3d.ofCenter(pos).getX() + treeDistance * treePos.X, pos.getY(), Vec3d.ofCenter(pos).getZ() + treeDistance * treePos.Z);

            // Iterate down to find ground
            Iterable<BlockPos> searchY = BlockPos.iterate(treeBasePos.offset(Direction.UP, be.radius), treeBasePos.offset(Direction.DOWN, be.radius));

            boolean foundDirt = false;
            for (BlockPos space : searchY) {
                if (world.getBlockState(space).isIn(BlockTags.DIRT)) {
                    treeBasePos = space.mutableCopy();
                    foundDirt = true;
                }
            }

            // Iterate up to check there's space for a tree
            if (foundDirt) {
                Iterable<BlockPos> spaceToGrow = BlockPos.iterate(treeBasePos.up(), treeBasePos.offset(Direction.UP, 6));
                boolean spaceFree = true;
                for (BlockPos space : spaceToGrow) {
                    if (!world.getBlockState(space).isOf(Blocks.AIR)) {
                        spaceFree = false;
                    }
                }

                if (spaceFree)
                    generateTree(world, treeBasePos.up(), be);
            }
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {
        int treeDistance = be.radius - 4;

        for (TreePosition treePos : TreePosition.values()) {
            // Particles are random but individually random to each block
            int randomCheck = Arborealis.RANDOM.nextInt(10);
            if (randomCheck == 1) {
                // Get the starting block
                BlockPos treeBasePos = new BlockPos(Vec3d.ofCenter(pos).getX() + treeDistance * treePos.X, pos.getY(), Vec3d.ofCenter(pos).getZ() + treeDistance * treePos.Z);
                Iterable<BlockPos> searchY = BlockPos.iterate(treeBasePos.offset(Direction.UP, be.radius), treeBasePos.offset(Direction.DOWN, be.radius));

                // Search for the last dirt block
                boolean foundDirt = false;
                for (BlockPos space : searchY) {
                    if (world.getBlockState(space).isIn(BlockTags.DIRT)) {
                        treeBasePos = space.mutableCopy().up();
                        foundDirt = true;
                    }
                }

                // If that block is air, create the particle effect
                if (foundDirt && world.getBlockState(treeBasePos).isOf(Blocks.AIR)) {
                    Random random = Arborealis.RANDOM;
                    world.addParticle(ParticleTypes.COMPOSTER, treeBasePos.getX() + random.nextDouble(), treeBasePos.getY() + random.nextDouble(), treeBasePos.getZ() + random.nextDouble(), -0.5 + random.nextFloat(), random.nextFloat(), -0.5 + random.nextFloat());
                }
            }
        }
    }

    public void generateTree(World world, BlockPos basePos, CarvedLogEntity be) {
        int logCount = 1 + Arborealis.RANDOM.nextInt(3);

        for (int log = 0; log <= logCount; log++) {
            world.setBlockState(basePos.offset(Direction.UP, log), be.getLogState().getBlock().getDefaultState());

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
                world.setBlockState(middleLeafPos.up(), Blocks.OAK_LEAVES.getDefaultState());
            }
        }

    }
}
