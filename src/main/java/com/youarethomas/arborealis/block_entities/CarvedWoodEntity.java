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

    public int[] faceNorth = {
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 2, 0, 0, 0,
            0, 0, 0, 2, 0, 0, 0,
            0, 2, 0, 2, 2, 2, 0,
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 2, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0
    };
    public int[] faceEast = {
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 1, 0, 1, 1, 1, 0,
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0
    };
    public int[] faceSouth = {
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 1, 0, 1, 1, 1, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0
    };
    public int[] faceWest = {
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 1, 1, 1, 1, 1, 0,
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0
    };

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public void performCarve(Direction face) {
        switch (face) {
            case NORTH -> {
                faceNorth = Arrays.stream(faceNorth).map(i -> i == 2 ? 1 : i).toArray();
            }
            case EAST -> faceEast = Arrays.stream(faceEast).map(i -> i == 2 ? 1 : i).toArray();
            case SOUTH -> faceSouth = Arrays.stream(faceSouth).map(i -> i == 2 ? 1 : i).toArray();
            case WEST -> faceWest = Arrays.stream(faceWest).map(i -> i == 2 ? 1 : i).toArray();
        }

/*        NbtCompound tag = new NbtCompound();
        tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);

        readNbt(tag);
        markDirty();*/
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

    // Serialize the BlockEntity
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        toClientTag(tag);

        tag.putString("log_id", logID);
        /*tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);*/

        return tag;
    }

    // Deserialize the BlockEntity
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        fromClientTag(tag);

        logID = tag.getString("log_id");
        /*faceNorth = tag.getIntArray("face_north");
        faceEast = tag.getIntArray("face_east");
        faceSouth = tag.getIntArray("face_south");
        faceWest = tag.getIntArray("face_west");*/
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        super.readNbt(tag);
        logID = tag.getString("log_id");
        /*faceNorth = tag.getIntArray("face_north");
        faceEast = tag.getIntArray("face_east");
        faceSouth = tag.getIntArray("face_south");
        faceWest = tag.getIntArray("face_west");*/
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        super.writeNbt(tag);
        tag.putString("log_id", logID);
        /*tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);*/
        return tag;
    }
}
