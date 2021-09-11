package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class CarvedWoodEntity extends BlockEntity implements BlockEntityClientSerializable {

    private String logID = "";

    private int[] faceNorth = new int[49];
    private int[] faceEast = new int[49];
    private int[] faceSouth = new int[49];
    private int[] faceWest = new int[49];

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public void performCarve() {
        setFaceArray(Direction.NORTH, Arrays.stream(getFaceArray(Direction.NORTH)).map(i -> i == 2 ? 1 : i).toArray());
        setFaceArray(Direction.EAST, Arrays.stream(getFaceArray(Direction.EAST)).map(i -> i == 2 ? 1 : i).toArray());
        setFaceArray(Direction.SOUTH, Arrays.stream(getFaceArray(Direction.SOUTH)).map(i -> i == 2 ? 1 : i).toArray());
        setFaceArray(Direction.WEST, Arrays.stream(getFaceArray(Direction.WEST)).map(i -> i == 2 ? 1 : i).toArray());

        System.out.println(StringUtils.join(ArrayUtils.toObject(getFaceArray(Direction.NORTH)), ", "));
    }

    public void setLogID(String logID) {
        this.logID = logID;
        updateListeners();
    }

    public String getLogID() {
        return logID;
    }

    public void setFaceArray(Direction direction, int[] array) {
        switch (direction) {
            case NORTH -> this.faceNorth = array;
            case EAST -> this.faceEast = array;
            case SOUTH -> this.faceSouth = array;
            case WEST -> this.faceWest = array;
        }

        updateListeners();
    }

    public int[] getFaceArray(Direction direction) {
        switch (direction) {
            case NORTH -> {
                return faceNorth;
            }
            case EAST -> {
                return faceEast;
            }
            case SOUTH -> {
                return faceSouth;
            }
            case WEST -> {
                return faceWest;
            }
            default -> {
                return null;
            }
        }
    }

    // Serialize the BlockEntity - storing data
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putString("log_id", logID);
        tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);

        return tag;
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        logID = tag.getString("log_id");
        faceNorth = tag.getIntArray("face_north");
        faceEast = tag.getIntArray("face_east");
        faceSouth = tag.getIntArray("face_south");
        faceWest = tag.getIntArray("face_west");
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
        updateListeners();
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    private void updateListeners() {
        // This method is the magic that makes the whole carving system work. No touchy
        this.markDirty();
        if (this.world != null) {
            this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }
}
