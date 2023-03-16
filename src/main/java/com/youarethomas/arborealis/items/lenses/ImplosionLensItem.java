package com.youarethomas.arborealis.items.lenses;

import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImplosionLensItem extends LensItem {

    public ImplosionLensItem(Settings settings) {
        super(settings);
    }

    @Override
    public ArborealisUtil.Colour getLensColor() {
        return new ArborealisUtil.Colour(0x255534);
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.arborealis.implosion_lens.tooltip1"));
            tooltip.add(Text.translatable("item.arborealis.implosion_lens.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.implosion_lens.tooltip3"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }
}
