package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class PrismBlockEntity extends BeamEmittingBlockEntity {

    public PrismBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PRISM_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, PrismBlockEntity be) {
        be.createBeamParticles(world, pos, state, be);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, PrismBlockEntity pbe) {
        pbe.recalculateAllBeams();
    }
}
