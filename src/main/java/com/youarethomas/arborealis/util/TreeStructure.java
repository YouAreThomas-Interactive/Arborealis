package com.youarethomas.arborealis.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;

/**
 * A tree object
 */
public class TreeStructure {

    private HashSet<BlockPos> leaves = new HashSet<>();
    public HashSet<BlockPos> logs = new HashSet<>();
    public int logCount() {
        return logs.size();
    }

    /**
     * Returns true if a tree is deemed to be suitably natural;
     */
    public boolean isNatural() {

        // TODO: Check over leaves List for x number of naturally generated leaves

        return true;
    }

    public void replaceLogStructure(World world, Block replacementBlock) {
        for (BlockPos pos : logs) {
            world.setBlockState(pos, replacementBlock.getDefaultState());
        }
    }

    public void replaceLeafStructure(Block replacementBlock) {
        // TODO: Replace all logs and leaves in the structure with replacementBlock
    }

    public void replaceTreeStructure(Block replacementBlock) {
        // TODO: Replace all logs and leaves in the structure with replacementBlock
    }
}
