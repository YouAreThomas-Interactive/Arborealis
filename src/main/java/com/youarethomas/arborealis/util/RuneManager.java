package com.youarethomas.arborealis.util;

import com.google.gson.Gson;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.runes.AbstractRune;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RuneManager {

    private static HashMap<Identifier, AbstractRune> RuneRegistry = new HashMap<>();
    private static List<AbstractRune> Runes = new ArrayList<>();
    private static final Gson GSON = new Gson();

    public static void initializeRunes(Identifier runesPath) {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return runesPath;
            }

            @Override
            public void reload(ResourceManager manager) {
                Runes.clear(); // Clear out and reload runes

                int runesRegistered = 0;
                for (Identifier id : manager.findResources("runes", path -> path.endsWith(".json"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                        AbstractRune.RuneSettings runeSettings = GSON.fromJson(reader, AbstractRune.RuneSettings.class);
                        runeSettings.id = id.toString();
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

    public static AbstractRune getRuneFromID(String id) {
        for (AbstractRune rune : Runes) {
            if (rune.settings.id.equals(id)) {
                return rune; // If a rune is found with a matching path id
            }
        }

        return null;
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
        // Get the rune from the runeName, and save it's shape for later
        Optional<AbstractRune> rune = Runes.stream().filter(r -> Objects.equals(r.name, runeName)).findFirst();
        int[] runeShape;
        if (rune.isPresent())
            runeShape = rune.get().shape;
        else
            return false;

        // Turn all highlights (2) into uncarved boyos (0)
        faceArray = Arrays.stream(faceArray).map(i -> i == 2 ? 0 : i).toArray();

        // If the array is all 0s it contains shit all
        if (Arrays.stream(faceArray).allMatch(i -> i == 0))
            return false;

        // Convert 1D rune array into a 2D rune array
        int runeSize = 7;
        int[][] faceMatrix = new int[runeSize][runeSize];
        for (int y = 0; y < runeSize; y++) {
            System.arraycopy(faceArray, (y * runeSize), faceMatrix[y], 0, runeSize);
        }

        // Test and rotate the rune in each of the 4 possible rotations
        for (int rotation = 0; rotation < 4; rotation++) {
            List<int[]> rows = new ArrayList<>(Arrays.asList(faceMatrix));

            // Trim off empty rows from start
            while (Arrays.stream(rows.get(0)).allMatch(i -> i == 0)) {
                rows.remove(0);
            }
            // and the end
            while (Arrays.stream(rows.get(rows.size() - 1)).allMatch(i -> i == 0)) {
                rows.remove(rows.size() - 1);
            }

            // Turn the 2D array back into a 1D array and test the rune. End loop if found.
            int[][] newFaceMatrix = new int[rows.size()][7];
            faceArray = Stream.of(rows.toArray(newFaceMatrix)).flatMapToInt(IntStream::of).toArray();
            int trimmedLength = faceArray.length;

            // Remove all leading 0s
            int carvingsRemoved = 0;
            while (faceArray[0] == 0 && carvingsRemoved < 7) {
                faceArray = Arrays.copyOfRange(faceArray, 1, faceArray.length);
                carvingsRemoved++;
            }

            // Then add them in systematically to check for offsets
            for (int offset = 0; offset < 7; offset++) {
                int[] newFaceArray = Arrays.copyOf(faceArray, faceArray.length + 1);
                newFaceArray[0] = 0;
                System.arraycopy(faceArray, 0, newFaceArray, 1, faceArray.length);
                faceArray = Arrays.copyOf(newFaceArray, trimmedLength);

                printRune(faceArray);

                // A bit of a hack to make it so that parts of a rune done count, and then see if our array (that's been altered a lot) is actually in the master runeShape array
                if (countOccurrences(runeShape, 1) == countOccurrences(faceArray, 1) && containsRune(runeShape, faceArray)) {
                    return true;
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

        return false;
    }

    private static int countOccurrences(int[] arr, int value)
    {
        int count = 0;
        for (int i : arr)
            if (i == value)
                count++;

        return count;
    }

    // Stolen kindly from https://stackoverflow.com/a/21467021/13317368
    public static boolean containsRune(int[] baseRune, int[] runeToCompare) {
        OUTER:
        for (int offset = 0; offset <= baseRune.length - runeToCompare.length; offset++) {
            for (int carving = 0; carving < runeToCompare.length; carving++) {
                if (baseRune[offset + carving] != runeToCompare[carving])
                    continue OUTER;
            }
            return true;
        }
        return false;
    }

    public static void printRune(int[] runeArray) {
        int count = 0;
        for (int j : runeArray) {
            System.out.print(j);
            count++;
            if (count == 7) {
                System.out.print("\n");
                count = 0;
            }
        }
        System.out.println("\n");
    }
}
