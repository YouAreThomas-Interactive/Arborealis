package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.TimerTask;

public class Chop extends AbstractRune{

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedWoodEntity be) {
        be.chopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TreeStructure tree = TreeManager.getTreeStructureFromBlock(pos, world);
                if (tree.isNatural()) {
                    tree.chopTreeStructure(world);
                }
            }
        }, 3 * 1000);
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedWoodEntity be) {
        be.chopTimer.cancel();
    }
}
