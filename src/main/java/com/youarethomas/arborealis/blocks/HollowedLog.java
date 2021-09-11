package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HollowedLog extends Block implements BlockEntityProvider {

    public enum LogIDs { OAK, SPRUCE, BIRCH, JUNGLE, DARK_OAK, ACACIA, CRIMSON, WARPED}
    public static final IntProperty LOG_ID = IntProperty.of("logid", 0, 7);

    public HollowedLog(Settings settings) {
        super(settings.nonOpaque());

        setDefaultState(getStateManager().getDefaultState().with(LOG_ID, LogIDs.OAK.ordinal()));
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
}
