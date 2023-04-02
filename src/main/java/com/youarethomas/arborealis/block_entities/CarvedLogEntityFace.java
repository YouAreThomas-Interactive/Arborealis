package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.ArborealisNbt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Direction;

public class CarvedLogEntityFace  {

    private boolean catalysed;
    private boolean emissive;
    private int[] faceArray;
    private Rune rune;

    // Create face from deserialized data
    public CarvedLogEntityFace(NbtCompound tag, Direction direction) {
        NbtCompound nbt = tag.getCompound(direction.getName());

        faceArray = nbt.getIntArray("face_array");
        catalysed = nbt.getBoolean("catalysed");
        emissive = nbt.getBoolean("emissive");
        if (nbt.contains("rune"))
            rune = ArborealisNbt.deserializeRune(nbt.getCompound("rune"));
    }

    // Create a new face
    public CarvedLogEntityFace() {
        faceArray = new int[25];
        catalysed = false;
        emissive = false;
        rune = null;
    }

    // Convert face to NbtElement
    public NbtElement serialize() {
        NbtCompound nbt = new NbtCompound();

        nbt.putIntArray("face_array", faceArray);
        nbt.putBoolean("catalysed", catalysed);
        nbt.putBoolean("emissive", emissive);
        if (rune != null)
            nbt.put("rune", ArborealisNbt.serializeRune(rune));

        return nbt;
    }

    public boolean isCatalysed() {
        return catalysed;
    }
    public void setCatalysed(boolean catalysed) {
        this.catalysed = catalysed;
    }

    public boolean isEmissive() {
        return emissive;
    }
    public void setEmissive(boolean emissive) {
        this.emissive = emissive;
    }

    public int[] getFaceArray() {
        return faceArray;
    }
    public void setFaceArray(int[] faceArray) {
        this.faceArray = faceArray;
    }

    public Rune getFaceRune() {
        return rune;
    }
    public void setFaceRune(Rune rune) {
        this.rune = rune;
    }
}
