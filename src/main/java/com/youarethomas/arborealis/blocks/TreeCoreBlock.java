package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TreeCoreBlock extends Block {

    public static final IntProperty LOG_ID = IntProperty.of("logid", 0, 7);

    public TreeCoreBlock(Settings settings) {
        super(settings.nonOpaque().luminance(value -> 8).strength(2.0F));

        setDefaultState(getStateManager().getDefaultState().with(LOG_ID, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(LOG_ID);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!player.isCreative()) {
            dropStack(world, pos, new ItemStack(Arborealis.TREE_CORE, 1));
            //dropStack(world, pos, Registry.ITEM.get(Arborealis.LogIDs.get(state.get(TreeCoreBlock.LOG_ID))).getDefaultStack());
        }
    }
}
