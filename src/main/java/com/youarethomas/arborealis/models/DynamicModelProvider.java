package com.youarethomas.arborealis.models;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DynamicModelProvider implements ModelResourceProvider {

    private DynamicModel model;
    private final Identifier identifier;

    public DynamicModelProvider(DynamicModel model, Identifier identifier) {
        this.model = model;
        this.identifier = identifier;
    }

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if(resourceId.equals(identifier)) {
            return model;
        } else {
            return null;
        }
    }
}
