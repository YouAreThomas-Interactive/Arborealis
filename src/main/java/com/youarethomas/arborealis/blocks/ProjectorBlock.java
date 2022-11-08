package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.block_entities.ProjectorBlockEntity;
import com.youarethomas.arborealis.block_entities.WoodenBucketEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.SimpleVoxelShape;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.LightType;
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

        return VoxelShapes.union(baseShape);

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
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ProjectorBlockEntity pbe = (ProjectorBlockEntity) world.getBlockEntity(pos);
        ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

        if (pbe.getStack(0).isEmpty()) {
            if (stackInHand.isOf(Arborealis.CARVED_STENCIL) || stackInHand.isOf(Arborealis.INFUSION_LENS)) {
                if (world.isClient) {
                    world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.3F);
                } else {
                    pbe.setStack(0, stackInHand.copy().split(1));
                    stackInHand.decrement(1);
                    pbe.markDirty();
                }

                return ActionResult.SUCCESS;
            }
        } else {
            if (world.isClient) {
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.6F);
            } else {
                player.getInventory().offerOrDrop(pbe.getStack(0).copy());
                pbe.removeStack(0);
                pbe.markDirty();
            }

            return ActionResult.SUCCESS;
        }

        pbe.markDirty();

        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Remove stencil or lens and update projector beam to remove any light stencils
        ProjectorBlockEntity pbe = (ProjectorBlockEntity) world.getBlockEntity(pos);
        ItemScatterer.spawn(world, pos, pbe.getItems());
        pbe.removeStack(0);
        pbe.updateProjector();
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
