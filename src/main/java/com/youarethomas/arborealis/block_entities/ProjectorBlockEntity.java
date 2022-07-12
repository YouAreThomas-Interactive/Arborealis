package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.util.ArborealisConstants;
import com.youarethomas.arborealis.util.RuneManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class ProjectorBlockEntity extends BlockEntity {

    private int lightLevel;

    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
        markDirty();
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PROJECTOR_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ProjectorBlockEntity be) {

    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ProjectorBlockEntity be) {
        BlockPos blockBehind = pos.offset(state.get(HorizontalFacingBlock.FACING).getOpposite());

        if (!world.getBlockState(blockBehind).isOf(Blocks.AIR)) {
            int lightBehind = world.getLightLevel(LightType.BLOCK, blockBehind);
            if (lightBehind != be.getLightLevel()) {
                // TODO: Would be nice to move this into neighbour update, but the light level doesn't update before it's called
                be.setLightLevel(lightBehind);
                System.out.println(lightBehind);
            }
        }
        else {
            if (be.getLightLevel() != 0) {
                be.setLightLevel(0);
            }
        }
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putInt("light_level", lightLevel);
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        lightLevel = tag.getInt("light_level");

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

    public static void updateProjector(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        BlockPos pos = packetByteBuf.readBlockPos();
        ProjectorBlockEntity pbe = (ProjectorBlockEntity) minecraftClient.world.getBlockEntity(pos);

        minecraftClient.execute(() -> {
            pbe.setLightLevel(packetByteBuf.readInt());
        });
    }
}
