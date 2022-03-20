package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class InfusedSap extends Item {

    public InfusedSap(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // Sparkle sparkle damn it
    }

    // Test Method
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient) {
            ServerWorldMixinAccess serverWorld = (ServerWorldMixinAccess) context.getWorld();

            TreeManager treeManager = serverWorld.getTreeManager();
            BlockPos pos = context.getBlockPos();
            ServerWorld world = (ServerWorld) context.getWorld();

            if (treeManager.isBlockInTreeStructure(pos))
                treeManager.removeTreeStructureFromBlock(pos, world);
            else
                treeManager.addTreeStructureFromBlock(pos, world);
        }

        return ActionResult.SUCCESS;
    }
}
