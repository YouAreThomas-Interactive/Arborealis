package com.youarethomas.therotwithin.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Test extends AbstractRune {
    boolean applyEffect = false;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        applyEffect = true;
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        applyEffect = false;
    }

    @Override
    public void onServerTick(World world, BlockPos blockPos, CarvedLogEntity carvedLogEntity) {
        if (applyEffect)
            ArborealisUtil.applyStatusEffectsToEntities(ArborealisUtil.getEntitiesInRadius(world, blockPos, 10, true), StatusEffects.SPEED);
    }

    @Override
    public void onClientTick(World world, BlockPos blockPos, CarvedLogEntity carvedLogEntity) {

    }
}
