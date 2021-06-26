package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class CarvedWoodEntity extends BlockEntity implements BlockEntityClientSerializable {

    private String logID = "";

    private int[] face = {
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 1, 0, 1, 1, 1, 0,
            0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0
    };

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public void setLogID(String logID) {
        NbtCompound tag = new NbtCompound();
        tag.putString("log_id", logID);

        fromClientTag(tag);
        markDirty();
    }

    public String getLogID() {
        NbtCompound tag = new NbtCompound();
        tag = toClientTag(tag);

        return tag.getString("log_id");
    }

    // Serialize the BlockEntity
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        // Save the current value of the number to the tag
        tag.putIntArray("north_face", face);
        tag.putString("log_id", logID);

        return tag;
    }

    // Deserialize the BlockEntity
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        face = tag.getIntArray("north_face");
        logID = tag.getString("log_id");
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        super.readNbt(tag);
        face = tag.getIntArray("north_face");
        logID = tag.getString("log_id");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        super.writeNbt(tag);

        // Save the current value of the number to the tag
        tag.putIntArray("north_face", face);
        tag.putString("log_id", logID);

        return tag;
    }
}
