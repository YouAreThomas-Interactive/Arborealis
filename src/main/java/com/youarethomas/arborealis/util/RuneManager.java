package com.youarethomas.arborealis.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;

public class RuneManager {

    private static HashMap<int[], String> Runes = new HashMap<>();

    private static final int[] light = new int[] {
        0, 0, 0, 1, 0, 0, 0,
        0, 0, 0, 1, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 1, 0, 1, 0, 0,
        0, 0, 1, 0, 1, 0, 0,
        0, 0, 1, 0, 1, 0, 0,
        0, 0, 1, 1, 0, 0, 0
    };

    public static void initializeRunes() {
        Runes.put(light, "light");
    }

    public static boolean isValidRune(int[] faceArray) {
        faceArray = Arrays.stream(faceArray).map(i -> i == 2 ? 0 : i).toArray();

        for (int[] runeArray : Runes.keySet()) {
            if (Arrays.deepEquals(ArrayUtils.toObject(faceArray), ArrayUtils.toObject(runeArray))) {
                return true;
            }
        }
        return false;
    }

    public static String getRuneName(int[] faceArray) {
        for (int[] runeArray : Runes.keySet()) {
            if (Arrays.deepEquals(ArrayUtils.toObject(faceArray), ArrayUtils.toObject(runeArray))) {
                return Runes.get(runeArray);
            }
        }
        return null;
    }
}
