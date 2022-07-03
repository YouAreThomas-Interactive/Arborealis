package com.youarethomas.arborealis.util;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Contains helper methods related to creating {@code TreeStructure} and {@code LogStructure} definition.
 * <br> May eventually contain a stored list of TreeStructures to iterate through to determine if trees stop being valid natural trees
 */
public class TreeManager extends PersistentState {

    private static final String TREE_MANAGER_NBT = "arborealis.treemanager";
    private static final String TREE_STRUCTURE_MAPPING_KEYS_NBT = "arborealis.treemanager.treestructuremapping.keys";
    private static final String TREE_STRUCTURE_MAPPING_VALUES_NBT = "arborealis.treemanager.treestructuremapping.values";
    private static final String TREE_STRUCTURE_REGISTRY_IDS_NBT = "arborealis.treemanager.treestructureregistry.keys";
    private static final String TREE_STRUCTURE_REGISTRY_VALUES_NBT = "arborealis.treemanager.treestructureregistry.values";

    private static final int LIFE_FORCE_MAX = 3;

    private Hashtable<BlockPos, String> treeStructureMapping;
    private Hashtable<String, TreeStructure> treeStructureRegistry;

    /**
     * Default constructor of the tree manager.
     */
    public TreeManager() {
        this.treeStructureMapping = new Hashtable<>();
        this.treeStructureRegistry = new Hashtable<>();
    }

    /**
     * Constructs the manager with a generated tree structure mapping.
     *
     * @param treeStructureMapping The generated tree structure mapping.
     */
    public TreeManager(Hashtable<BlockPos, String> treeStructureMapping, Hashtable<String, TreeStructure> treeStructureRegistery) {
        this.treeStructureMapping = treeStructureMapping;
        this.treeStructureRegistry = treeStructureRegistery;
    }

    public HashSet<TreeStructure> getTreeStructures() {
        return new HashSet<>(treeStructureRegistry.values());
    }

    public static boolean isTreeBlock(BlockState blockState) {
        return isLogBlock(blockState) || isLeafBlock(blockState);
    }

    public static boolean isLogBlock(BlockState blockState) {
        return blockState.isIn(BlockTags.LOGS) || blockState.isIn(Arborealis.MODIFIED_LOGS);
    }

    public static boolean isLeafBlock(BlockState blockState) {
        return blockState.isIn(BlockTags.LEAVES);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        // Handles the encoding of the block positions in the tree structure mapping.
        NbtList nbtMappingKeys = new NbtList();

        for(BlockPos pos : this.treeStructureMapping.keySet()) {
            nbtMappingKeys.add(NbtHelper.fromBlockPos(pos));
        }

        nbt.put(TREE_STRUCTURE_MAPPING_KEYS_NBT, nbtMappingKeys);

        // Handles the encoding of the tree structure IDs in the tree structure mapping.
        NbtList nbtMappingValues = new NbtList();

        for(String structureID : this.treeStructureMapping.values()) {
            nbtMappingValues.add(NbtString.of(structureID));
        }

        nbt.put(TREE_STRUCTURE_MAPPING_VALUES_NBT, nbtMappingValues);

        // Handles the encoding of the tree structure IDs in the registry.
        NbtList nbtRegistryKeys = new NbtList();

        for(String structureID : this.treeStructureRegistry.keySet()) {
            nbtRegistryKeys.add(NbtString.of(structureID));
        }

        nbt.put(TREE_STRUCTURE_REGISTRY_IDS_NBT, nbtRegistryKeys);

        // Handles the encoding of the tree structure objects in the registry.
        NbtList nbtRegistryValues = new NbtList();

        for(TreeStructure structure : this.treeStructureRegistry.values()) {
            nbtRegistryValues.add(TreeStructure.toNbt(structure));
        }

        nbt.put(TREE_STRUCTURE_REGISTRY_VALUES_NBT, nbtRegistryValues);

        return nbt;
    }

