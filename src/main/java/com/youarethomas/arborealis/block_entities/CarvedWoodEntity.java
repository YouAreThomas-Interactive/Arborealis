package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.blocks.CarvedWood;
import com.youarethomas.arborealis.util.Rune;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.util.TreeManager;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class CarvedWoodEntity extends BlockEntity implements BlockEntityClientSerializable {

    private String logID = "";

    private int[] faceNorth = new int[49];
    private int[] faceEast = new int[49];
    private int[] faceSouth = new int[49];
    private int[] faceWest = new int[49];

    private boolean applyStatus = false;

    // Tick Counter
    private static int ticks = 0;
    private static final int TICKS_FOR_RUNE_CHECK = 100;

    public CarvedWoodEntity(BlockPos pos, BlockState state) {
        super(Arborealis.CARVED_WOOD_ENTITY, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {

    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CarvedWoodEntity be) {

        if (be.applyStatus) {
            //System.out.println("Success!");
            applyStatusEffectsToEntities(getPlayersInRadius(world, pos, 10), pos, StatusEffects.SPEED);
        }

        // Rune checking
        ticks++;

        if (ticks > TICKS_FOR_RUNE_CHECK) {
            System.out.println("Performed valid rune check");
            be.checkForRunes();
            ticks = 0;
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

    private void checkForRunes() {
        // Create array of face arrays to iterate through
        int[][] directions = new int[][] { getFaceArray(Direction.NORTH), getFaceArray(Direction.EAST), getFaceArray(Direction.SOUTH), getFaceArray(Direction.WEST) };

        List<String> foundRunes = new ArrayList<>();

        for (int[] faceArray : directions) {
            Rune rune = RuneManager.getRuneFromArray(faceArray);

            if (rune != null && TreeManager.getTreeStructureFromBlock(pos, world).isNatural())
            {
                if (arrayContainsRune(faceArray, "light")) {
                    world.setBlockState(pos, world.getBlockState(pos).with(CarvedWood.LIT, true));
                    foundRunes.add("light");
                } else if (arrayContainsRune(faceArray, "test")) {
                    applyStatus = true;
                    foundRunes.add("test");
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

        updateListeners();
    }

    private boolean arrayContainsRune(int[] faceArray, String runeName) {
        return Objects.equals(Objects.requireNonNull(RuneManager.getRuneFromArray(faceArray)).name, runeName);
    }

    private static List<LivingEntity> getPlayersInRadius(World world, BlockPos pos, int radius) {
        Box box = Box.from(new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)).expand(radius + 1);

        List<LivingEntity> entities = new ArrayList<>();

        for (LivingEntity entity : world.getNonSpectatingEntities(LivingEntity.class, box)) {
            if (pos.isWithinDistance(entity.getBlockPos(), radius + 1)) {
                entities.add(entity);
            }
        }

        return entities;
    }

    private static void applyStatusEffectsToEntities(List<LivingEntity> entityList, BlockPos pos, StatusEffect effect) {
        if (!entityList.isEmpty()) {
            for (LivingEntity playerEntity : entityList) {
                playerEntity.addStatusEffect(new StatusEffectInstance(effect, 5, 0, true, false, true));
            }
        }
    }

}
