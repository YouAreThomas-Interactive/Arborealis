package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.blocks.CarvedWood;
import com.youarethomas.arborealis.util.Rune;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CarvedWoodEntity extends BlockEntity implements BlockEntityClientSerializable, RenderAttachmentBlockEntity {

    private String logID = "";

    private int[] faceNorth = new int[49];
    private int[] faceEast = new int[49];
    private int[] faceSouth = new int[49];
    private int[] faceWest = new int[49];

    private boolean applyStatus = false;

    // Radius logic
    private int radius = 12;
    private boolean showRadius = false;

    // Timers
    private Timer chopTimer;

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {
        if (be.showRadius) {
            createParticleRadiusBorder(world, pos, be.radius, 150);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {
        Random random = new Random();

        if (be.applyStatus) {
            //System.out.println("Success!");
            applyStatusEffectsToEntities(getPlayersInRadius(world, pos, 10, be), pos, StatusEffects.SPEED);
        }

        int randomCheck = random.nextInt(20);
        if (randomCheck == 1) {
            System.out.println("Performed valid rune check");
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

    public void setLogID(String logID) {
        this.logID = logID;
        updateListeners();
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

    // Serialize the BlockEntity - storing data
    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.putString("log_id", logID);
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

    private Rune checkForRunes() {
        // Create array of face arrays to iterate through
        int[][] directions = new int[][] { getFaceArray(Direction.NORTH), getFaceArray(Direction.EAST), getFaceArray(Direction.SOUTH), getFaceArray(Direction.WEST) };

        List<String> foundRunes = new ArrayList<>();

        for (int[] faceArray : directions) {
            Rune rune = RuneManager.getRuneFromArray(faceArray);
            TreeStructure tree = TreeManager.getTreeStructureFromBlock(pos, world);

            if (rune != null && tree.isNatural())
            {
                if (arrayContainsRune(faceArray, "light") && !foundRunes.contains("light")) {
                    world.setBlockState(pos, world.getBlockState(pos).with(CarvedWood.LIT, true));
                    foundRunes.add("light");
                } else if (arrayContainsRune(faceArray, "test") && !foundRunes.contains("test")) {
                    applyStatus = true;
                    showRadius = true;
                    foundRunes.add("test");
                } else if (arrayContainsRune(faceArray, "chop") && !foundRunes.contains("chop")) {
                    chopTimer = new Timer();

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
        }
        if (!foundRunes.contains("chop")) {
            // Stop chop timer
        }

        updateListeners();

        return null;
    }

    private boolean arrayContainsRune(int[] faceArray, String runeName) {
        return Objects.equals(Objects.requireNonNull(RuneManager.getRuneFromArray(faceArray)).name, runeName);
    }

    private static List<LivingEntity> getPlayersInRadius(World world, BlockPos pos, int radius, CarvedWoodEntity entity) {
        Box box = Box.from(new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)).expand(radius + 1);

        List<LivingEntity> entities = new ArrayList<>();

        for (LivingEntity livingEntity : world.getNonSpectatingEntities(LivingEntity.class, box)) {
            if (isWithinEffectRadius(livingEntity.getBlockPos(), entity)) {
                entities.add(livingEntity);
            }
        }

        return entities;
    }

    public static boolean isWithinEffectRadius(Vec3i vec, CarvedWoodEntity entity) {
        double x = (double)vec.getX() + 0.05 - entity.pos.getX();
        double z = (double)vec.getZ() + 0.05 - entity.pos.getZ();

        double distance = x * x + z * z;

        return distance < entity.radius * entity.radius;
    }

    private static void applyStatusEffectsToEntities(List<LivingEntity> entityList, BlockPos pos, StatusEffect effect) {
        if (!entityList.isEmpty()) {
            for (LivingEntity playerEntity : entityList) {
                playerEntity.addStatusEffect(new StatusEffectInstance(effect, 5, 0, true, false, true));
            }
        }
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
                world.addParticle(ParticleTypes.COMPOSTER, pos.getX() + point.x, pos.down().getY(), pos.getZ() + point.y, -0.5 + random.nextFloat(), random.nextFloat() / 3, -0.5 + random.nextFloat());
            }
        }
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return null;
    }
}
