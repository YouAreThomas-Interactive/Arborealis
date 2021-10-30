package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.blocks.CarvedWood;
import com.youarethomas.arborealis.util.*;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

import static com.youarethomas.arborealis.util.ArborealisUtil.applyStatusEffectsToEntities;
import static com.youarethomas.arborealis.util.ArborealisUtil.getPlayersInRadius;

public class CarvedWoodEntity extends BlockEntity implements BlockEntityClientSerializable {

    private String logID = "";

    private int[] faceNorth = new int[49];
    private int[] faceEast = new int[49];
    private int[] faceSouth = new int[49];
    private int[] faceWest = new int[49];

    private boolean applyStatus = false;

    // Radius
    private final int BASE_RADIUS = 10;
    public int radius = 12;
    private boolean showRadius;

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {
        if (be.getShowRadius()) {
            createParticleRadiusBorder(world, pos, be.radius, 150);
        }

        // TODO: Rune particles?
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {
        Random random = new Random();

        if (be.applyStatus) {
            applyStatusEffectsToEntities(getPlayersInRadius(world, pos, be.radius), StatusEffects.SPEED);
        }

        int randomCheck = random.nextInt(40);
        if (randomCheck == 1) {
            be.checkForRunes();
        }
    }

    public void performCarve() {
        setFaceArray(Direction.NORTH, Arrays.stream(getFaceArray(Direction.NORTH)).map(i -> i == 2 ? 1 : i).toArray());
        setFaceArray(Direction.EAST, Arrays.stream(getFaceArray(Direction.EAST)).map(i -> i == 2 ? 1 : i).toArray());
        setFaceArray(Direction.SOUTH, Arrays.stream(getFaceArray(Direction.SOUTH)).map(i -> i == 2 ? 1 : i).toArray());
        setFaceArray(Direction.WEST, Arrays.stream(getFaceArray(Direction.WEST)).map(i -> i == 2 ? 1 : i).toArray());

        checkForRunes();
    }

    //region NBT Shenanagins
    public void setLogID(String logID) {
        this.logID = logID;
        updateListeners();
    }

    public String getLogID() {
        return logID;
    }

    public void setShowRadius(boolean showRadius) {
        this.showRadius = showRadius;
        markDirty();
    }

    public boolean getShowRadius() {
        return showRadius;
    }

    public void setFaceArray(Direction direction, int[] array) {
        switch (direction) {
            case NORTH -> this.faceNorth = array;
            case EAST -> this.faceEast = array;
            case SOUTH -> this.faceSouth = array;
            case WEST -> this.faceWest = array;
        }

        updateListeners();
    }

    public int[] getFaceArray(Direction direction) {
        switch (direction) {
            case NORTH -> {
                return faceNorth;
            }
            case EAST -> {
                return faceEast;
            }
            case SOUTH -> {
                return faceSouth;
            }
            case WEST -> {
                return faceWest;
            }
            default -> {
                return null;
            }
        }
    }
    //endregion

    // Serialize the BlockEntity - storing data
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putString("log_id", logID);
        tag.putBoolean("show_radius", showRadius);
        tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);

        return tag;
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        logID = tag.getString("log_id");
        showRadius = tag.getBoolean("show_radius");
        faceNorth = tag.getIntArray("face_north");
        faceEast = tag.getIntArray("face_east");
        faceSouth = tag.getIntArray("face_south");
        faceWest = tag.getIntArray("face_west");
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
        updateListeners();
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    private void updateListeners() {
        // This method is the magic that makes the whole carving system work. No touchy
        this.markDirty();
        if (this.world != null) {
            this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }

    /**
     * All the logic for each rune if detected. Called randomly every 2 seconds or so.
     */
    private void checkForRunes() {
        // Create array of face arrays to iterate through
        int[][] directions = new int[][] { getFaceArray(Direction.NORTH), getFaceArray(Direction.EAST), getFaceArray(Direction.SOUTH), getFaceArray(Direction.WEST) };

        List<String> foundRunes = new ArrayList<>();

        for (int[] faceArray : directions) {
            Rune rune = RuneManager.getRuneFromArray(faceArray);
            TreeStructure tree = TreeManager.getTreeStructureFromBlock(pos, world);

            if (rune != null && tree.isNatural())
            {
                if (RuneManager.faceContainsRune(faceArray, "light") && !foundRunes.contains("light")) {
                    world.setBlockState(pos, world.getBlockState(pos).with(CarvedWood.LIT, true));
                    foundRunes.add("light");
                } else if (RuneManager.faceContainsRune(faceArray, "test") && !foundRunes.contains("test")) {
                    applyStatus = true;
                    setShowRadius(true);
                    foundRunes.add("test");
                } else if (RuneManager.faceContainsRune(faceArray, "chop") && !foundRunes.contains("chop")) {
                    // TODO: Start chop timer
                    foundRunes.add("chop");
                }
            }
        }

        // Reset effects when runes aren't found
        if (!foundRunes.contains("light")) {
            world.setBlockState(pos, world.getBlockState(pos).with(CarvedWood.LIT, false));
        }
        if (!foundRunes.contains("test")) {
            applyStatus = false;
            setShowRadius(false);
        }
        if (!foundRunes.contains("chop")) {
            // TODO: Stop chop timer
        }

        updateListeners();
    }



    private static void createParticleRadiusBorder(World world, BlockPos pos, float radius, int numberOfPoints) {
        Vec2f[] points = new Vec2f[numberOfPoints];
        Random random = world.random;

        for (int i = 0; i < numberOfPoints; ++i)
        {
            double angle = Math.toRadians(((double) i / numberOfPoints) * 360d);

            points[i] = new Vec2f(
                    (float)Math.cos(angle) * radius,
                    (float)Math.sin(angle) * radius
            );
        }

        for (Vec2f point : points) {
            int randomParticle = random.nextInt(100);
            // TODO: Add in a nice way to find the ground and great the particles above that
            if (randomParticle == 1) {
                world.addParticle(ParticleTypes.COMPOSTER, pos.getX() + point.x + 0.5f, pos.down().getY(), pos.getZ() + point.y + 0.5f, -0.5 + random.nextFloat(), random.nextFloat() / 3, -0.5 + random.nextFloat());
            }
        }
    }
}
