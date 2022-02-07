package com.youarethomas.arborealis.items.tool_materials;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class CopperKnifeMaterial implements ToolMaterial {

    public static final CopperKnifeMaterial INSTANCE = new CopperKnifeMaterial();

    @Override
    public int getDurability() {
        return 50;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0;
    }

    @Override
    public float getAttackDamage() {
        return 0;
    }

    @Override
    public int getMiningLevel() {
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.COPPER_INGOT);
    }
}
