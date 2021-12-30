package com.youarethomas.arborealis.mixins;

import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Blocks.class)
public interface CreateLeavesBlockInvoker {

    @Invoker("createLeavesBlock")
    static LeavesBlock createLeavesBlock(BlockSoundGroup soundGroup) {
        throw new AssertionError();
    }
}
