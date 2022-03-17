package com.youarethomas.arborealis.mixins;

import net.minecraft.block.Block;
import net.minecraft.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {
    @Accessor("STRIPPED_BLOCKS")
    static Map<Block, Block> getStrippedBlocks() {
        throw new AssertionError();
    }

    @Accessor("STRIPPED_BLOCKS")
    static void setStrippedBlocks(Map<Block, Block> blocks) {
        throw new AssertionError();
    }
}
