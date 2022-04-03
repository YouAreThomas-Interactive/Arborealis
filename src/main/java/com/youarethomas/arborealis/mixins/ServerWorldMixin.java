package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeManagerRenderer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.RaidManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements ServerWorldMixinAccess {
    private TreeManager treeManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void treeManagerInit(CallbackInfo ci) {
        // Create a new tree manager
        ServerWorld thisWorld = (ServerWorld)(Object)this;
        this.treeManager = TreeManager.getManager(thisWorld);
        TreeManagerRenderer.setBlockPositions(thisWorld.getRegistryKey(), treeManager.getStructureBlocks());
        System.out.println("World: " + thisWorld.getRegistryKey().toString() + " contains structures:" + treeManager.getTreeStructures().size());
    }

    @Override
    public TreeManager getTreeManager() {
        return treeManager;
    }
}
