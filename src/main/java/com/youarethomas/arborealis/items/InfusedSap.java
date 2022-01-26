package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.entity.BlockEntity;
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
            BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
            if (be instanceof CarvedLogEntity) {
                System.out.println(((CarvedLogEntity) be).getRunesActive());
            }
        }

        return ActionResult.PASS;
    }
}
