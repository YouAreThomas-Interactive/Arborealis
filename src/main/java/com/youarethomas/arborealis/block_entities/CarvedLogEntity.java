package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.*;

public class CarvedLogEntity extends BlockEntity {

    private BlockState logState = Blocks.OAK_LOG.getDefaultState();
    private boolean runesActive = true;
    private boolean reload = true;

    private final int BASE_RADIUS = 10;
    public int radius = 10;
    private boolean showRadius;

    public List<Rune> runesPresentLastCheck = new ArrayList<>();
    public Timer chopTimer = new Timer();

    public Map<Direction, CarvedLogEntityFace> carvedFaces = new HashMap<>() {{
        put(Direction.UP, new CarvedLogEntityFace());
        put(Direction.DOWN, new CarvedLogEntityFace());
        put(Direction.NORTH, new CarvedLogEntityFace());
        put(Direction.EAST, new CarvedLogEntityFace());
        put(Direction.SOUTH, new CarvedLogEntityFace());
        put(Direction.WEST, new CarvedLogEntityFace());
    }};

    public CarvedLogEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_LOG_ENTITY, pos, state);
    }

    public CarvedLogEntity(BlockEntityType type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CarvedLogEntity be) {
        for (Rune rune : be.getRunesPresentLastCheck()) {
            if (rune != null) {
                rune.onClientTick(world, pos, be);
            }
        }

        if (be.getShowRadius()) {
            createParticleRadiusBorder(world, pos, be.radius, 150);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CarvedLogEntity be) {
        if (be.reload) {
            for (Rune rune : be.getRunesPresentLastCheck()) {
                rune.onRuneFound(world, pos, be);
            }
            be.reload = false;
        }

        for (Rune rune : be.getRunesPresentLastCheck()) {
            rune.onServerTick(world, pos, be);
        }
    }

    public void performCarve() {
        for (Direction dir : Direction.values()) {
            setFaceArray(dir, Arrays.stream(getFaceArray(dir)).map(i -> i == 2 ? 1 : i).toArray());
        }

        checkForRunes();
    }

    public void projectLightRune(Direction dir, int[] runeToProject) {
        int[] removeOldLight = Arrays.stream(getFaceArray(dir)).map(i -> i == 3 ? 0 : i).toArray(); // Set all existing light to uncarved
        int[] projectedPattern = removeOldLight.clone();

        for (int i = 0; i < runeToProject.length; i++) {
            projectedPattern[i] = (projectedPattern[i] == 0 && runeToProject[i] == 2) ? 3 : projectedPattern[i]; // Set all uncarved to light
            projectedPattern[i] = (projectedPattern[i] == 2 && runeToProject[i] == 2) ? 3 : projectedPattern[i]; // Set all highlighted to light
        }

        setFaceArray(dir, projectedPattern);

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
    public boolean getShowRadius() { return showRadius; }
    public void setShowRadius(boolean radius) {
        this.showRadius = radius;
        markDirty();
    }

    public BlockState getLogState() {
        return logState;
    }
    public void setLogState(BlockState logState) {
        this.logState = logState;
        this.markDirty();
    }

    public boolean areRunesActive() {
        return runesActive;
    }
    public void setRunesActive(boolean active) {
        runesActive = active;
        this.markDirty();
    }

    public int[] getFaceArray(Direction direction) {
        return carvedFaces.get(direction).getFaceArray();
    }
    public void setFaceArray(Direction direction, int[] array) {
        CarvedLogEntityFace face = carvedFaces.get(direction);
        face.setFaceArray(array);
        carvedFaces.replace(direction, face);
        this.markDirty();
    }

    public boolean isFaceCatalysed(Direction direction) {
        return carvedFaces.get(direction).isCatalysed();
    }
    public void setFaceCatalysed(Direction direction, boolean catalysed) {
        CarvedLogEntityFace face = carvedFaces.get(direction);
        face.setCatalysed(catalysed);
        carvedFaces.replace(direction, face);
        this.markDirty();
    }

    public boolean isFaceEmissive(Direction direction) {
        return carvedFaces.get(direction).isEmissive();
    }
    public void setFaceEmissive(Direction direction, boolean emissive) {
        CarvedLogEntityFace face = carvedFaces.get(direction);
        face.setEmissive(emissive);
        carvedFaces.replace(direction, face);
        this.markDirty();
    }

    public Rune getFaceRune(Direction direction) {
        return carvedFaces.get(direction).getFaceRune();
    }
    public void setFaceRune(Direction direction, Rune rune) {
        CarvedLogEntityFace face = carvedFaces.get(direction);
        face.setFaceRune(rune);
        carvedFaces.replace(direction, face);
        this.markDirty();
    }
    public List<Rune> getRunesPresentLastCheck() {
        return runesPresentLastCheck;
    }
    public void setRunesPresentLastCheck(List<Rune> runeList) {
        runesPresentLastCheck = runeList.stream().toList();
        markDirty();
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.put("log_state", NbtHelper.fromBlockState(logState));
        tag.putBoolean("runes_active", runesActive);
        tag.putBoolean("show_radius", showRadius);
        tag.put("runes_list", ArborealisNbt.serializeRuneList(runesPresentLastCheck));

        for (Map.Entry<Direction, CarvedLogEntityFace> face : carvedFaces.entrySet()) {
            tag.put(face.getKey().getName(), face.getValue().serialize());
        }
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        logState = NbtHelper.toBlockState(tag.getCompound("log_state"));
        runesActive = tag.getBoolean("runes_active");
        showRadius = tag.getBoolean("show_radius");
        runesPresentLastCheck = ArborealisNbt.deserializeRuneList(tag.getList("runes_list", NbtElement.COMPOUND_TYPE));

        for (Direction face : Direction.values()) {
            carvedFaces.replace(face, new CarvedLogEntityFace(tag, face));
        }

        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL | Block.FORCE_STATE);
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
        if (world != null && !world.isClient()) {
            System.out.println("Checking for runes");
            List<Rune> foundRunes = new ArrayList<>();

            for (Direction dir : Direction.values()) {
                if (isFaceCatalysed(dir) && areRunesActive()) {
                    int[] faceArray = getFaceArray(dir);

                    Rune rune = RuneManager.getRuneFromArray(faceArray);

                    if (rune != null) {
                        setFaceRune(dir, rune); // Save rune to BlockEntity

                        if (!getRunesPresentLastCheck().contains(rune)) {
                            System.out.println(rune.name + " was found.");
                            rune.onRuneFound(world, pos, this); // If rune appears for the first time, execute onRuneFound(...)
                        }

                        foundRunes.add(rune);
                    } else {
                        setFaceRune(dir, null); // Clear rune if not recognised
                    }
                }
            }

            for (Rune rune : getRunesPresentLastCheck()) {
                if (!foundRunes.contains(rune)) {
                    System.out.println(rune.name + " was lost.");
                    rune.onRuneLost(world, pos, this);
                }
            }

            setRunesPresentLastCheck(foundRunes);
            this.markDirty();

            boolean displayRadiusParticles = false;
            for (Rune rune : getRunesPresentLastCheck()) {
                if (rune.showRadiusEffect())
                    displayRadiusParticles = true;
            }

            setShowRadius(displayRadiusParticles);
        }
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
