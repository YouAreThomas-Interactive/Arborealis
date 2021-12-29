package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.lwjgl.system.CallbackI;

import java.util.*;

public class CarvedWoodEntity extends BlockEntity {

    private String logID = "";

    private int[] faceNorth = new int[49];
    private int[] faceEast = new int[49];
    private int[] faceSouth = new int[49];
    private int[] faceWest = new int[49];
    private int[] faceTop = new int[49];
    private int[] faceBottom = new int[49];

    private boolean runesActive = false;

    private boolean northCatalysed = false;
    private boolean eastCatalysed = false;
    private boolean southCatalysed = false;
    private boolean westCatalysed = false;
    private boolean topCatalysed = false;
    private boolean bottomCatalysed = false;

    private boolean northGlow = false;
    private boolean eastGlow = false;
    private boolean southGlow = false;
    private boolean westGlow = false;
    private boolean topGlow = false;
    private boolean bottomGlow = false;

    // Radius
    private final int BASE_RADIUS = 10;
    public int radius = 10;
    private boolean showRadius;

    private List<AbstractRune> runesPresentLastCheck = new ArrayList<>();

    public Timer chopTimer = new Timer();


    private boolean reload = true;

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
        if (be.reload) {
            be.runesPresentLastCheck.clear();
            be.reload = false;
        }

