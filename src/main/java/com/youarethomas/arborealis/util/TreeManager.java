package com.youarethomas.arborealis.util;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Contains helper methods related to creating {@code TreeStructure} and {@code LogStructure} definition.
 * <br> May eventually contain a stored list of TreeStructures to iterate through to determine if trees stop being valid natural trees
 */
public class TreeManager {

    /**If valid, returns a tree definition with structure and leaves from a given log block.
     */
    public static TreeStructure getTreeStructureFromBlock(BlockPos startingPos, World world) {
        TreeStructure structure = new TreeStructure();
        BlockState clickedBlock = world.getBlockState(startingPos);

        // Return and empty tree structure if the starting position is not a log
        if (!(clickedBlock.isIn(BlockTags.LOGS) || clickedBlock.isIn(Arborealis.MODIFIED_LOGS))) {
            return structure;
        }

        structure.logs.addAll(getTreeLogs(world, startingPos)); // Add all found logs to the TreeStructure

        structure.leaves.addAll(getTreeLeaves(world, structure.logs)); // Add all the founded leaves to the TreeStructure

        return structure;
    }

    private static TreeSet<BlockPos> getTreeLogs(World world, BlockPos startingPos) {
        TreeSet<BlockPos> toVisit = new TreeSet<>();
        TreeSet<BlockPos> visited = new TreeSet<>();
        BlockPos currentPos = startingPos;

        int logsCounted = 0; // Cap in case something goes horribly wrong

        do {
            // 3x3x3 cube to search around
            BlockPos scanCubeStart = currentPos.down().south().west();
            BlockPos scanCubeEnd = currentPos.up().north().east();

            visited.add(currentPos); // Add the current log into visited

            for (BlockPos pos : BlockPos.iterate(scanCubeStart, scanCubeEnd)) {
                BlockState currentBlockState = world.getBlockState(pos);
                // If a log is detected that hasn't been iterated over yet, add to the list of blocks to get around to
                if ((currentBlockState.isIn(BlockTags.LOGS) || currentBlockState.isIn(Arborealis.MODIFIED_LOGS)) && !visited.contains(pos)) {
                    toVisit.add(pos.mutableCopy()); // mutableCopy() required because Java is a tool
                }
            }

            // If there are blocks to visit, get the first block and remove it from blocks to visit
            if (toVisit.size() > 0) {
                currentPos = toVisit.pollFirst();
            }

            logsCounted++;
        } while (logsCounted < 100 && !visited.contains(currentPos)); // While there are blocks left to visit, keep going
        //TODO: Add log count cap to config

        return visited;
    }

    private static TreeSet<BlockPos> getTreeLeaves(World world, HashSet<BlockPos> logSet) {
        TreeSet<BlockPos> visited = new TreeSet<>();

        //TODO: Add leaf range to config
        int range = 5; // The maximum manhattan search range for the leaves

        for(BlockPos logPos : logSet) {
            TreeSet<BlockPos> tempVisited = new TreeSet<>();
            TreeSet<BlockPos> toVisit = new TreeSet<>();

            // Add log position as a starting point.
            toVisit.add(logPos.mutableCopy());

            // Do a breadth-first search, with the number of layers determined by range.
            for(int i = 0; i < range; i++) {

                // Create a temporary set to allow altering of the toVisit within the loop.
                TreeSet<BlockPos> tempSet = new TreeSet<>(toVisit);

                for (BlockPos currentPos : tempSet) {
                    tempVisited.add(currentPos.mutableCopy());
                    toVisit.remove(currentPos);

                    // Scan in all the surrounding leaves (manhattan-style)
                    scanLeaves(world, currentPos, toVisit, tempVisited);
                }
            }

            // Doing a final sweep to check the boundary blocks
            for(BlockPos pos : toVisit) {
                if (isNaturalLeaf(world, pos)) {
                    visited.add(pos.mutableCopy()); // mutableCopy() required because Java is a tool
                }
            }

            // Remove log position as it is a log... not a leaf
            tempVisited.remove(logPos);

            // Makes a union of all the combined leaf searches.
            visited.addAll(tempVisited);
        }

        return visited;
    }

    private static void scanLeaves(World world, BlockPos currentPos, TreeSet<BlockPos> toVisit, TreeSet<BlockPos> visited) {
        // The true manhattan
        for (Direction d : Direction.values()) {
            BlockPos pos = currentPos.offset(d, 1);

            // If a leaf is detected that hasn't been iterated over yet, add to the list of blocks to get around to
            if (isNaturalLeaf(world, pos) && !visited.contains(pos)) {
                toVisit.add(pos.mutableCopy()); // mutableCopy() required because Java is a tool
            }
        }
    }

    private static boolean isNaturalLeaf(World world, BlockPos pos) {
        return world.getBlockState(pos).isIn(BlockTags.LEAVES) && !world.getBlockState(pos).get(LeavesBlock.PERSISTENT);
    }

}
