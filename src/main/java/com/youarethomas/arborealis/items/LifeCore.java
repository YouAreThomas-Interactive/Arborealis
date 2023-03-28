package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.ArborealisNbt;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LifeCore extends Item {

    public LifeCore(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains("rune_list");
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.arborealis.life_core.tooltip1"));
            tooltip.add(Text.translatable("item.arborealis.life_core.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.life_core.tooltip3"));

            // Get rune list if available
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("rune_list")) {
                tooltip.add(Text.of("")); // Add a blank line

                List<Rune> runeList = ArborealisNbt.deserializeRuneList(nbt.getList("rune_list", NbtList.COMPOUND_TYPE));
                for (Rune rune : runeList)
                    tooltip.add(Text.translatable("rune.arborealis." + rune.name));
            }
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }
}
