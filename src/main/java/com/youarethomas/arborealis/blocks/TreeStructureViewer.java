package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.block_entities.TreeStructureViewerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class TreeStructureViewer extends Block implements BlockEntityProvider {
    public TreeStructureViewer(Settings settings) {
        super(settings.strength(2.0F));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TreeStructureViewerEntity(pos, state);
    }
}
