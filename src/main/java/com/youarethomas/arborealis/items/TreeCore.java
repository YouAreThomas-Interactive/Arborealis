package com.youarethomas.arborealis.items;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreeCore extends Item {

    public TreeCore(Settings settings) {
        super(settings);
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.arborealis.tree_core.tooltip1"));
            tooltip.add(Text.translatable("item.arborealis.tree_core.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.tree_core.tooltip3"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }
}
