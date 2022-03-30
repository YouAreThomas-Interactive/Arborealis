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
        BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
        if (be instanceof CarvedLogEntity) {
            System.out.printf("== %s ==%n", context.getWorld().isClient() ? "Client" : "Server");
            System.out.println(" Active: " + ((CarvedLogEntity) be).areRunesActive());
            System.out.println(" Catalysed: " + ((CarvedLogEntity) be).isFaceCatalysed(context.getSide()));
            System.out.println(" Glowing: " + ((CarvedLogEntity) be).isFaceEmissive(context.getSide()));
        }

        return ActionResult.PASS;
    }
}
