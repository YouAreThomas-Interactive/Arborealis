package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ImplementedInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.system.Pointer;

public class HollowedLogEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> item = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private BlockState logState = Blocks.OAK_LOG.getDefaultState();

    public HollowedLogEntity(BlockPos pos, BlockState state) {
        super(Arborealis.HOLLOWED_LOG_ENTITY, pos, state);
    }

    public void setLogState(BlockState logState) {
        this.logState = logState;
        this.markDirty();
    }

    public BlockState getLogState() {
        return logState;
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, item);

        tag.put("log_state", NbtHelper.fromBlockState(logState));
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        Inventories.readNbt(tag, item);

        logState = NbtHelper.toBlockState(tag.getCompound("log_state"));

        this.markDirty();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return item;
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL | Block.FORCE_STATE);
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
