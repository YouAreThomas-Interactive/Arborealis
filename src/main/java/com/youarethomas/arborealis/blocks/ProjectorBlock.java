package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.items.lenses.ProjectionModifierItem;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProjectorBlock extends BlockWithEntity implements BlockEntityProvider {

    protected static final VoxelShape SINGLE_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public ProjectorBlock(Settings settings) {
        super(settings.nonOpaque().strength(1.0F));
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape baseShape = Block.createCuboidShape(0, 0, 0, 16, 1, 16);

        VoxelShape funnel1 = baseShape;
        VoxelShape funnel2 = baseShape;
        VoxelShape funnel3 = baseShape;
        VoxelShape funnel4 = baseShape;
        switch (state.get(HorizontalFacingBlock.FACING)) {
            case NORTH -> {
                funnel1 = Block.createCuboidShape(0, 1, 4, 16, 16, 7);
                funnel2 = Block.createCuboidShape(1, 1, 7, 15, 15, 10);
                funnel3 = Block.createCuboidShape(3, 3, 10, 13, 13, 13);
                funnel4 = Block.createCuboidShape(5, 5, 13, 11, 11, 16);
            }
            case SOUTH -> {
                funnel1 = Block.createCuboidShape(0, 1, 9, 16, 16, 12);
                funnel2 = Block.createCuboidShape(1, 1, 6, 15, 15, 9);
                funnel3 = Block.createCuboidShape(3, 3, 3, 13, 13, 6);
                funnel4 = Block.createCuboidShape(5, 5, 0, 11, 11, 3);
            }
            case EAST -> {
                funnel1 = Block.createCuboidShape(9, 1, 0, 12, 16, 16);
                funnel2 = Block.createCuboidShape(6, 1, 1, 9, 15, 15);
                funnel3 = Block.createCuboidShape(3, 3, 3, 6,13, 13);
                funnel4 = Block.createCuboidShape(0, 5, 5, 3, 11, 11);
            }
            case WEST -> {
                funnel1 = Block.createCuboidShape(4, 1, 0, 7, 16, 16);
                funnel2 = Block.createCuboidShape(7, 1, 1, 10, 15, 15);
                funnel3 = Block.createCuboidShape(10, 3, 3, 13, 13, 13);
                funnel4 = Block.createCuboidShape(13, 5, 5, 16, 11, 11);
            }
        }

        return VoxelShapes.union(baseShape, funnel1, funnel2, funnel3, funnel4);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ProjectorBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ProjectorBlockEntity pbe = (ProjectorBlockEntity) world.getBlockEntity(pos);
        ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

        if (pbe.getStack(0).isEmpty()) {
            if (stackInHand.getItem() instanceof ProjectionModifierItem) {
                if (world.isClient) {
                    world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.3F);
                } else {
                    pbe.setStack(0, stackInHand.copy().split(1));
                    stackInHand.decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        } else {
            if (world.isClient) {
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.6F);
            } else {
                player.getInventory().offerOrDrop(pbe.getStack(0).copy());
                pbe.removeStack(0);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Remove stencil or lens and update projector beam to remove any light stencils
        ProjectorBlockEntity pbe = (ProjectorBlockEntity) world.getBlockEntity(pos);
        ItemScatterer.spawn(world, pos, pbe.getItems());
        pbe.removeStack(0);
        pbe.setLightLevel(0);
        super.onBreak(world, pos, state, player);
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("block.arborealis.projector.tooltip1"));
            tooltip.add(Text.translatable("block.arborealis.projector.tooltip2"));
            tooltip.add(Text.translatable("block.arborealis.projector.tooltip3"));
            tooltip.add(Text.translatable("block.arborealis.projector.tooltip4"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Arborealis.PROJECTOR_ENTITY, world.isClient ? ProjectorBlockEntity::clientTick : ProjectorBlockEntity::serverTick);
    }
}
