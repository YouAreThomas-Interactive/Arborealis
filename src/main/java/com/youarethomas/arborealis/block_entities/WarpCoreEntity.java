package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ArborealisPersistentState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WarpCoreEntity extends BlockEntity {

    private static boolean allowTeleport = true; // This likely won't work for multiplayer

    public WarpCoreEntity(BlockPos pos, BlockState state) {
        super(Arborealis.WARP_CORE_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, WarpCoreEntity be) {

    }

    public static void serverTick(World world, BlockPos pos, BlockState state, WarpCoreEntity be) {
        List<PlayerEntity> players = (List<PlayerEntity>) world.getPlayers();

        for (PlayerEntity player : players) {
            if (world.getBlockState(player.getBlockPos().down()).isOf(Arborealis.WARP_CORE) && player.getBlockPos().down().equals(pos) && player.isSneaking() && allowTeleport) {
                ArborealisPersistentState worldNbt = ((ServerWorld)world).getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");

                List<BlockPos> corePositions = new ArrayList<>(worldNbt.getWarpCoreList());
                if (corePositions.size() > 1) {
                    corePositions.remove(pos);
                    BlockPos randomPos = corePositions.get(Arborealis.RANDOM.nextInt(corePositions.size())).up();

                    player.teleport(randomPos.getX() + 0.5D, randomPos.getY(), randomPos.getZ() + 0.5D);
                }

                allowTeleport = false;
            } else if (world.getBlockState(player.getBlockPos().down()).isOf(Arborealis.WARP_CORE) && !player.isSneaking()) {
                allowTeleport = true;
            }
        }
    }
}
