package com.youarethomas.arborealis.util;

import com.youarethomas.arborealis.runes.Rune;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArborealisNbt {
    public static NbtCompound serializeRune(Rune rune) {
        NbtCompound nbt = new NbtCompound();

        nbt.putString("id", rune.id);
        nbt.putString("name", rune.name);
        nbt.putString("colour", rune.colour);
        nbt.putString("catalyst", rune.catalyst.toString());
        nbt.putInt("life_force", rune.lifeForce);
        nbt.putIntArray("shape", rune.shape);

        return nbt;
    }

    public static Rune deserializeRune(NbtCompound nbt) {
        String id = nbt.getString("id");
        String name = nbt.getString("name");
        String colour = nbt.getString("colour");
        String catalyst = nbt.getString("catalyst");
        int lifeForce = nbt.getInt("life_force");
        int[] shape = nbt.getIntArray("shape");

        return RuneManager.getRuneFromID(id).fromValues(id, name, colour, catalyst, lifeForce, shape);
    }

    public static NbtList serializeRuneList(List<Rune> runeList) {
        NbtList list = new NbtList();

        for (Rune rune : runeList) {
            list.add(serializeRune(rune));
        }

        return list;
    }

    public static List<Rune> deserializeRuneList(NbtList nbtList) {
        List<Rune> runes = new ArrayList<>();

        for (NbtElement nbt : nbtList) {
             runes.add(deserializeRune((NbtCompound)nbt));
        }

        return runes;
    }

    public static NbtList serializeCorePosList(Map<BlockPos, String> blockPosList) {
        NbtList list = new NbtList();

        for (Map.Entry<BlockPos, String> entry : blockPosList.entrySet()) {
            NbtCompound nbt = new NbtCompound();
            nbt.put("position", NbtHelper.fromBlockPos(entry.getKey()));
            nbt.putString("name", entry.getValue());
            list.add(nbt);
        }

        return list;
    }

    public static Map<BlockPos, String> deserializeCorePosList(NbtList nbtList) {
        Map<BlockPos, String> map = new HashMap<>();

        for (NbtElement nbt : nbtList) {
            NbtCompound nbtc = (NbtCompound)nbt;
            map.put(NbtHelper.toBlockPos(nbtc.getCompound("position")), nbtc.getString("name"));
        }

        return map;
    }

    public static NbtList serializePasswordPosList(Map<BlockPos, Direction> blockPosList) {
        NbtList list = new NbtList();

        for (Map.Entry<BlockPos, Direction> entry : blockPosList.entrySet()) {
            NbtCompound nbt = new NbtCompound();
            nbt.put("position", NbtHelper.fromBlockPos(entry.getKey()));
            nbt.putInt("direction", entry.getValue().getId());
            list.add(nbt);
        }

        return list;
    }

    public static Map<BlockPos, Direction> deserializePasswordPosList(NbtList nbtList) {
        Map<BlockPos, Direction> map = new HashMap<>();

        for (NbtElement nbt : nbtList) {
            NbtCompound nbtc = (NbtCompound)nbt;
            map.put(NbtHelper.toBlockPos(nbtc.getCompound("position")), Direction.byId(nbtc.getInt("direction")));
        }

        return map;
    }
}
