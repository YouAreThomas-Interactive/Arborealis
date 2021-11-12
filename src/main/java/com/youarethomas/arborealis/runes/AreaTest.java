package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.youarethomas.arborealis.util.ArborealisUtil.applyStatusEffectsToEntities;
import static com.youarethomas.arborealis.util.ArborealisUtil.getEntitiesInRadius;

public class AreaTest extends AbstractRune {

    boolean applyStatus = false;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedWoodEntity be) {
        applyStatus = true;
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedWoodEntity be) {
        applyStatus = false;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedWoodEntity be) {
        applyStatusEffectsToEntities(getEntitiesInRadius(world, pos, be.radius, true), StatusEffects.SPEED);
    }

    @Override
    public boolean showRadiusEffect() {
        return true;
    }
}
