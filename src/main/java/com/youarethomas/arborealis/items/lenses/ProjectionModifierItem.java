package com.youarethomas.arborealis.items.lenses;

import com.youarethomas.arborealis.block_entities.BeamEmittingBlockEntity;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ProjectionModifierItem {

    ArborealisUtil.Colour getLensColor();

    void onActivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam);

    void onDeactivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam);
}
