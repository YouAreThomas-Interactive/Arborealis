package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class SPIMMixin {
//    @Shadow private ServerWorld world;
//
//    @Inject(method = "finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V", at = @At("TAIL"))
//    public void finishMiningTree(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
//        TreeManager treeManager = TreeManager.getManager(world);
//
//        treeManager.removeBlockFromTreeStructure(pos, world);
//    }

    @Inject(method = "onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("HEAD"))
    public void onBreakTree(World world, BlockPos pos, BlockState state, PlayerEntity player , CallbackInfo ci) {
        if(TreeManager.isTreeBlock(state) && !world.isClient()) {
            ServerWorld serverWorld = (ServerWorld)world;
            TreeManager treeManager = TreeManager.getManager(serverWorld);
            treeManager.removeBlockFromTreeStructure(pos, serverWorld);
        }
    }
}
