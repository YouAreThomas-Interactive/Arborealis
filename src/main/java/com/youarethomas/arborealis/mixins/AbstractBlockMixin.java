package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "onStateReplaced(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", at = @At("HEAD"))
    public void onStateReplacedTree(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if(!Registry.BLOCK.getId(state.getBlock()).equals(Registry.BLOCK.getId(newState.getBlock())) && TreeManager.isTreeBlock(state) && !world.isClient()) {
            ServerWorld serverWorld = (ServerWorld)world;
            TreeManager treeManager = ((ServerWorldMixinAccess)serverWorld).getTreeManager();
            treeManager.removeBlockFromTreeStructure(state, pos, serverWorld);
        }
    }
}
