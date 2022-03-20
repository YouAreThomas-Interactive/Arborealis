package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.raid.RaidManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements ServerWorldMixinAccess {
    private TreeManager treeManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void treeManagerInit(CallbackInfo ci) {
        this.treeManager = TreeManager.getManager((ServerWorld)(Object)this);
    }

    @Override
    public TreeManager getTreeManager() {
        return treeManager;
    }
}
