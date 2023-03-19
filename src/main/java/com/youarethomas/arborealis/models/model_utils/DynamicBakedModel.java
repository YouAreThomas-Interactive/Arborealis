package com.youarethomas.arborealis.models.model_utils;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.render.model.BakedModel;

public class DynamicBakedModel {
    private BakedModel model;
    private RenderContext.QuadTransform transform;

    public DynamicBakedModel(BakedModel model) {
        this.model = model;
    }

    public DynamicBakedModel(BakedModel model, RenderContext.QuadTransform transform) {
        this.model = model;
        this.transform = transform;
    }

    public BakedModel getModel() {
        return model;
    }

    public RenderContext.QuadTransform getTransform() {
        return transform;
    }
}
