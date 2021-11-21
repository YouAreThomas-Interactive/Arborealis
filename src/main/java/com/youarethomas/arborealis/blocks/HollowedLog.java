package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HollowedLog extends Block implements BlockEntityProvider {

    public static final IntProperty LOG_ID = IntProperty.of("logid", 0, 7);

    public HollowedLog(Settings settings) {
        super(settings.nonOpaque().strength(2.0F));

        setDefaultState(getStateManager().getDefaultState().with(LOG_ID, 0));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        HollowedLogEntity entity = ((HollowedLogEntity) world.getBlockEntity(pos));

        if (Objects.equals(entity.getItemID(), "")) {
            if (player.getStackInHand(hand).isOf(Arborealis.TREE_CORE)) {
                world.setBlockState(pos, Arborealis.TREE_CORE_BLOCK.getDefaultState().with(TreeCoreBlock.LOG_ID, state.get(HollowedLog.LOG_ID)));

                // Update all runes in the tree
                TreeManager.checkLifeForce(world, pos);

                if (!player.isCreative()) {
                    player.getStackInHand(hand).decrement(1);
                }
                return ActionResult.SUCCESS;
            } else if (!player.getStackInHand(hand).isEmpty()) {
                ItemStack itemInHand = player.getStackInHand(hand);
                entity.setItemID(Registry.ITEM.getId(itemInHand.getItem()).toString());
                System.out.println(Registry.ITEM.getId(itemInHand.getItem()));

                if (!player.isCreative()) {
                    itemInHand.decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        } else {
            player.getInventory().offerOrDrop(Registry.ITEM.get(new Identifier(entity.getItemID())).getDefaultStack());
            entity.setItemID("");
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
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(LOG_ID);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!player.isCreative()) {
            HollowedLogEntity entity = (HollowedLogEntity)world.getBlockEntity(pos);
            dropStack(world, pos, Registry.ITEM.get(new Identifier(entity.getItemID())).getDefaultStack());
            dropStack(world, pos, Registry.ITEM.get(Arborealis.LogIDs.get(state.get(TreeCoreBlock.LOG_ID))).getDefaultStack());
        }
    }
}
