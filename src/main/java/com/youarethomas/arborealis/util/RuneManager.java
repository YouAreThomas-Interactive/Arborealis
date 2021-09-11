package com.youarethomas.arborealis.util;

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
        System.out.println("Rune valid: %s".formatted(Runes.containsKey(faceArray)));
        return Runes.containsKey(reverse(faceArray, faceArray.length));
    }

    public static String getRuneName(int[] faceArray) {
        if (isValidRune(faceArray)) {
            return Runes.get(reverse(faceArray, faceArray.length));
        } else {
            return null;
        }
    }

    static int[] reverse(int[] array, int arrayLength)
    {
        int[] reversed = new int[arrayLength];
        for (int i = 0; i < arrayLength; i++) {
            reversed[arrayLength - 1] = array[i];
            arrayLength = arrayLength - 1;
        }

        return reversed;
    }
}
