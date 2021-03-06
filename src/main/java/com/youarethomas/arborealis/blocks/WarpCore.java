package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.block_entities.WarpCoreEntity;
import com.youarethomas.arborealis.misc.ArborealisPersistentState;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class WarpCore extends BlockWithEntity implements BlockEntityProvider {

    public WarpCore(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WarpCoreEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.isOf(Items.NAME_TAG) && stack.hasCustomName()) {
            if (world.getBlockEntity(pos) instanceof WarpCoreEntity be && !world.isClient) {
                ArborealisPersistentState worldNbt = ((ServerWorld) world).getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");
                Map<BlockPos, String> warpCores = worldNbt.getWarpCoreList();

                for (Map.Entry<BlockPos, String> entry : warpCores.entrySet()) {
                    if (entry.getKey().equals(pos)) {
                        warpCores.replace(pos, stack.getName().getString());
                        worldNbt.setWarpCoreList(warpCores);
                        stack.decrement(1);

                        return ActionResult.SUCCESS;
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world instanceof ServerWorld serverWorld) {
            ArborealisPersistentState worldNbt = serverWorld.getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");
            worldNbt.addWarpCore(pos, "Warp Tree");

            System.out.println("Warp core placed. Count now at: " + worldNbt.getWarpCoreList().size());
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            ArborealisPersistentState worldNbt = serverWorld.getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");

            worldNbt.removeWarpCore(pos);

            System.out.println("Warp core broken: " + worldNbt.getWarpCoreList().size());
        }

        super.onBreak(world, pos, state, player);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Arborealis.WARP_CORE_ENTITY, world.isClient ? WarpCoreEntity::clientTick : WarpCoreEntity::serverTick);
    }
}
