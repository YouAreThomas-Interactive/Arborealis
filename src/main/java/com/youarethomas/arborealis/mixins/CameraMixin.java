package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.CameraMixinAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin implements CameraMixinAccess {
    private static float cameraOffset = 0f;
    @Shadow private float cameraY;

    @Inject(method = "updateEyeHeight()V", at = @At("TAIL"))
    public void abrUpdateEyeHeight(CallbackInfo callbackInfo) {
        cameraY += cameraOffset;
    }

    @Override
    public void setCameraOffset(float offset) {
        cameraOffset = offset;
    }

    @Override
    public float getCameraOffset() {
        return cameraOffset;
    }
}
