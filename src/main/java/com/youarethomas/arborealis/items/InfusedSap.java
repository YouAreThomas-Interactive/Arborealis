package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.block_entities.PrismBlockEntity;
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
        /*if (!context.getWorld().isClient) {
            ServerWorldMixinAccess serverWorld = (ServerWorldMixinAccess) context.getWorld();

            TreeManager treeManager = serverWorld.getTreeManager();
            BlockPos pos = context.getBlockPos();
            ServerWorld world = (ServerWorld) context.getWorld();

            if (treeManager.isBlockInTreeStructure(pos))
                treeManager.deconstructTreeStructureFromBlock(pos, world);
            else
                treeManager.constructTreeStructureFromBlock(pos, world);
        }*/

        BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
        System.out.printf("== %s ==%n", context.getWorld().isClient() ? "Client" : "Server");
        if (be instanceof CarvedLogEntity carvedLogEntity) {
            System.out.println(" Runes found: " + carvedLogEntity.getRunesPresentLastCheck());
            System.out.println(" Active: " + carvedLogEntity.areRunesActive());
            System.out.println(" Catalysed: " + carvedLogEntity.isFaceCatalysed(context.getSide()));
            System.out.println(" Glowing: " + carvedLogEntity.isFaceEmissive(context.getSide()));
            System.out.println(" Show radius: " + carvedLogEntity.getShowRadius());

            return ActionResult.SUCCESS;
        } else if (be instanceof PrismBlockEntity prismBlockEntity) {
            System.out.println("Face: " + context.getSide());
            System.out.println("State: " + prismBlockEntity.getFaceClosed(context.getSide()));

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
