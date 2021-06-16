package com.youarethomas.arborealis.models;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ArborealisModelProvider implements ModelResourceProvider {

    private static final Identifier CARVED_WOOD = new Identifier("arborealis:block/carved_wood_model");

    @Override
    public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
        if(resourceId.equals(CARVED_WOOD)) {
            return new CarvedWoodUnbaked();
        } else {
            return null;
        }
    }
}
