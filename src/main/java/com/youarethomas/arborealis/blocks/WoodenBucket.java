package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.WoodenBucketEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WoodenBucket extends Block implements BlockEntityProvider {

    public static final IntProperty SAP_LEVEL = IntProperty.of("sap_level", 0, 4);

    public WoodenBucket(Settings settings) {
        super(settings.nonOpaque());
        setDefaultState(getStateManager().getDefaultState().with(SAP_LEVEL, 0));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        WoodenBucketEntity bucketEntity = (WoodenBucketEntity)world.getBlockEntity(pos);
        BlockState tapState = world.getBlockState(pos.up());

        if (player.getStackInHand(hand).isOf(Items.GLASS_BOTTLE) && state.get(SAP_LEVEL) > 0)
        {
            player.getInventory().offerOrDrop(Arborealis.BOTTLED_SAP.getDefaultStack());
            player.getStackInHand(hand).decrement(1);
            changeSapLevel(world, pos, -1);

            if (tapState.isOf(Arborealis.TREE_TAP) && tapState.get(TreeTap.READY)) {
                changeSapLevel(world, pos, 1);
                world.setBlockState(pos.up(), tapState.with(TreeTap.READY, false));
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockState tapState = world.getBlockState(pos.up());

        if (tapState.isOf(Arborealis.TREE_TAP) && tapState.get(TreeTap.READY)) {
            changeSapLevel(world, pos, 1);
            world.setBlockState(pos.up(), tapState.with(TreeTap.READY, false));
        }
    }

    public static BlockState changeSapLevel(World world, BlockPos pos, int amountToChange) {
        WoodenBucketEntity bucketEntity = (WoodenBucketEntity)world.getBlockEntity(pos);

        boolean rebuild = false;

        // Increase or decrease sap level
        if (amountToChange > 0 && bucketEntity.getSapAmount() < 12) {
            bucketEntity.setSapAmount(bucketEntity.getSapAmount() + 1);
            rebuild = true;
        } else if (amountToChange < 0 && bucketEntity.getSapAmount() > 0) {
            bucketEntity.setSapAmount(bucketEntity.getSapAmount() - 1);
            rebuild = true;
        }

        BlockState state = world.getBlockState(pos);
        BlockState filledState = null;

        if (rebuild && state.isOf(Arborealis.WOODEN_BUCKET)) {
            if (bucketEntity.getSapAmount() > 0 && bucketEntity.getSapAmount() <= 4){
                filledState = state.with(WoodenBucket.SAP_LEVEL, 1);
            } else if (bucketEntity.getSapAmount() > 4 && bucketEntity.getSapAmount() <= 8) {
                filledState = state.with(WoodenBucket.SAP_LEVEL, 2);
            } else if (bucketEntity.getSapAmount() > 8 && bucketEntity.getSapAmount() < 12) {
                filledState = state.with(WoodenBucket.SAP_LEVEL, 3);
            } else if (bucketEntity.getSapAmount() == 12){
                filledState = state.with(WoodenBucket.SAP_LEVEL, 4);
            } else {
                filledState = state.with(WoodenBucket.SAP_LEVEL, 0);
            }

            world.setBlockState(pos, filledState);
        }

        return filledState;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(SAP_LEVEL);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        double pixelSize = 1.0D / 16.0D;

        return VoxelShapes.cuboid(2D * pixelSize, 0D, 2f * pixelSize, 1D - (2D * pixelSize), 12D * pixelSize, 1D - (2D * pixelSize));
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("block.arborealis.wooden_bucket.tooltip1"));
            tooltip.add(Text.translatable("block.arborealis.wooden_bucket.tooltip2"));
            tooltip.add(Text.translatable("block.arborealis.wooden_bucket.tooltip3"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WoodenBucketEntity(pos, state);
    }
}
