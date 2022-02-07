package com.youarethomas.arborealis.misc;

import com.google.common.collect.Lists;
import com.youarethomas.arborealis.Arborealis;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;

public class StencilBagDyeRecipe extends SpecialCraftingRecipe {

    public StencilBagDyeRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        ItemStack itemStack = ItemStack.EMPTY;
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack2 = inventory.getStack(i);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.getItem() instanceof DyeableItem) {
                if (!itemStack.isEmpty()) {
                    return false;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.getItem() instanceof DyeItem) {
                list.add(itemStack2);
                continue;
            }
            return false;
        }
        return !itemStack.isEmpty() && !list.isEmpty();
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        ArrayList<DyeItem> list = Lists.newArrayList();
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack2 = inventory.getStack(i);
            if (itemStack2.isEmpty()) continue;
            Item item = itemStack2.getItem();
            if (item instanceof DyeableItem) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2.copy();
                continue;
            }
            if (item instanceof DyeItem) {
                list.add((DyeItem)item);
                continue;
            }
            return ItemStack.EMPTY;
        }
        if (itemStack.isEmpty() || list.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return DyeableItem.blendAndSetColor(itemStack, list);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Arborealis.STENCIL_BAG_DYE;
    }
}
