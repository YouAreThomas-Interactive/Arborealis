package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.CameraMixinAccess;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("TAIL"))
    public void blackoutRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        CameraMixinAccess cameraAccess = (CameraMixinAccess) MinecraftClient.getInstance().gameRenderer.getCamera();

        int windowWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int windowHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        if (cameraAccess.getCameraOffset() < 0) {
            int alpha =  Math.min(255, (int)(255.0f * (Math.abs(cameraAccess.getCameraOffset() * 2))));
            Screen.fill(matrices, 0, 0, windowWidth, windowHeight, (int) ArborealisUtil.argbToHex(alpha, 0, 0, 0));
        }
    }
}
