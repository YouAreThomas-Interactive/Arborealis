package com.youarethomas.arborealis.util;

import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

        //region Logs
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
                // If a log is detected that hasn't been iterated over yet, add to the list of blocks to get around to
                if (world.getBlockState(pos).isIn(BlockTags.LOGS) && !visited.contains(pos)) {
                    toVisit.add(pos.mutableCopy()); // mutableCopy() required because Java is a tool
                }
            }

            // If there are blocks to visit, get the first block and remove it from blocks to visit
            if (toVisit.size() > 0) {
                currentPos = toVisit.pollFirst();
            }

            logsCounted++;
        } while (logsCounted < 50 && !visited.contains(currentPos)); // While there are blocks left to visit, keep going

        structure.logs.addAll(visited); // Add all found logs to the TreeStructure
        System.out.println(visited.size());
        //endregion

        // TODO: Step 2 - Get the surrounding leaves x blocks away from log structure
        // use BlockPos.iterateOutwards() - Manhattan

        // TODO: Step 3 - Remove leaves associated with other trees
        // use BlockPos.iterateOutwards() - Manhattan, but one further than step 2
        // if a log block is found that ISN'T in our log set, then

        return structure;
    }

}
