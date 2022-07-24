package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfusionLensItem extends Item {

    public InfusionLensItem(Settings settings) {
        super(settings);
    }

    public ArborealisUtil.Colour getLensColor() {
        return new ArborealisUtil.Colour(0x9EFAFF);
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.arborealis.infusion_lens.tooltip1"));
            tooltip.add(Text.translatable("item.arborealis.infusion_lens.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.infusion_lens.tooltip3"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }
}
