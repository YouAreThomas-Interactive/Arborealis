package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;

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
            TreeStructure structure = TreeManager.getTreeStructureFromBlock(context.getBlockPos(), context.getWorld());
            structure.chopTreeStructure(context.getWorld());
            /*structure.replaceLogStructure(context.getWorld(), Blocks.BONE_BLOCK);
            structure.replaceLeafStructure(context.getWorld(), Blocks.GLASS);*/
        }

        return ActionResult.PASS;
    }
}
