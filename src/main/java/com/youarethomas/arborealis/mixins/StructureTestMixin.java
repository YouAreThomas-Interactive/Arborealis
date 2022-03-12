package com.youarethomas.arborealis.mixins;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureBlockBlockEntity.class)
public class StructureTestMixin {
    @Inject(method = "loadStructure(Lnet/minecraft/server/world/ServerWorld;Z)Z", at = @At("HEAD"))
    private void injected(ServerWorld world, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        System.out.println(((StructureBlockBlockEntity)(Object)this).getStructureName());
    }
}
