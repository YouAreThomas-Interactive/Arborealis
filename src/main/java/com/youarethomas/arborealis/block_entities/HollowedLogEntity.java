package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class HollowedLogEntity extends BlockEntity implements BlockEntityClientSerializable {

    private String itemID = "arborealis:item/tree_core";

    public HollowedLogEntity(BlockPos pos, BlockState state) {
        super(Arborealis.HOLLOWED_LOG_ENTITY, pos, state);

        setItemID("");
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
        updateListeners();
    }

    public String getItemID() {
        return itemID;
    }

    // Serialize the BlockEntity - storing data
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putString("item_id", itemID);

        return tag;
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        itemID = tag.getString("item_id");
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
            this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.REDRAW_ON_MAIN_THREAD);
        }
    }
}
