package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HollowedLog extends HorizontalFacingBlock implements BlockEntityProvider {

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

        if (Objects.equals(entity.getItemID(), new Identifier(""))) {
            if (!player.getStackInHand(hand).isEmpty()) {
                if (world.isClient) {
                    world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.3F);
                } else {
                    // Update all runes in the tree
                    // TreeManager.checkLifeForce(world, pos);
                }

                ItemStack itemInHand = player.getStackInHand(hand);
                entity.setItemID(Registry.ITEM.getId(itemInHand.getItem()));


                if (!player.isCreative()) {
                    itemInHand.decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        } else {
            if (world.isClient) {
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.75F, 0.6F);
            } else {
                player.getInventory().offerOrDrop(Registry.ITEM.get(entity.getItemID()).getDefaultStack());
                entity.setItemID(new Identifier(""));
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
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
}
