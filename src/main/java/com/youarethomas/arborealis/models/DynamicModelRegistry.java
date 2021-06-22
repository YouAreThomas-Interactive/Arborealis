package com.youarethomas.arborealis.models;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;

public class DynamicModelRegistry {

    private static Collection<DynamicModelProvider> modelProviders = new ArrayList<>();

    public static void register(DynamicModel model, Identifier identifier) {
        modelProviders.add(new DynamicModelProvider(model, identifier));
    }

    public static void registerModels()
    {
        for (DynamicModelProvider modelProvider : modelProviders) {
            ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> modelProvider);
        }
    }
}
