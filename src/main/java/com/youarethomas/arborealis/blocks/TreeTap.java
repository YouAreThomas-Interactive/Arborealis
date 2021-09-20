package com.youarethomas.arborealis.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreeTap extends HorizontalFacingBlock {

    public TreeTap(Settings settings) {
        super(settings.nonOpaque());
        setDefaultState(getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        switch(dir) {
            case NORTH:
                return VoxelShapes.cuboid(0.3f, 0.1f, 0.6f, 0.7f, 0.5f, 1.0f);
            case SOUTH:
                return VoxelShapes.cuboid(0.3f, 0.1f, 0.0f, 0.7f, 0.5f, 0.4f);
            case EAST:
                return VoxelShapes.cuboid(0.0f, 0.1f, 0.3f, 0.4f, 0.5f, 0.7f);
            case WEST:
                return VoxelShapes.cuboid(0.6f, 0.1f, 0.3f, 1.0f, 0.5f, 0.7f);
            default:
                return VoxelShapes.fullCube();
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, direction);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (!(ctx.getSide() == Direction.DOWN || ctx.getSide() == Direction.UP)) {
            return getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getSide());
        }

        return null;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("block.arborealis.tree_tap.tooltip1"));
            tooltip.add(new TranslatableText("block.arborealis.tree_tap.tooltip2"));
            tooltip.add(new TranslatableText("block.arborealis.tree_tap.tooltip3"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}
