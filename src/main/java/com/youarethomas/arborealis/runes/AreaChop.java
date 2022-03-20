package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AreaChop extends AbstractRune{

    boolean runeActive = false;
    final int SPEED = 2;

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
        TreeManager treeManager = ((ServerWorldMixinAccess)world).getTreeManager();

        // Iterate randomly through logs
        BlockPos.iterateRandomly(Arborealis.RANDOM, SPEED, pos, be.radius).forEach(blockPos -> {
            // If the block found is a log
            if (world.getBlockState(blockPos).isIn(BlockTags.LOGS) || world.getBlockState(blockPos).isIn(Arborealis.MODIFIED_LOGS)) {
                TreeStructure homeTree = treeManager.getTreeStructureFromBlock(pos, world);

                // if the found block is in the home tree
                if (!homeTree.isPosInTree(blockPos)) {
                    TreeStructure foundTree = treeManager.getTreeStructureFromBlock(blockPos, world);

                    // if the tree is mini natural, kill it >:)
                    if (foundTree.isNatural()) {
                        foundTree.chopTreeStructure(world);
                    }
                }

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
