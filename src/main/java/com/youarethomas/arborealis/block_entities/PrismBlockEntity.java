package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;

public class PrismBlockEntity extends BlockEntity {

    public Map<Direction, Boolean> openFaces = new HashMap<>() {{
        put(Direction.UP, true);
        put(Direction.DOWN, false);
        put(Direction.NORTH, false);
        put(Direction.EAST, false);
        put(Direction.SOUTH, false);
        put(Direction.WEST, false);
    }};

    public PrismBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PRISM_ENTITY, pos, state);
    }

    public boolean getFaceClosed(Direction direction) {
        return openFaces.get(direction);
    }
    public void setFaceClosed(Direction direction, boolean open) {
        openFaces.replace(direction, open);
        System.out.println(openFaces.get(direction));
        this.markDirty();
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        for (Map.Entry<Direction, Boolean> face : openFaces.entrySet()) {
            tag.putBoolean(face.getKey().getName(), face.getValue());
        }
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        for (Direction face : Direction.values()) {
            openFaces.replace(face, tag.getBoolean(face.getName()));
        }

        this.markDirty();
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
