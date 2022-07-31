package com.youarethomas.arborealis.items.lenses;

import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.item.Item;

public abstract class LensItem extends Item {

    public LensItem(Settings settings) {
        super(settings);
    }

    public abstract ArborealisUtil.Colour getLensColor();
}
