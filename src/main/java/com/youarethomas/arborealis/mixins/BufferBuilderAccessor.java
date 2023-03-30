package com.youarethomas.arborealis.mixins;

import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Accessor("sortingPrimitiveCenters")
    Vector3f[] getSortingPrimitiveCenters();

    @Accessor("sortingCameraX")
    float getSortingCameraX();

    @Accessor("sortingCameraY")
    float getSortingCameraY();

    @Accessor("sortingCameraZ")
    float getSortingCameraZ();

    @Accessor("elementOffset")
    int getElementOffset();

    @Accessor("drawMode")
    VertexFormat.DrawMode getDrawMode();

    @Invoker("getIndexConsumer")
    IntConsumer callGetIndexConsumer(int offset, VertexFormat.IndexType indexType);
}
