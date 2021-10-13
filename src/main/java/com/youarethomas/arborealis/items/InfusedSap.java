package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        TreeManager.getTreeStructureFromBlock(context.getBlockPos());

        return ActionResult.PASS;
    }
}
