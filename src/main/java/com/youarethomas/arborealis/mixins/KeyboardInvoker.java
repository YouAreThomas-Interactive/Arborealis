package com.youarethomas.arborealis.mixins;

import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Keyboard.class)
public interface KeyboardInvoker {

    @Invoker("debugLog")
    void invokeDebugLog(String key, Object ... args);
}
