package com.youarethomas.arborealis.util;

import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A tree object
 */
public class TreeStructure {
    private static final String TREE_STRUCTURE_LEAVES_NBT = "arborealis.treemanager.treestructure.leaves";
    private static final String TREE_STRUCTURE_LOGS_NBT = "arborealis.treemanager.treestructure.logs";

    public static final int NAT_LEAVES_MIN = 12;
    public static final int LOGS_MIN = 2;

    public boolean isPumpkin = false;

    public HashSet<BlockPos> leaves = new HashSet<>();
    public HashSet<BlockPos> logs = new HashSet<>();
    public int logCount() {
        return logs.size();
    }

    /**
     * Returns true if a tree is deemed to be suitably natural;
     */
    public boolean isNatural() {
        // Pumpkin always natural :P
        return isPumpkin || (leaves.size() >= NAT_LEAVES_MIN && logCount() >= LOGS_MIN);
    }

    public boolean isEmpty() {
        return logs.isEmpty();
    }

    public void replaceLogStructure(World world) {
        for (BlockPos pos : logs) {
            world.breakBlock(pos, true);
        }
    }

    public void replaceLeafStructure(World world) {
        for (BlockPos pos : leaves) {
            world.breakBlock(pos, true);
        }
    }

    public void chopTreeStructure(World world) {
        if(isNatural()) {
            replaceLogStructure(world);
            replaceLeafStructure(world);
        }
    }

    public boolean isPosInTree(BlockPos blockPos) {
        for (BlockPos pos : logs) {
            if (pos.equals(blockPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts the tree structure into NBT data.
     *
     * @param structure The tree structure being converted.
     * @return The NBT object representation of this tree structure.
     */
    public static NbtCompound toNbt(TreeStructure structure) {
        NbtCompound nbt = new NbtCompound();

        // Creates the NBT list for the leaves.
        NbtList nbtLeafList = new NbtList();

        for(BlockPos pos : structure.leaves) {
            nbtLeafList.add(NbtHelper.fromBlockPos(pos));
        }

        nbt.put(TREE_STRUCTURE_LEAVES_NBT, nbtLeafList);

        // Creates the NBT list for the logs.
        NbtList nbtLogList = new NbtList();

        for(BlockPos pos : structure.logs) {
            nbtLogList.add(NbtHelper.fromBlockPos(pos));
        }

        nbt.put(TREE_STRUCTURE_LOGS_NBT, nbtLogList);

        return nbt;
    }

    /**
     * Creates a new tree structure with the given NBT data.
     *
     * @param nbt The given NBT data.
     * @return The generated tree structure, null if it could not be determined.
     */
    public static TreeStructure fromNbt(NbtCompound nbt) {
        TreeStructure structure = null;

        if(nbt.contains(TREE_STRUCTURE_LEAVES_NBT) && nbt.contains(TREE_STRUCTURE_LOGS_NBT)) {
            structure = new TreeStructure();

            // Generates the set of leaves.
            NbtList nbtLeafList = nbt.getList(TREE_STRUCTURE_LEAVES_NBT, NbtElement.COMPOUND_TYPE);

            for (NbtElement element : nbtLeafList) {
                structure.leaves.add(NbtHelper.toBlockPos((NbtCompound) element));
            }

            // Generates the set of logs.
            NbtList nbtLogList = nbt.getList(TREE_STRUCTURE_LOGS_NBT, NbtElement.COMPOUND_TYPE);

            for (NbtElement element : nbtLogList) {
                structure.logs.add(NbtHelper.toBlockPos((NbtCompound) element));
            }
        }

        return structure;
    }
}
