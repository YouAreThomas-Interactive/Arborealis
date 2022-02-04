package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HollowedLogEntity extends BlockEntity {

    private BlockState logState = Blocks.OAK_LOG.getDefaultState();
    private Identifier itemID;

    public HollowedLogEntity(BlockPos pos, BlockState state) {
        super(Arborealis.HOLLOWED_LOG_ENTITY, pos, state);
        itemID = new Identifier("");
    }

    public void setLogState(BlockState logState) {
        this.logState = logState;
        this.markDirty();
    }

    public BlockState getLogState() {
        return logState;
    }

    public void setItemID(Identifier itemID) {
        this.itemID = itemID;
        this.markDirty();
    }

    public Identifier getItemID() {
        return itemID;
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.put("log_state", NbtHelper.fromBlockState(logState));
        tag.putString("item_id", itemID.toString());
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        logState = NbtHelper.toBlockState(tag.getCompound("log_state"));
        itemID = new Identifier(tag.getString("item_id"));

        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.REDRAW_ON_MAIN_THREAD);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }
}
