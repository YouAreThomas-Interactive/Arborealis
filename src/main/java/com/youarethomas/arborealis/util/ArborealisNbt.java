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
import java.util.List;

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

    public static NbtList serializeBlockPosList(List<BlockPos> blockPosList) {
        NbtList list = new NbtList();

        for (BlockPos pos : blockPosList)
            list.add(NbtHelper.fromBlockPos(pos));

        return list;
    }

    public static List<BlockPos> deserializeBlockPosList(NbtList nbtList) {
        List<BlockPos> list = new ArrayList<>();

        for (NbtElement nbt : nbtList)
            list.add(NbtHelper.toBlockPos((NbtCompound)nbt));

        return list;
    }
}
