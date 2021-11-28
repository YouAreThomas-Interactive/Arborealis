package com.youarethomas.arborealis.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;

/**
 * A tree object
 */
public class TreeStructure {
    public static final int NAT_LEAVES_MIN = 12;
    public static final int LOGS_MIN = 2;

    public HashSet<BlockPos> leaves = new HashSet<>();
    public HashSet<BlockPos> logs = new HashSet<>();
    public int logCount() {
        return logs.size();
    }

    /**
     * Returns true if a tree is deemed to be suitably natural;
     */
    public boolean isNatural() {
        return leaves.size() >= NAT_LEAVES_MIN && logCount() >= LOGS_MIN;
    }

    public boolean isEmpty() {
        return logs.isEmpty();
    }

    public void replaceLogStructure(World world) {
        for (BlockPos pos : logs) {
            //world.setBlockState(pos, replacementBlock.getDefaultState());
            world.breakBlock(pos, true);
        }
    }

    public void replaceLeafStructure(World world) {
        for (BlockPos pos : leaves) {
            //world.setBlockState(pos, replacementBlock.getDefaultState());
            world.breakBlock(pos, true);
        }
    }

    public void chopTreeStructure(World world) {
        if(isNatural()) {
            replaceLogStructure(world);
            replaceLeafStructure(world);
        }
    }

    public boolean isPosInTree(BlockPos blockPos) {
        for (BlockPos pos : logs) {
            if (pos.equals(blockPos)) {
                return true;
            }
        }
        return false;
    }
}
