package com.youarethomas.arborealis.util;

import com.google.gson.Gson;
import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

                System.out.printf("%s runes found and registered!%n", runesRegistered);
            }
        });
    }

    public static boolean isValidRune(int[] faceArray) {
        faceArray = Arrays.stream(faceArray).map(i -> i == 2 ? 0 : i).toArray();

        for (Rune rune : Runes) {
            if (Arrays.deepEquals(ArrayUtils.toObject(faceArray), ArrayUtils.toObject(rune.shape))) {
                return true;
            }
        }
        return false;
    }

    public static Rune getRuneFromArray(int[] faceArray) {
        for (Rune rune : Runes) {
            if (Arrays.deepEquals(ArrayUtils.toObject(faceArray), ArrayUtils.toObject(rune.shape))) {
                return rune;
            }
        }
        return null;
    }
}
