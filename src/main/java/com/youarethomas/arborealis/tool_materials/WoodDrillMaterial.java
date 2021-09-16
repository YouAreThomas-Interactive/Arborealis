package com.youarethomas.arborealis.tool_materials;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class WoodDrillMaterial implements ToolMaterial {

    public static final WoodDrillMaterial INSTANCE = new WoodDrillMaterial();

    @Override
    public int getDurability() {
        return 25;
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
        return Ingredient.ofItems(Items.AMETHYST_SHARD);
    }
}
