package com.youarethomas.arborealis.util;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.stream.Stream;

/**
 * Contains helper methods related to creating {@code TreeStructure} and {@code LogStructure} definition.
 * <br> May eventually contain a stored list of TreeStructures to iterate through to determine if trees stop being valid natural trees
 */
public class TreeManager {

    /**If valid, returns a tree definition with structure and leaves from a given log block.
     */
    public static TreeStructure getTreeStructureFromBlock(BlockPos blockPos) {
        TreeStructure structure = new TreeStructure();

        // TODO: Step 1 - Get the log structure
        // use BlockPos.steam(BlockBox) - Chebyshev on below, middle and above

        // TODO: Step 2 - Get the surrounding leaves x blocks away from log structure
        // use BlockPos.iterateOutwards() - Manhattan

        // TODO: Step 3 - Remove leaves associated with other trees
        // use BlockPos.iterateOutwards() - Manhattan, but one further than step 2
        // if a log block is found that ISN'T in our log set, then

        return structure;
    }

}
