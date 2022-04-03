package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.TimerTask;

public class Chop extends Rune {

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        TreeManager treeManager = ((ServerWorldMixinAccess)world).getTreeManager();
        TreeStructure tree = treeManager.getTreeStructureFromBlock(pos, world);

        be.chopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (tree.isNatural()) {
                    tree.chopTreeStructure(world);
                }
            }
        }, 3 * 1000);
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        be.chopTimer.cancel();
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {

    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {

    }
}
