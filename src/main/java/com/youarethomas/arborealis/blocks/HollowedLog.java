package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
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
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        HollowedLogEntity entity = ((HollowedLogEntity) world.getBlockEntity(pos));

        player.sendMessage(new LiteralText(entity.getItemID().toString()), false);

        if (Objects.equals(entity.getItemID(), new Identifier(""))) {
            if (!player.getStackInHand(hand).isEmpty()) {
                ItemStack itemInHand = player.getStackInHand(hand);
                entity.setItemID(Registry.ITEM.getId(itemInHand.getItem()));
                System.out.println(Registry.ITEM.getId(itemInHand.getItem()));

                // Update all runes in the tree
                TreeManager.checkLifeForce(world, pos);

                if (!player.isCreative()) {
                    itemInHand.decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        } else {
            player.getInventory().offerOrDrop(Registry.ITEM.get(entity.getItemID()).getDefaultStack());
            entity.setItemID(new Identifier(""));
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
