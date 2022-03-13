package com.youarethomas.arborealis.util;

import com.youarethomas.arborealis.runes.AbstractRune;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArborealisNbt {
    public static NbtCompound serializeRune(AbstractRune rune) {
        NbtCompound nbt = new NbtCompound();

        nbt.putString("rune_id", rune.settings.id);

        return nbt;
    }

    public static AbstractRune deserializeRune(NbtCompound nbt) {
        if (nbt.contains("rune_id")) {
            String id = nbt.getString("rune_id");

            return RuneManager.getRuneFromID(id);
        }

        return null;
    }

    public static NbtList serializeRuneList(List<AbstractRune> runeList) {
        NbtList list = new NbtList();

        for (AbstractRune rune : runeList) {
            list.add(serializeRune(rune));
        }

        return list;
    }

    public static List<AbstractRune> deserializeRuneList(NbtList nbtList) {
        List<AbstractRune> runes = new ArrayList<>();

        for (NbtElement nbt : nbtList) {
             runes.add(deserializeRune((NbtCompound)nbt));
        }

        return runes;
    }

    public static NbtList serializeBlockPosList(Map<BlockPos, String> blockPosList) {
        NbtList list = new NbtList();

        for (Map.Entry<BlockPos, String> entry : blockPosList.entrySet()) {
            NbtCompound nbt = new NbtCompound();
            nbt.put("position", NbtHelper.fromBlockPos(entry.getKey()));
            nbt.putString("name", entry.getValue());
            list.add(nbt);
        }

        return list;
    }

    public static Map<BlockPos, String> deserializeBlockPosList(NbtList nbtList) {
        Map<BlockPos, String> map = new HashMap<>();

        for (NbtElement nbt : nbtList) {
            NbtCompound nbtc = (NbtCompound)nbt;
            map.put(NbtHelper.toBlockPos(nbtc.getCompound("position")), nbtc.getString("name"));
        }

        return map;
    }
}
