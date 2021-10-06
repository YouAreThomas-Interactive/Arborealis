package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class TreeTap extends HorizontalFacingBlock {

    public static final BooleanProperty READY = BooleanProperty.of("ready");

    public TreeTap(Settings settings) {
        super(settings.nonOpaque());
        setDefaultState(getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(READY, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(READY);
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
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).isOf(Items.GLASS_BOTTLE) && state.get(READY))
        {
            player.getInventory().offerOrDrop(Arborealis.BOTTLED_SAP.getDefaultStack());
            player.getStackInHand(hand).decrement(1);
            world.setBlockState(pos, state.with(READY, false), Block.NOTIFY_LISTENERS);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(2) == 0) {
            System.out.println(state.get(Properties.HORIZONTAL_FACING));
            if (world.getBlockState(pos.offset(state.get(Properties.HORIZONTAL_FACING), -1)).isIn(BlockTags.LOGS))
            {
                world.setBlockState(pos, state.with(READY, true), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("block.arborealis.tree_tap.tooltip1"));
            tooltip.add(new TranslatableText("block.arborealis.tree_tap.tooltip2"));
            tooltip.add(new TranslatableText("block.arborealis.tree_tap.tooltip3"));
            tooltip.add(new TranslatableText("block.arborealis.tree_tap.tooltip4"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}
