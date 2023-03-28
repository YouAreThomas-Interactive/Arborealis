package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HollowedLog extends BlockWithEntity implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public HollowedLog(Settings settings) {
        super(settings.nonOpaque().strength(2.0F));
        setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(FACING);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        HollowedLogEntity entity = ((HollowedLogEntity) world.getBlockEntity(pos));
        ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

        if (entity.getStack(0).isEmpty()) {
            if (!stackInHand.isEmpty()) {
                if (world.isClient)
                    world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.3F);

                entity.setStack(0, stackInHand.copy().split(1));
                stackInHand.decrement(1);
                entity.markDirty();

                return ActionResult.SUCCESS;
            }
        } else {
            if (world.isClient)
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.6F);

            player.getInventory().offerOrDrop(entity.getStack(0).copy());
            entity.removeStack(0);
            entity.markDirty();

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        HollowedLogEntity logEntity = (HollowedLogEntity) world.getBlockEntity(pos);
        return new ItemStack(logEntity.getLogState().getBlock().asItem());
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HollowedLogEntity(pos, state);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        HollowedLogEntity be = (HollowedLogEntity)world.getBlockEntity(pos);
        world.setBlockState(pos, be.getLogState());

        world.breakBlock(pos, !player.isCreative());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Arborealis.HOLLOWED_LOG_ENTITY, world.isClient ? HollowedLogEntity::clientTick : HollowedLogEntity::serverTick);
    }
}
