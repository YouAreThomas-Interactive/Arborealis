package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.*;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

public class CarvedWoodEntity extends BlockEntity implements BlockEntityClientSerializable {

    private String logID = "";

    private int[] faceNorth = new int[49];
    private int[] faceEast = new int[49];
    private int[] faceSouth = new int[49];
    private int[] faceWest = new int[49];
    private int[] faceTop = new int[49];
    private int[] faceBottom = new int[49];

    // Radius
    private final int BASE_RADIUS = 10;
    public int radius = 12;
    private boolean showRadius;

    private List<AbstractRune> runesPresentLastCheck = new ArrayList<>();

    public Timer chopTimer = new Timer();

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {
        for (AbstractRune rune : be.runesPresentLastCheck) {
            rune.onClientTick(world, pos, be);
        }

        if (be.getShowRadius()) {
            createParticleRadiusBorder(world, pos, be.radius, 150);
        }

        // TODO: Rune particles?
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {
        Random random = new Random();



        int randomCheck = random.nextInt(40);
        if (randomCheck == 1) {
            be.checkForRunes();
        }

        for (AbstractRune rune : be.runesPresentLastCheck) {
            rune.onServerTick(world, pos, be);

            if (rune.showRadiusEffect()) {
                be.setShowRadius(true);
            }
        }
    }

    public void performCarve() {
        for (Direction dir : Direction.values()) {
            setFaceArray(dir, Arrays.stream(getFaceArray(dir)).map(i -> i == 2 ? 1 : i).toArray());
        }

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
        updateListeners();
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
            case UP -> this.faceTop = array;
            case DOWN -> this.faceBottom = array;
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
            case UP -> {
                return faceTop;
            }
            case DOWN -> {
                return faceBottom;
            }
            default -> {
                return null;
            }
        }
    }

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
        tag.putIntArray("face_top", faceTop);
        tag.putIntArray("face_bottom", faceBottom);

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
        faceTop = tag.getIntArray("face_top");
        faceBottom = tag.getIntArray("face_bottom");
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
    //endregion

    /**
     * All the logic for each rune if detected. Called randomly every 2 seconds or so.
     */
    private void checkForRunes() {
        List<AbstractRune> foundRunes = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            int[] faceArray = getFaceArray(dir);

            AbstractRune rune = RuneManager.getRuneFromArray(faceArray);
            TreeStructure tree = TreeManager.getTreeStructureFromBlock(pos, world);

            if (rune != null && tree.isNatural()) {
                if (!runesPresentLastCheck.contains(rune)) {
                    rune.onRuneFound(world, pos, this);
                }
                foundRunes.add(rune);
            }
        }

        for (AbstractRune rune : runesPresentLastCheck) {
            if (!foundRunes.contains(rune)) {
                rune.onRuneLost(world, pos, this);
            }
        }

        runesPresentLastCheck = foundRunes;

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
