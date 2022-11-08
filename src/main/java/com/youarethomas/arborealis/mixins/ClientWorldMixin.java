package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.ClientWorldMixinAccess;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements ClientWorldMixinAccess {

    private static boolean muteSounds = false;

    @ModifyVariable(method = "Lnet/minecraft/client/world/ClientWorld;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"), ordinal = 1)
    private float mufflePitch(float pitch) {
        if (muteSounds)
            return pitch * 0.6f;

        return pitch;
    }

    @ModifyVariable(method = "Lnet/minecraft/client/world/ClientWorld;playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At("HEAD"), ordinal = 0)
    private float muffleVolume(float volume) {
        if (muteSounds)
            return volume * 0.05f;

        return volume;
    }

    @Override
    public void setMuted(boolean muted) {
        muteSounds = muted;
    }
}
