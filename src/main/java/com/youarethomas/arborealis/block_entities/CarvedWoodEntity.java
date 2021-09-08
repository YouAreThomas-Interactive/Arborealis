package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CarvedWoodEntity extends BlockEntity implements BlockEntityClientSerializable {

    private String logID = "";

    public int[] faceNorth = new int[49];
    public int[] faceEast = new int[49];
    public int[] faceSouth = new int[49];
    public int[] faceWest = new int[49];

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public void performCarve(Direction face) {
        switch (face) {
            case NORTH -> faceNorth = Arrays.stream(faceNorth).map(i -> i == 2 ? 1 : i).toArray();
            case EAST -> faceEast = Arrays.stream(faceEast).map(i -> i == 2 ? 1 : i).toArray();
            case SOUTH -> faceSouth = Arrays.stream(faceSouth).map(i -> i == 2 ? 1 : i).toArray();
            case WEST -> faceWest = Arrays.stream(faceWest).map(i -> i == 2 ? 1 : i).toArray();
        }
    }

    public void setLogID(String logID) {
        NbtCompound tag = new NbtCompound();
        tag.putString("log_id", logID);

        readNbt(tag);
        markDirty();
    }

    public String getLogID() {
        NbtCompound tag = new NbtCompound();
        tag = writeNbt(tag);

        return tag.getString("log_id");
    }

    public void setFaceArray(Direction direction, int[] array) {
        NbtCompound tag = new NbtCompound();

        switch (direction) {
            case NORTH -> tag.putIntArray("face_north", array);
            case EAST -> tag.putIntArray("face_east", array);
            case SOUTH -> tag.putIntArray("face_south", array);
            case WEST -> tag.putIntArray("face_west", array);
        }

        readNbt(tag);
        markDirty();
    }

    public int[] getFaceArray(Direction direction) {
        NbtCompound tag = new NbtCompound();
        tag = writeNbt(tag);

        switch (direction) {
            case NORTH -> {
                return tag.getIntArray("face_north");
            }
            case EAST -> {
                return tag.getIntArray("face_east");
            }
            case SOUTH -> {
                return tag.getIntArray("face_south");
            }
            case WEST -> {
                return tag.getIntArray("face_west");
            }
            default -> {
                return null;
            }
        }
    }

    // Serialize the BlockEntity
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        toClientTag(tag);

        tag.putString("log_id", logID);
        tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);

        return tag;
    }

    // Deserialize the BlockEntity
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        fromClientTag(tag);

        logID = tag.getString("log_id");
        faceNorth = tag.getIntArray("face_north");
        faceEast = tag.getIntArray("face_east");
        faceSouth = tag.getIntArray("face_south");
        faceWest = tag.getIntArray("face_west");
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        super.readNbt(tag);
        logID = tag.getString("log_id");
        faceNorth = tag.getIntArray("face_north");
        faceEast = tag.getIntArray("face_east");
        faceSouth = tag.getIntArray("face_south");
        faceWest = tag.getIntArray("face_west");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putString("log_id", logID);
        tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);
        return tag;
    }
}
