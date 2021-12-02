package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class WoodenBucketEntity extends BlockEntity {

    private int sapAmount = 0;

    public WoodenBucketEntity(BlockPos pos, BlockState state) {
        super(Arborealis.WOODEN_BUCKET_ENTITY, pos, state);
    }

    public void setSapAmount(int sapAmount) {
        this.sapAmount = sapAmount;
        updateListeners();
    }

    public int getSapAmount() {
        return sapAmount;
    }

    private void updateListeners() {
        // This method is the magic that makes the whole carving system work. No touchy
        this.markDirty();
        if (this.world != null) {

            this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putInt("sap_amount", sapAmount);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        sapAmount = tag.getInt("sap_amount");
    }
}
