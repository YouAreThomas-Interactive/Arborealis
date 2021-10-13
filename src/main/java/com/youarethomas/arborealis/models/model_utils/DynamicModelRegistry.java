package com.youarethomas.arborealis.models.model_utils;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;

public class DynamicModelRegistry {

    private static Collection<DynamicModelProvider> modelProviders = new ArrayList<>();

    public static void register(UnbakedModel model, Identifier identifier) {
        modelProviders.add(new DynamicModelProvider(model, identifier));
    }

    public static void registerModels()
    {
        for (DynamicModelProvider modelProvider : modelProviders) {
            ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> modelProvider);
        }
    }
}
