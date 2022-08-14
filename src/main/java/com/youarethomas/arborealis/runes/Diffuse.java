package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Diffuse extends Rune {
    List<CreeperEntity> diffusedCreepers = new ArrayList<>();

    @Override
    public boolean showRadiusEffect() {
        return true;
    }

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {
        List<Entity> entities = ArborealisUtil.getEntitiesInRadius(world, Vec3d.ofCenter(pos), be.radius, false);

        for (Entity entity : entities) {
            if (entity instanceof CreeperEntity creeper) {
                if (!diffusedCreepers.contains(creeper)) {
                    diffusedCreepers.add(creeper);
                    System.out.println("Creepers diffused: " + diffusedCreepers.size());
                }
            } else if (entity instanceof TntEntity tntEntity) {
                // Get data from the Tnt entity
                Vec3d tntPos = tntEntity.getPos();
                Vec3d tntVelocity = tntEntity.getVelocity();

                // Make sound
                world.playSound(tntPos.x, tntPos.y, tntPos.z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f, false);

                // Remove it and replace with the item
                tntEntity.discard();
                world.spawnEntity(new ItemEntity(world, tntPos.x, tntPos.y, tntPos.z, Items.TNT.getDefaultStack(), tntVelocity.x, tntVelocity.y, tntVelocity.z));
            }
        }

        for (int i = diffusedCreepers.size() - 1; i >= 0; i--) {
            CreeperEntity creeper = diffusedCreepers.get(i);

            if (ArborealisUtil.isWithinRadius(creeper.getPos(), Vec3d.ofCenter(pos), be.radius)) {
                if (creeper.isAlive()) {
                    creeper.setFuseSpeed(-1);
                } else {
                    diffusedCreepers.remove(creeper);
                }
            } else {
                diffusedCreepers.remove(creeper);
            }
        }
    }
}