    public static TreeManager fromNbt(NbtCompound nbt) {
        Hashtable<BlockPos, String> treeStructureMapping = new Hashtable<>();
        Hashtable<String, TreeStructure> treeStructureRegistry = new Hashtable<>();

        if(nbt.contains(TREE_STRUCTURE_MAPPING_KEYS_NBT) && nbt.contains(TREE_STRUCTURE_MAPPING_VALUES_NBT)) {
            // Generates the position keys for the mapping.
            NbtList nbtPositionKeys = nbt.getList(TREE_STRUCTURE_MAPPING_KEYS_NBT, NbtElement.COMPOUND_TYPE);

            List<BlockPos> positionKeys = nbtPositionKeys.stream()
                    .map(element -> NbtHelper.toBlockPos((NbtCompound) element)).toList();

            // Generates the structure ID values for the mapping.
            NbtList nbtMappingValues = nbt.getList(TREE_STRUCTURE_MAPPING_VALUES_NBT, NbtElement.STRING_TYPE);

            List<String> mappingValues = nbtMappingValues.stream()
                    .map(element -> ((NbtString) element).asString()).toList();

            // Generates the mapping hashmap from the keys and values.
            treeStructureMapping.putAll(IntStream.range(0, positionKeys.size()).boxed()
                    .collect(Collectors.toMap(positionKeys::get, mappingValues::get)));

            // Generates the structure ID values for the mapping.
            NbtList nbtRegistryKeys = nbt.getList(TREE_STRUCTURE_REGISTRY_IDS_NBT, NbtElement.STRING_TYPE);

            List<String> registryKeys = nbtRegistryKeys.stream()
                    .map(element -> ((NbtString) element).asString()).toList();

            // Generates the tree structure values in the registry.
            NbtList nbtTreeStructureValues = nbt.getList(TREE_STRUCTURE_REGISTRY_VALUES_NBT, NbtElement.COMPOUND_TYPE);

            List<TreeStructure> treeStructureValues = nbtTreeStructureValues.stream()
                    .map(element -> TreeStructure.fromNbt((NbtCompound) element)).toList();

            // Generates the registry hashmap from the keys and values.
            treeStructureRegistry.putAll(IntStream.range(0, registryKeys.size()).boxed()
                    .collect(Collectors.toMap(registryKeys::get, treeStructureValues::get)));
        }

        return new TreeManager(treeStructureMapping, treeStructureRegistry);
    }

    public static TreeManager getManager(ServerWorld serverWorld) {
        return serverWorld.getPersistentStateManager().getOrCreate(TreeManager::fromNbt, TreeManager::new, TREE_MANAGER_NBT);
    }

    public boolean isBlockInTreeStructure(BlockPos position) {
        return treeStructureMapping.containsKey(position);
    }

    public void removeBlockFromTreeStructure(BlockState state, BlockPos pos, ServerWorld world) {
        // If the block is in an existing structure...
        if (isBlockInTreeStructure(pos)) {
            if(isLogBlock(state)) {
                deconstructTreeStructureFromBlock(pos, world);

                BlockPos.iterateOutwards(pos, 1, 1, 1).forEach(pos1 -> {
                    if (!pos1.equals(pos))
                        if (!treeStructureMapping.containsKey(pos1))
                            constructTreeStructureFromBlock(pos1.mutableCopy(), List.of(pos), world);
                });
            } else if(isLeafBlock(state)) {
                getTreeStructureFromPos(pos, world).removeBlockFromTree(pos);
                treeStructureMapping.remove(pos);
            }

            updateAllPlayers(world);
        }
    }

    /**
     * Attempt to get an existing tree structure from a given BlockPos
     * @param pos BlockPos to test
     * @return Returns a TreeStucture if found, otherwise returns null
     */
    public TreeStructure getTreeStructureFromPos(BlockPos pos, World world) {
        if(this.treeStructureMapping.containsKey(pos)) {
            // Get the structure that is stored at that position.
            return this.treeStructureRegistry.get(this.treeStructureMapping.get(pos));
        }

        return null;
    }

