package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Pull extends AbstractRune{

    boolean applyEffect = false;
    final double MULTIPLIER = 0.1D;

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedWoodEntity be) {
        applyEffect = true;
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedWoodEntity be) {
        applyEffect = false;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedWoodEntity be) {
        // TODO: Change speed based on entity type

        if (applyEffect) {
            List<Entity> entities = ArborealisUtil.getEntitiesInRadius(world, pos, be.radius, false);

            for (Entity entity : entities) {
                Vec3d target = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                Vec3d pullVelocity = target.subtract(entity.getPos()).normalize().multiply(MULTIPLIER);

                entity.addVelocity(pullVelocity.x, 0, pullVelocity.z);
            }
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedWoodEntity be) {
        /*if (applyEffect) {
            for (Entity entity : ArborealisUtil.getEntitiesInRadius(world, pos, be.radius, true)) {
                if (entity instanceof PlayerEntity player) {
                    if (!player.isSneaking()) {
                        Vec3d target = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        Vec3d pullVelocity = target.subtract(entity.getPos()).normalize().multiply(MULTIPLIER);

                        entity.addVelocity(pullVelocity.x, 0, pullVelocity.z);
                    }
                }
            }
        }*/
    }

    @Override
    public boolean showRadiusEffect() {
        return true;
    }
}
