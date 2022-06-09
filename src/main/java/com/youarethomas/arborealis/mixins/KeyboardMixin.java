package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.mixin_access.KeyboardMixinAccess;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class KeyboardMixin implements KeyboardMixinAccess {

    @Shadow private MinecraftClient client;
    private boolean renderTreeOutlines = false;

    @Inject(method = "processF3(I)Z", at = @At("HEAD"), cancellable = true)
    private void processFTree(int key, CallbackInfoReturnable<Boolean> cir) {
        switch (key) {
            case 77:
                renderTreeOutlines = !renderTreeOutlines;
                ((KeyboardInvoker)(Object)this).invokeDebugLog(renderTreeOutlines ? "debug.show_tree_outline.on" : "debug.show_tree_outline.off", new Object[0]);
                cir.setReturnValue(true);
        }
    }


    @Override
    public boolean getRenderTreeOutlines() {
        return renderTreeOutlines;
    }
}