    public TreeStructure constructTreeStructureFromBlock(BlockPos startingPos, ServerWorld world) {
        return constructTreeStructureFromBlock(startingPos, null, world);
    }

    public TreeStructure constructTreeStructureFromBlock(BlockPos startingPos, @Nullable Collection<BlockPos> blackListPoses, ServerWorld world) {
        BlockState clickedBlock = world.getBlockState(startingPos);

        if (isTreeBlock(clickedBlock)) {
            TreeStructure structure = new TreeStructure();

            structure.logs.addAll(getTreeLogs(world, startingPos, blackListPoses)); // Add all found logs to the TreeStructure
            structure.leaves.addAll(getTreeLeaves(world, structure.logs)); // Add all the found leaves to the TreeStructure

            // Create a new ID for the tree structure.
            String structureID = UUID.randomUUID().toString();

            // Stores the information of the tree structure found
            this.treeStructureMapping.putAll(structure.logs.stream()
                    .collect(Collectors.toMap(Function.identity(), key -> structureID)));
            this.treeStructureMapping.putAll(structure.leaves.stream()
                    .collect(Collectors.toMap(Function.identity(), key -> structureID)));
            this.treeStructureRegistry.put(structureID, structure);

            updateAllPlayers(world);
            markDirty();

            return structure;
        }

        return null;
    }

    public List<BlockPos> getStructureBlocks() {
        return treeStructureMapping.keySet().stream().toList();
    }

    public void deconstructTreeStructureFromBlock(BlockPos startingPos, ServerWorld world) {
        String structureID = this.treeStructureMapping.get(startingPos);

        if(structureID != null) {
            treeStructureMapping.values().removeAll(Collections.singleton(structureID));
            treeStructureRegistry.remove(structureID);

            updateAllPlayers(world);
            markDirty();
        }
    }

    public void updateAllPlayers(ServerWorld world) {
        RegistryKey<World> worldKey = world.getRegistryKey();

        // Add the tree structure map to a packet buf
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeCollection(treeStructureMapping.keySet(), PacketByteBuf::writeBlockPos);
        buf.writeString(worldKey.getValue().toString());

        // Iterate over all players tracking a position in the world and send the packet to each player
        for (ServerPlayerEntity player : PlayerLookup.all(world.getServer())) {
            ServerPlayNetworking.send(player, ArborealisConstants.TREE_MAP_UPDATE, buf);
        }
    }

    private static TreeSet<BlockPos> getTreeLogs(World world, BlockPos startingPos, @Nullable Collection<BlockPos> blackListPoses) {
        TreeSet<BlockPos> toVisit = new TreeSet<>();
        TreeSet<BlockPos> visited = new TreeSet<>();
        BlockPos currentPos = startingPos;

        int logsCounted = 0; // Cap in case something goes horribly wrong

        do {
            // 3x3x3 cube to search around
            BlockPos scanCubeStart = currentPos.down().south().west();
            BlockPos scanCubeEnd = currentPos.up().north().east();

            visited.add(currentPos); // Add the current log into visited

            for (BlockPos pos : BlockPos.iterate(scanCubeStart, scanCubeEnd)) {
                BlockState currentBlockState = world.getBlockState(pos);

                // TODO: Find better way to identify blocks NOT to be added to the tree, may be possible to handle through block state tags/metadata.
                if(blackListPoses == null || !blackListPoses.contains(pos)) {
                    // If a log is detected that hasn't been iterated over yet, add to the list of blocks to get around to
                    if (isLogBlock(currentBlockState) && !visited.contains(pos)) {
                        toVisit.add(pos.mutableCopy()); // mutableCopy() required because Java is a tool
                    }
                }
            }

            // If there are blocks to visit, get the first block and remove it from blocks to visit
            if (toVisit.size() > 0) {
                currentPos = toVisit.pollFirst();
            }

            logsCounted++;
        } while (logsCounted < 300 && !visited.contains(currentPos)); // While there are blocks left to visit, keep going
        //TODO: Add log count cap to config

        return visited;
    }

