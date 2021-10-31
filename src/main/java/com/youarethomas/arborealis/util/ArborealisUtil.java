package com.youarethomas.arborealis.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Box;
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
    public static boolean isWithinEffectRadius(Vec3i posToCheck, Vec3i radiusPos, int radius) {
        double x = (double)posToCheck.getX() + 0.5D - (double)radiusPos.getX();
        double z = (double)posToCheck.getZ() + 0.5D - (double)radiusPos.getZ();

        double distance = x * x + z * z;

        return distance < radius * radius;
    }

    public static List<LivingEntity> getPlayersInRadius(World world, Vec3i pos, int radius) {
        Box box = Box.from(new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)).expand(radius + 1);

        List<LivingEntity> entities = new ArrayList<>();

        for (LivingEntity livingEntity : world.getNonSpectatingEntities(LivingEntity.class, box)) {
            if (ArborealisUtil.isWithinEffectRadius(livingEntity.getBlockPos(), pos, radius)) {
                entities.add(livingEntity);
            }
        }

        return entities;
    }

    public static void applyStatusEffectsToEntities(List<LivingEntity> entityList, StatusEffect effect) {
        if (!entityList.isEmpty()) {
            for (LivingEntity playerEntity : entityList) {
                playerEntity.addStatusEffect(new StatusEffectInstance(effect, 5, 0, true, false, true));
            }
        }
    }
}
