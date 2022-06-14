package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class SPIMMixin {
    @Shadow private ServerWorld world;

    @Inject(method = "finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V", at = @At("TAIL"))
    public void finishMiningTree(BlockPos pos, int sequence, String reason, CallbackInfo ci) {
        TreeManager treeManager = TreeManager.getManager(world);

        treeManager.removeBlockFromTreeStructure(pos, world);
    }
}
