package com.youarethomas.arborealis.rendering;

import com.google.common.primitives.Floats;
import com.youarethomas.arborealis.mixins.BufferBuilderAccessor;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Vec3f;

public class BeamBufferBuilder extends BufferBuilder {

    public BeamBufferBuilder(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public void writeSortedIndices(VertexFormat.IndexType indexType) {
        Vec3f[] sortingPrimCentres = ((BufferBuilderAccessor)this).getSortingPrimitiveCenters();

        float[] fs = new float[sortingPrimCentres.length];
        int[] is = new int[sortingPrimCentres.length];
        for (int i = 0; i < sortingPrimCentres.length; ++i) {
            float f = sortingPrimCentres[i].getX() - ((BufferBuilderAccessor)this).getSortingCameraX();
            float g = sortingPrimCentres[i].getY() - ((BufferBuilderAccessor)this).getSortingCameraY();
            float h = sortingPrimCentres[i].getZ() - ((BufferBuilderAccessor)this).getSortingCameraZ();
            fs[i] = f * f + g * g + h * h;
            is[i] = i;
        }

        IntArrays.mergeSort(is, (a, b) -> Floats.compare(fs[a], fs[b]));

        IntConsumer intConsumer = ((BufferBuilderAccessor)this).callCreateIndexWriter(((BufferBuilderAccessor)this).getElementOffset(), indexType);
        for (int j : is) {
            intConsumer.accept(j * ((BufferBuilderAccessor)this).getDrawMode().additionalVertexCount);
            intConsumer.accept(j * ((BufferBuilderAccessor)this).getDrawMode().additionalVertexCount + 1);
            intConsumer.accept(j * ((BufferBuilderAccessor)this).getDrawMode().additionalVertexCount + 2);
            intConsumer.accept(j * ((BufferBuilderAccessor)this).getDrawMode().additionalVertexCount + 2);
            intConsumer.accept(j * ((BufferBuilderAccessor)this).getDrawMode().additionalVertexCount + 3);
            intConsumer.accept(j * ((BufferBuilderAccessor)this).getDrawMode().additionalVertexCount);
        }
    }
}
