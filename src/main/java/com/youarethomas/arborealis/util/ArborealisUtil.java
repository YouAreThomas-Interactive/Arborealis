package com.youarethomas.arborealis.util;

import com.youarethomas.arborealis.Arborealis;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic utilities used by Arborealis in many places
 */
public class ArborealisUtil {

    /**
     * Returns whether a given position is within a certain radius of another position.
     * @param posToCheck The position you're testing.
     * @param radiusPos The position the radius emanates from.
     * @param radius Radius of the test, defined as a cylinder.
     */
    public static boolean isWithinRadius(Vec3d posToCheck, Vec3d radiusPos, int radius) {
        double x = posToCheck.getX() + 0.5D - radiusPos.getX();
        double z = posToCheck.getZ() + 0.5D - radiusPos.getZ();

        double distance = x * x + z * z;

        return distance < radius * radius;
    }

    public static long argbToHex(int a, int r, int g, int b) {
        String hex = String.format("%02X%02X%02X%02X", a, r, b, b);
        return Long.parseLong(hex, 16);
    }

    public static List<Entity> getEntitiesInRadius(World world, Vec3d pos, int radius, boolean onlyPlayers) {
        Box box = Box.from(new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)).expand(radius + 1);

        List<Entity> entities = new ArrayList<>();

        for (Entity entity : world.getNonSpectatingEntities(Entity.class, box)) {
            if (ArborealisUtil.isWithinRadius(entity.getPos(), pos, radius)) {
                if (onlyPlayers) {
                    if (entity instanceof PlayerEntity) {
                        entities.add(entity);
                    }
                } else {
                    entities.add(entity);
                }
            }
        }

        return entities;
    }

    public static void applyStatusEffectsToEntities(List<Entity> entityList, StatusEffect effect) {
        if (!entityList.isEmpty()) {
            for (Entity entity : entityList) {
                if (entity instanceof PlayerEntity playerEntity) {
                    playerEntity.addStatusEffect(new StatusEffectInstance(effect, 5, 0, true, false, true));
                }
            }
        }
    }

    public static class Colour {
        public float red;
        public float green;
        public float blue;

        public Colour(int hex) {
            red = (hex & 0xFF0000) >> 16;
            green = (hex & 0xFF00) >> 8;
            blue = (hex & 0xFF);
        }

        public Colour(float r, float g, float b) {
            red = r;
            green = g;
            blue = b;
        }
    }
}