    private static TreeSet<BlockPos> getTreeLeaves(World world, HashSet<BlockPos> logSet) {
        TreeSet<BlockPos> visited = new TreeSet<>();

        //TODO: Add leaf range to config
        int range = 6; // The maximum manhattan search range for the leaves

        for(BlockPos logPos : logSet) {
            TreeSet<BlockPos> tempVisited = new TreeSet<>();
            TreeSet<BlockPos> toVisit = new TreeSet<>();

            // Add log position as a starting point.
            toVisit.add(logPos.mutableCopy());

            // Do a breadth-first search, with the number of layers determined by range.
            for(int i = 0; i < range; i++) {

                // Create a temporary set to allow altering of the toVisit within the loop.
                TreeSet<BlockPos> tempSet = new TreeSet<>(toVisit);

                for (BlockPos currentPos : tempSet) {
                    tempVisited.add(currentPos.mutableCopy());
                    toVisit.remove(currentPos);

                    // Scan in all the surrounding leaves (manhattan-style)
                    scanLeaves(world, currentPos, toVisit, tempVisited);
                }
            }

            // Doing a final sweep to check the boundary blocks
            for(BlockPos pos : toVisit) {
                if (isNaturalLeaf(world, pos)) {
                    visited.add(pos.mutableCopy()); // mutableCopy() required because Java is a tool
                }
            }

            // Remove log position as it is a log... not a leaf
            tempVisited.remove(logPos);

            // Makes a union of all the combined leaf searches.
            visited.addAll(tempVisited);
        }

        return visited;
    }

    private static void scanLeaves(World world, BlockPos currentPos, TreeSet<BlockPos> toVisit, TreeSet<BlockPos> visited) {
        // The true manhattan
        for (Direction d : Direction.values()) {
            BlockPos pos = currentPos.offset(d, 1);

            // If a leaf is detected that hasn't been iterated over yet, add to the list of blocks to get around to
            if (isNaturalLeaf(world, pos) && !visited.contains(pos)) {
                toVisit.add(pos.mutableCopy()); // mutableCopy() required because Java is a tool
            }
        }
    }

    private static boolean isNaturalLeaf(World world, BlockPos pos) {
        return world.getBlockState(pos).isIn(BlockTags.LEAVES) && !world.getBlockState(pos).get(LeavesBlock.PERSISTENT);
    }

    public static void checkLifeForce(World world, BlockPos startingPos) {
        TreeManager treeManager = ((ServerWorldMixinAccess)world).getTreeManager();
        TreeStructure tree = treeManager.getTreeStructureFromPos(startingPos, world);

        // Check life force of entire tree
        // TODO: Optimise
        int lifeForceTotal = 0;
        for (BlockPos pos : tree.logs) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof CarvedLogEntity carvedEntity) {
                for (Direction dir : Direction.values()) {
                    int[] faceArray = carvedEntity.getFaceArray(dir);

                    Rune rune = RuneManager.getRuneFromArray(faceArray);

                    if (rune != null && tree.isNatural() && carvedEntity.isFaceCatalysed(dir)) {
                        lifeForceTotal += rune.lifeForce;
                    }
                }
            } else if (be instanceof HollowedLogEntity hollowEntity) {
                if (hollowEntity.getItemID().equals(Registry.ITEM.getId(Arborealis.TREE_CORE))) {
                    lifeForceTotal -= 5;
                }
            }
        }

        // Deactivate every rune in tree if over life force limit
        for (BlockPos pos : tree.logs) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof CarvedLogEntity carvedLogEntity) {
                if (carvedLogEntity.getLogState().isOf(Blocks.PUMPKIN))
                    carvedLogEntity.setRunesActive(lifeForceTotal <= 1);
                else
                    carvedLogEntity.setRunesActive(lifeForceTotal <= LIFE_FORCE_MAX);

                carvedLogEntity.checkForRunes();
            }
        }
    }
}
