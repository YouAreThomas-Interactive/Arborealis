package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Push extends Rune {

    boolean applyEffect = false;
    final double MULTIPLIER = 0.1D;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        applyEffect = true;
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        applyEffect = false;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {
        if (applyEffect) {
            List<Entity> entities = ArborealisUtil.getEntitiesInRadius(world, pos, be.radius, false);

            for (Entity entity : entities) {
                Vec3d target = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

                double multiplier = MULTIPLIER;
                if (entity.getVelocity().normalize().y > 0.2D || entity.getVelocity().normalize().y < -0.2D) {
                    multiplier *= 0.1;
                }
                if (entity instanceof LivingEntity) {
                    multiplier *= 5;
                }

                Vec3d pushVelocity = entity.getPos().subtract(target).normalize().multiply(multiplier);

                entity.addVelocity(pushVelocity.x, 0, pushVelocity.z);
            }
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {

    }

    @Override
    public boolean showRadiusEffect() {
        return true;
    }
}