        int randomCheck = Arborealis.RANDOM.nextInt(40);
        if (randomCheck == 1) {
            if (be.hasWorld()) {
                TreeManager.checkLifeForce(world, pos);
                be.checkForRunes();
            }
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

    public void markRune(Direction dir, int[] pattern) {
        int[] currentPattern = getFaceArray(dir);
        int[] combinedPattern = new int[49];

        for (int i = 0; i < pattern.length; i++) {
            if (currentPattern[i] == 1) {
                combinedPattern[i] = 1;
            } else if (pattern[i] == 2) {
                combinedPattern[i] = 2;
            }
        }

        setFaceArray(dir, combinedPattern);
    }

    //region NBT Shenanigans
    public void setLogID(String logID) {
        this.logID = logID;
        this.markDirty();
    }

    public String getLogID() {
        return logID;
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

        this.markDirty();
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

    public void setRunesActive(boolean active) {
        runesActive = active;
        this.markDirty();
    }

    public boolean getRunesActive() {
        return runesActive;
    }

    public void setFaceCatalysed(Direction direction, boolean active) {
        switch (direction) {
            case NORTH -> this.northCatalysed = active;
            case EAST -> this.eastCatalysed = active;
            case SOUTH -> this.southCatalysed = active;
            case WEST -> this.westCatalysed = active;
            case UP -> this.topCatalysed = active;
            case DOWN -> this.bottomCatalysed = active;
        }

        this.markDirty();
    }

    public boolean getFaceCatalysed(Direction direction) {
        switch (direction) {
            case NORTH -> {
                return northCatalysed;
            }
            case EAST -> {
                return eastCatalysed;
            }
            case SOUTH -> {
                return southCatalysed;
            }
            case WEST -> {
                return westCatalysed;
            }
            case UP -> {
                return topCatalysed;
            }
            case DOWN -> {
                return bottomCatalysed;
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

        this.markDirty();
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
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putString("log_id", logID);

        tag.putIntArray("face_north", faceNorth);
        tag.putIntArray("face_east", faceEast);
        tag.putIntArray("face_south", faceSouth);
        tag.putIntArray("face_west", faceWest);
        tag.putIntArray("face_top", faceTop);
        tag.putIntArray("face_bottom", faceBottom);

        tag.putBoolean("north_catalysed", northCatalysed);
        tag.putBoolean("east_catalysed", eastCatalysed);
        tag.putBoolean("south_catalysed", southCatalysed);
        tag.putBoolean("west_catalysed", westCatalysed);
        tag.putBoolean("top_catalysed", topCatalysed);
        tag.putBoolean("bottom_catalysed", bottomCatalysed);

        tag.putBoolean("runes_active", runesActive);
        tag.putBoolean("show_radius", showRadius);

        tag.putBoolean("north_glow", northGlow);
        tag.putBoolean("east_glow", eastGlow);
        tag.putBoolean("south_glow", southGlow);
        tag.putBoolean("west_glow", westGlow);
        tag.putBoolean("top_glow", topGlow);
        tag.putBoolean("bottom_glow", bottomGlow);

        tag.put("runes_list", ArborealisNbt.serializeRuneList(runesPresentLastCheck));
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        logID = tag.getString("log_id");

        faceNorth = tag.getIntArray("face_north");
        faceEast = tag.getIntArray("face_east");
        faceSouth = tag.getIntArray("face_south");
        faceWest = tag.getIntArray("face_west");
        faceTop = tag.getIntArray("face_top");
        faceBottom = tag.getIntArray("face_bottom");

        northCatalysed = tag.getBoolean("north_catalysed");
        eastCatalysed = tag.getBoolean("east_catalysed");
        southCatalysed = tag.getBoolean("south_catalysed");
        westCatalysed = tag.getBoolean("west_catalysed");
        topCatalysed = tag.getBoolean("top_catalysed");
        bottomCatalysed = tag.getBoolean("bottom_catalysed");

        runesActive = tag.getBoolean("runes_active");
        showRadius = tag.getBoolean("show_radius");

        northGlow = tag.getBoolean("north_glow");
        eastGlow = tag.getBoolean("east_glow");
        southGlow = tag.getBoolean("south_glow");
        westGlow = tag.getBoolean("west_glow");
        topGlow = tag.getBoolean("top_glow");
        bottomGlow = tag.getBoolean("bottom_glow");

        runesPresentLastCheck = ArborealisNbt.deserializeRuneList(tag.getList("runes_list", NbtElement.COMPOUND_TYPE));

        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.REDRAW_ON_MAIN_THREAD);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }
    //endregion

    /**
     * All the logic for each rune if detected. Called randomly every 2 seconds or so.
     */
    public void checkForRunes() {
        List<AbstractRune> foundRunes = new ArrayList<>();
        TreeStructure tree = TreeManager.getTreeStructureFromBlock(pos, world);

        for (Direction dir : Direction.values()) {
            if (getFaceCatalysed(dir) && getRunesActive()) {
                int[] faceArray = getFaceArray(dir);

                AbstractRune rune = RuneManager.getRuneFromArray(faceArray);

                if (rune != null && tree.isNatural()) {
                    if (!runesPresentLastCheck.contains(rune)) {
                        rune.onRuneFound(world, pos, this); // If rune appears for the first time, execute onRuneFound(...)
                    }

                    foundRunes.add(rune);
                }
            }
        }

        for (AbstractRune rune : runesPresentLastCheck) {
            if (!foundRunes.contains(rune)) {
                rune.onRuneLost(world, pos, this);
            }
        }

        runesPresentLastCheck = foundRunes;
        this.markDirty();
    }

    private static void createParticleRadiusBorder(World world, BlockPos pos, float radius, int numberOfPoints) {
        Vec2f[] points = new Vec2f[numberOfPoints];
        Random random = world.random;

        // Create the circle of points to display particles at
        for (int i = 0; i < numberOfPoints; ++i)
        {
            double angle = Math.toRadians(((double) i / numberOfPoints) * 360d);

            points[i] = new Vec2f(
                    (float)Math.cos(angle) * (radius + 1) + 0.5f,
                    (float)Math.sin(angle) * (radius + 1) + 0.5f
            );
        }

        for (Vec2f point : points) {
            int randomParticle = random.nextInt(100);

            // With a 1% chance to display a particle...
            if (randomParticle == 1) {
                BlockPos particlePos = new BlockPos(pos.getX() + point.x + 0.5f, pos.getY(), pos.getZ() + point.y + 0.5f);

                // Check from 10 blocks above rune to 10 below
                for (int y = 10; y > -10; y--) {
                    BlockPos testPos = particlePos.withY(pos.getY() + y);

                    // If the block is air or a plant, keep going
                    if (world.getBlockState(testPos).isOf(Blocks.AIR) || world.getBlockState(testPos).isIn(BlockTags.REPLACEABLE_PLANTS)) {
                        particlePos = testPos;
                    } else {
                        break;
                    }
                }

                world.addParticle(ParticleTypes.COMPOSTER, particlePos.getX(), particlePos.getY(), particlePos.getZ(), -0.5 + random.nextFloat(), random.nextFloat() / 3, -0.5 + random.nextFloat());
            }
        }
    }
}
