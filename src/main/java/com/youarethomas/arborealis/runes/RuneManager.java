package com.youarethomas.arborealis.runes;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RuneManager {
    public static final Light LIGHT = new Light();
    public static final Chop CHOP = new Chop();
    public static final Pull PULL = new Pull();
    public static final Push PUSH = new Push();
    public static final AreaChop AREA_CHOP = new AreaChop();
    public static final PlantTrees PLANT_TREES = new PlantTrees();
    public static final Harvest HARVEST = new Harvest();
    public static final PlantCrops PLANT_CROPS = new PlantCrops();
    public static final Extinguish EXTINGUISH = new Extinguish();
    public static final Grow GROW = new Grow();
    public static final Diffuse DIFFUSE = new Diffuse();
    public static final Breed BREED = new Breed();
    public static final Load LOAD = new Load();
    public static final Deafen DEAFEN = new Deafen();

    private static HashMap<Identifier, Rune> RuneRegistry = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static int getRuneCount() {
        return RuneRegistry.size();
    }

    public static List<Rune> getRunes() {
        return RuneRegistry.values().stream().toList();
    }

    public static void initializeRunePatterns(Identifier runesPath) {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return runesPath;
            }

            @Override
            public void reload(ResourceManager manager) {
                int runesRegistered = 0;
                for (Map.Entry<Identifier, Resource> entry : manager.findResources("runes", path -> path.getPath().endsWith(".json")).entrySet()) {
                    Identifier id = entry.getKey();
                    try (InputStream stream = manager.getResource(id).get().getInputStream()) {
                        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                        Rune.RuneSettings runeSettings = GSON.fromJson(reader, Rune.RuneSettings.class);
                        runeSettings.id = id.toString();
                        RuneRegistry.replace(id, RuneRegistry.get(id).fromJson(runeSettings));
                        runesRegistered++;
                    } catch (Exception e) {
                        Arborealis.LOGGER.error(String.format("Error occurred while loading resource json %s: %s%n", id.toString(), e));
                    }
                }

                Arborealis.LOGGER.info(String.format("%s runes found and registered!", runesRegistered));
            }
        });
    }

    public static void registerRunes() {
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "light"), LIGHT);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "chop"), CHOP);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "pull"), PULL);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "push"), PUSH);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "area_chop"), AREA_CHOP);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "plant_trees"), PLANT_TREES);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "harvest"), HARVEST);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "plant_crops"), PLANT_CROPS);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "extinguish"), EXTINGUISH);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "grow"), GROW);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "diffuse"), DIFFUSE);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "breed"), BREED);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "load"), LOAD);
        RuneManager.register(new Identifier(Arborealis.MOD_ID, "deafen"), DEAFEN);
    }

    public static void register(Identifier path, Rune rune) {
        if (RuneRegistry.containsKey(path))
            RuneRegistry.replace(path, rune);
        else
            RuneRegistry.put(getRuneJsonPath(path), rune);
    }

    public static void clientRunePush(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        ArrayList<Rune> runes = buf.readCollection(PacketByteBuf.getMaxValidator(Lists::newArrayListWithCapacity, 10000), innerBuf -> {
            String id = innerBuf.readString();
            String name = innerBuf.readString();
            String colour = innerBuf.readString();
            String catalyst = innerBuf.readString();
            int lifeForce = innerBuf.readInt();
            int[] shape = innerBuf.readIntArray();

            if (RuneManager.getRuneFromID(id) != null) {
                System.out.println("Found");
                return RuneManager.getRuneFromID(id).fromValues(id, name, colour, catalyst, lifeForce, shape);
            } else {
                System.out.println("Not found");
                return null;
            }
        });

        client.execute(() -> {
            // Replace existing runes with the new info
            for (Rune rune : runes)
                RuneManager.register(new Identifier(rune.id), rune);
        });
    }

    public static Rune getRuneFromID(String id) {
        if (RuneRegistry.containsKey(new Identifier(id)))
            return RuneRegistry.get(new Identifier(id));

        return null;
    }

    private static Identifier getRuneJsonPath(Identifier identifier) {
        return new Identifier(identifier.getNamespace(), "runes/" + identifier.getPath() + ".json");
    }

    public static Rune getRuneFromArray(int[] faceArray) {
        for (Rune rune : RuneRegistry.values()) {
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
            Rune rune = getRuneFromArray(faceArray);

            Item catalyst = Registries.ITEM.get(rune.catalyst);

            return catalyst;
        }

        return null;
    }

    public static boolean faceHasRune(int[] faceArray, String runeName) {
        // Get the rune from the runeName, and save its shape for later
        Optional<Rune> rune = RuneRegistry.values().stream().filter(r -> Objects.equals(r.name, runeName)).findFirst();
        int[] runeShape;
        if (rune.isPresent())
            runeShape = rune.get().shape;
        else
            return false;

        // Turn all highlights (2) into uncarved boyos (0), and all light runes (3) into carved boyos (1)
        faceArray = Arrays.stream(faceArray).map(i -> i == 2 ? 0 : i).toArray();
        faceArray = Arrays.stream(faceArray).map(i -> i == 3 ? 1 : i).toArray();

        // If the array is all 0s it contains shit all
        if (Arrays.stream(faceArray).allMatch(i -> i == 0))
            return false;

        // Convert 1D rune array into a 2D rune array
        int runeSize = 5;
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
            int[][] newFaceMatrix = new int[rows.size()][runeSize];
            faceArray = Stream.of(rows.toArray(newFaceMatrix)).flatMapToInt(IntStream::of).toArray();
            int trimmedLength = faceArray.length;

            // Remove all leading 0s
            int carvingsRemoved = 0;
            while (faceArray[0] == 0 && carvingsRemoved < runeSize) {
                faceArray = Arrays.copyOfRange(faceArray, 1, faceArray.length);
                carvingsRemoved++;
            }

            // Then add them in systematically to check for offsets
            for (int offset = 0; offset < runeSize; offset++) {
                int[] newFaceArray = Arrays.copyOf(faceArray, faceArray.length + 1);
                newFaceArray[0] = 0;
                System.arraycopy(faceArray, 0, newFaceArray, 1, faceArray.length);
                faceArray = Arrays.copyOf(newFaceArray, trimmedLength);

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

    private static int countOccurrences(int[] arr, int value) {
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
