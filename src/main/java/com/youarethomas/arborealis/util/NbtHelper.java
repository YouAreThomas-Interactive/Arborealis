package com.youarethomas.arborealis.util;

import com.youarethomas.arborealis.runes.AbstractRune;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;

public class NbtHelper {
    public static NbtCompound serializeRune(AbstractRune rune) {
        NbtCompound nbt = new NbtCompound();

        nbt.putString("rune_id", rune.settings.id);

        return nbt;
    }

    public static NbtList serializeRuneList(List<AbstractRune> runeList) {
        NbtList list = new NbtList();

        for (AbstractRune rune : runeList) {
            list.add(serializeRune(rune));
        }

        return list;
    }

    public static AbstractRune deserializeRune(NbtCompound nbt) {
        if (nbt.contains("rune_id")) {
            String id = nbt.getString("rune_id");

            return RuneManager.getRuneFromID(id);
        }

        return null;
    }

    public static List<AbstractRune> deserializeRuneList(NbtList nbtList) {
        List<AbstractRune> runes = new ArrayList<>();

        for (NbtElement nbt : nbtList) {
             runes.add(deserializeRune((NbtCompound)nbt));
        }

        return runes;
    }
}
