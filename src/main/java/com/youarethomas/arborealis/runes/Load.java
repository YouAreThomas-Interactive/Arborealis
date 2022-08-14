package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

public class Load extends Rune {

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.setChunkForced(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), true);
        }
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.setChunkForced(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), false);
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {
        // Draw chunk outline


    }
}
