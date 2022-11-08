package com.youarethomas.arborealis.items.tool_materials;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class TuningForkMaterial implements ToolMaterial {

    public static final TuningForkMaterial INSTANCE = new TuningForkMaterial();

    @Override
    public int getDurability() {
        return 100;
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
        return Ingredient.empty();
    }
}
