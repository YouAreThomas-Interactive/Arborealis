package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class PrismBlockEntity extends BeamEmittingBlockEntity {

    public Map<Direction, Boolean> inputSide = new HashMap<>() {{
        put(Direction.UP, false);
        put(Direction.DOWN, false);
        put(Direction.NORTH, false);
        put(Direction.EAST, false);
        put(Direction.SOUTH, false);
        put(Direction.WEST, false);
    }};

    public boolean getInputSide(Direction direction) {
        return inputSide.get(direction);
    }
    public void setInputSide(Direction direction, boolean open) {
        inputSide.replace(direction, open);
        markDirty();
    }

    public PrismBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PRISM_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, PrismBlockEntity be) {
        be.createBeamParticles(world, pos, state, be);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, PrismBlockEntity pbe) {
        for (Direction dir : Direction.values()) {
            if (pbe.getInputSide(dir))
                pbe.setShowBeam(dir, false);
        }

        pbe.recalculateAllBeams();
    }
}
