package com.youarethomas.arborealis.util;

import com.google.gson.Gson;
import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RuneManager {

    private static List<Rune> Runes = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public static void initializeRunes() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Arborealis.MOD_ID, "runes");
            }

            @Override
            public void reload(ResourceManager manager) {
                Runes.clear(); // Clear out and re-load runes

                int runesRegistered = 0;
                for (Identifier id : manager.findResources("runes", path -> path.endsWith(".json"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                        Rune rune = GSON.fromJson(reader, Rune.class);
                        Runes.add(rune);
                        runesRegistered++;
                    } catch (Exception e) {
                        System.out.printf("Error occurred while loading resource json %s: %s%n", id.toString(), e);
                    }
                }

                Arborealis.LOGGER.info(String.format("%s runes found and registered!", runesRegistered));
            }
        });
    }

    public static Rune getRuneFromArray(int[] faceArray) {
        for (Rune rune : Runes) {
            if (faceHasRune(faceArray, rune.name)) {
                return rune;
            }
        }

        return null;
    }

    public static boolean isValidRune(int[] faceArray) {
        return getRuneFromArray(faceArray) != null;
    }

    public static boolean faceHasRune(int[] faceArray, String runeName) {
        faceArray = Arrays.stream(faceArray).map(i -> i == 2 ? 0 : i).toArray();
        boolean runeFound = false;
        int runeSize = 7;
        int[][] faceMatrix = new int[runeSize][runeSize];

        // Convert 1D rune array into a 2D rune array
        for (int y = 0; y < runeSize; y++) {
            for (int x = 0; x < runeSize; x++) {
                faceMatrix[y][x] = faceArray[(y * runeSize) + x];
            }
        }

        // Test and rotate the rune in each of the 4 possible rotations
        for (int rotation = 0; rotation < 4; rotation++) {
            // Turn the 2D array back into a 1D array and test the rune. End loop if found.
            faceArray = Stream.of(faceMatrix).flatMapToInt(IntStream::of).toArray();
            for (Rune rune : Runes) {
                if (Objects.equals(rune.name, runeName) && Arrays.deepEquals(ArrayUtils.toObject(faceArray), ArrayUtils.toObject(rune.shape))) {
                    runeFound = true;
                    break;
                }
            }

            // Rotate the rune
            int[][] rotatedArray = new int[runeSize][runeSize];
            for (int i = 0; i < runeSize; ++i) {
                for (int j = 0; j < runeSize; ++j) {
                    rotatedArray[i][j] = faceMatrix[runeSize - j - 1][i];
                }
            }
            faceMatrix = rotatedArray.clone();
        }

        return runeFound;
    }
}
