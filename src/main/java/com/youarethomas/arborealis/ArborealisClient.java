package com.youarethomas.arborealis;

import com.youarethomas.arborealis.models.ModelRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ArborealisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelRegistry.registerModels();
    }
}
