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

    private boolean northActive = false;
    private boolean eastActive = false;
    private boolean southActive = false;
    private boolean westActive = false;
    private boolean topActive = false;
    private boolean bottomActive = false;

    private boolean northGlow = false;
    private boolean eastGlow = false;
    private boolean southGlow = false;
    private boolean westGlow = false;
    private boolean topGlow = false;
    private boolean bottomGlow = false;

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

        if (be.showRadius) {
            createParticleRadiusBorder(world, pos, be.radius, 150);
        }

        // TODO: Rune particles?
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {
        Random random = new Random();

        int randomCheck = random.nextInt(40);
        if (randomCheck == 1) {
            //be.checkForRunes();
        }

        boolean showRuneRadius = false;
        for (AbstractRune rune : be.runesPresentLastCheck) {
            rune.onServerTick(world, pos, be);

            if (rune.showRadiusEffect()) {
                showRuneRadius = true;
            }
        }

        be.showRadius = showRuneRadius;
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

    public void setFaceActive(Direction direction, boolean active) {
        switch (direction) {
            case NORTH -> this.northActive = active;
            case EAST -> this.eastActive = active;
            case SOUTH -> this.southActive = active;
            case WEST -> this.westActive = active;
            case UP -> this.topActive = active;
            case DOWN -> this.bottomActive = active;
        }

        updateListeners();
    }

    public boolean getFaceActive(Direction direction) {
        switch (direction) {
            case NORTH -> {
                return northActive;
            }
            case EAST -> {
                return eastActive;
            }
            case SOUTH -> {
                return southActive;
            }
            case WEST -> {
                return westActive;
            }
            case UP -> {
                return topActive;
            }
            case DOWN -> {
                return bottomActive;
            }
            default -> {
                return false;
            }
        }
    }

    public void setFaceGlow(Direction direction, boolean active) {
        switch (direction) {
            case NORTH -> this.northGlow = active;
            case EAST -> this.eastGlow = active;
            case SOUTH -> this.southGlow = active;
            case WEST -> this.westGlow = active;
            case UP -> this.topGlow = active;
            case DOWN -> this.bottomGlow = active;
        }

        updateListeners();
    }

    public boolean getFaceGlow(Direction direction) {
        switch (direction) {
            case NORTH -> {
                return northGlow;
            }
            case EAST -> {
                return eastGlow;
            }
            case SOUTH -> {
                return southGlow;
            }
            case WEST -> {
                return westGlow;
            }
            case UP -> {
                return topGlow;
            }
            case DOWN -> {
                return bottomGlow;
            }
            default -> {
                return false;
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

        tag.putBoolean("north_active", northActive);
        tag.putBoolean("east_active", eastActive);
        tag.putBoolean("south_active", southActive);
        tag.putBoolean("west_active", westActive);
        tag.putBoolean("top_active", topActive);
        tag.putBoolean("bottom_active", bottomActive);

        tag.putBoolean("north_glow", northGlow);
        tag.putBoolean("east_glow", eastGlow);
        tag.putBoolean("south_glow", southGlow);
        tag.putBoolean("west_glow", westGlow);
        tag.putBoolean("top_glow", topGlow);
        tag.putBoolean("bottom_glow", bottomGlow);

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

        northActive = tag.getBoolean("north_active");
        eastActive = tag.getBoolean("east_active");
        southActive = tag.getBoolean("south_active");
        westActive = tag.getBoolean("west_active");
        topActive = tag.getBoolean("top_active");
        bottomActive = tag.getBoolean("bottom_active");

        northGlow = tag.getBoolean("north_glow");
        eastGlow = tag.getBoolean("east_glow");
        southGlow = tag.getBoolean("south_glow");
        westGlow = tag.getBoolean("west_glow");
        topGlow = tag.getBoolean("top_glow");
        bottomGlow = tag.getBoolean("bottom_glow");
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
        this.world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.REDRAW_ON_MAIN_THREAD);
    }
    //endregion

    /**
     * All the logic for each rune if detected. Called randomly every 2 seconds or so.
     */
    public void checkForRunes() {
        List<AbstractRune> foundRunes = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            int[] faceArray = getFaceArray(dir);

            AbstractRune rune = RuneManager.getRuneFromArray(faceArray);
            TreeStructure tree = TreeManager.getTreeStructureFromBlock(pos, world);

            if (rune != null && tree.isNatural() && getFaceActive(dir)) {
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
