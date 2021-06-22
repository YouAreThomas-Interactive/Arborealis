package com.youarethomas.arborealis.models;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public class ModelRegistry {

    public static void registerModels()
    {
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(manager -> CarvedWoodUnbaked.VariantProvider.INSTANCE);
    }
}
