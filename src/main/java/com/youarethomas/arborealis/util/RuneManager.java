package com.youarethomas.arborealis.util;

import com.google.gson.Gson;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.runes.AbstractRune;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RuneManager {

    private static HashMap<Identifier, AbstractRune> RuneRegistry = new HashMap<>();
    private static List<AbstractRune> Runes = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public static void initializeRunes() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(Arborealis.MOD_ID, "runes");
            }

            @Override
            public void reload(ResourceManager manager) {
                Runes.clear(); // Clear out and reload runes

                int runesRegistered = 0;
                for (Identifier id : manager.findResources("runes", path -> path.endsWith(".json"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                        AbstractRune.RuneSettings runeSettings = GSON.fromJson(reader, AbstractRune.RuneSettings.class);
                        Runes.add(RuneRegistry.get(id).withSettings(runeSettings));
                        runesRegistered++;
                    } catch (Exception e) {
                        Arborealis.LOGGER.error(String.format("Error occurred while loading resource json %s: %s%n", id.toString(), e));
                    }
                }

                Arborealis.LOGGER.info(String.format("%s runes found and registered!", runesRegistered));
            }
        });
    }

    public static void register(Identifier path, AbstractRune rune) {
        RuneRegistry.put(getRuneJsonPath(path), rune);
    }

    private static Identifier getRuneJsonPath(Identifier identifier) {
        return new Identifier(identifier.getNamespace(), "runes/" + identifier.getPath() + ".json");
    }

    public static AbstractRune getRuneFromArray(int[] faceArray) {
        for (AbstractRune rune : Runes) {
            if (faceHasRune(faceArray, rune.name)) {
                return rune;
            }
        }

        return null;
    }

    public static boolean isValidRune(int[] faceArray) {
        return getRuneFromArray(faceArray) != null;
    }

    public static Item getRuneCatalyst(int[] faceArray) {
        if (isValidRune(faceArray)) {
            AbstractRune rune = getRuneFromArray(faceArray);

            Item catalyst = Registry.ITEM.get(rune.catalyst);

            return catalyst;
        }

        return null;
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
            for (AbstractRune rune : Runes) {
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
