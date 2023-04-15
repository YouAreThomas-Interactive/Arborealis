package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class PrismBlockEntity extends BeamEmittingBlockEntity {

    public Map<Direction, Boolean> sideOpen = new HashMap<>() {{
        put(Direction.UP, false);
        put(Direction.DOWN, false);
        put(Direction.NORTH, false);
        put(Direction.EAST, false);
        put(Direction.SOUTH, false);
        put(Direction.WEST, false);
    }};

    public Map<Direction, Boolean> sideInput = new HashMap<>() {{
        put(Direction.UP, false);
        put(Direction.DOWN, false);
        put(Direction.NORTH, false);
        put(Direction.EAST, false);
        put(Direction.SOUTH, false);
        put(Direction.WEST, false);
    }};

    public boolean getSideOpen(Direction direction) {
        return sideOpen.get(direction);
    }
    public void setSideOpen(Direction direction, boolean open) {
        sideOpen.replace(direction, open);
        markDirty();
    }

    public boolean getSideInput(Direction direction) {
        return sideInput.get(direction);
    }
    public void setSideInput(Direction direction, boolean input) {
        sideInput.replace(direction, input);
        markDirty();
    }

    public PrismBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PRISM_ENTITY, pos, state);
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        for (Direction dir : Direction.values()) {
            tag.putBoolean("input_" + dir.getName(), getSideInput(dir));
            tag.putBoolean("open_" + dir.getName(), getSideOpen(dir));
        }
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        for (Direction dir : Direction.values()) {
            setSideInput(dir, tag.getBoolean("input_" + dir.getName()));
            setSideOpen(dir, tag.getBoolean("open_" + dir.getName()));
        }

        this.markDirty();
    }

    public void checkBeamInputs() {
        // Check whether any input sides are open, and toggle beams respectively
        boolean anyInputSideOpen = false;
        for (Direction inputDir : Direction.values()) {
            if (getSideInput(inputDir) && getSideOpen(inputDir)) {
                anyInputSideOpen = true;
                break;
            }
        }

        // Show beams for open sides if input is open, but don't show beam for input side (duplicate beam)
        for (Direction dir : Direction.values()) {
            ProjectionBeam beam = getBeam(dir);
            beam.setShowBeam(anyInputSideOpen && !getSideInput(dir) && getSideOpen(dir));
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, PrismBlockEntity be) {
        //be.createBeamParticles(world, pos, state, be);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, PrismBlockEntity pbe) {
        pbe.recalculateAllBeams();
    }


}
