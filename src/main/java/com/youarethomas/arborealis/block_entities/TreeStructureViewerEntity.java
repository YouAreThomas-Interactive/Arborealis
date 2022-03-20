package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class TreeStructureViewerEntity extends BlockEntity {
    public TreeManager manager;

    public TreeStructureViewerEntity(BlockPos pos, BlockState state) {
        super(Arborealis.TREE_STRUCTURE_VIEWER_ENTITY, pos, state);

        if(this.world != null && !this.world.isClient)
            manager = TreeManager.getManager((ServerWorld) this.world);
    }
}
