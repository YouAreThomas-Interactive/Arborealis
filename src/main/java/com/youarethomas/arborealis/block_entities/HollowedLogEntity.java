package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class HollowedLogEntity extends BlockEntity {

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
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putString("item_id", itemID);
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        itemID = tag.getString("item_id");
    }

    private void updateListeners() {
        // This method is the magic that makes the whole carving system work. No touchy
        this.markDirty();
        if (this.world != null) {
            this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.REDRAW_ON_MAIN_THREAD);
        }
    }
}
