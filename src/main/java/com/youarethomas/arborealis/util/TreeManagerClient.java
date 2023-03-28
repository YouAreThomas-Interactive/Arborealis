package com.youarethomas.arborealis.util;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeManagerClient {

    private static final Map<RegistryKey<World>, Hashtable<String, TreeStructure>> treeStructureMappings = new HashMap<>();

    public static void downloadTreeStructures(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        List<String> structureIDs = buf.readCollection(PacketByteBuf.getMaxValidator(Lists::newArrayListWithCapacity, 10000), PacketByteBuf::readString);
        List<TreeStructure> structures = buf.readCollection(PacketByteBuf.getMaxValidator(Lists::newArrayListWithCapacity, 10000), packetByteBuf -> {
            NbtCompound nbt = packetByteBuf.readNbt();
            if (nbt != null) {
                return TreeStructure.fromNbt(nbt);
            } else {
                return null;
            }
        });
        RegistryKey<World> worldKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(buf.readString()));

        // Recreates the mappings from the structure IDs and the structures.
        Hashtable<String, TreeStructure> treeStructureMappings = new Hashtable<>(IntStream.range(0, structureIDs.size()).boxed()
                .collect(Collectors.toMap(structureIDs::get, structures::get)));

        client.execute(() -> initTreeStructure(worldKey, treeStructureMappings));
    }

    private static void initTreeStructure(RegistryKey<World> worldKey, Hashtable<String, TreeStructure> treeStructureMapping) {
        treeStructureMappings.put(worldKey, treeStructureMapping);
        System.out.println("Initialised trees structures for " + worldKey.toString() + ": " + treeStructureMapping.size());
    }

    public static void updateTreeStructures(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        List<String> removedStructureIDs = buf.readCollection(PacketByteBuf.getMaxValidator(Lists::newArrayListWithCapacity, 10000), PacketByteBuf::readString);
        List<String> addedStructureIDs = buf.readCollection(PacketByteBuf.getMaxValidator(Lists::newArrayListWithCapacity, 10000), PacketByteBuf::readString);
        List<TreeStructure> addedStructures = buf.readCollection(PacketByteBuf.getMaxValidator(Lists::newArrayListWithCapacity, 10000), packetByteBuf -> {
            NbtCompound nbt = packetByteBuf.readNbt();
            if (nbt != null) {
                return TreeStructure.fromNbt(nbt);
            } else {
                return null;
            }
        });
        RegistryKey<World> worldKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(buf.readString()));

        // Recreates the mappings from the structure IDs and the structures.
        Hashtable<String, TreeStructure> treeStructureMappings = new Hashtable<>(IntStream.range(0, addedStructureIDs.size()).boxed()
                .collect(Collectors.toMap(addedStructureIDs::get, addedStructures::get)));

        client.execute(() -> updateTreeStructures(worldKey, removedStructureIDs, treeStructureMappings));
    }

    private static void updateTreeStructures(RegistryKey<World> worldKey, List<String> removedIDs, Hashtable<String, TreeStructure> newStructures) {
        for(String removedID : removedIDs) {
            treeStructureMappings.get(worldKey).remove(removedID);
        }
        treeStructureMappings.get(worldKey).putAll(newStructures);

        System.out.println("Number of removed structures: " + removedIDs.size());
        System.out.println("Number of added structures: " + newStructures.size());
    }

    public static Collection<TreeStructure> getAllTreeStructures(RegistryKey<World> worldKey) {
        return treeStructureMappings.get(worldKey) == null ? Collections.emptyList() : treeStructureMappings.get(worldKey).values();
    }
}
