package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Breed extends Rune {

    @Override
    public void onServerTick(World world, BlockPos pos, CarvedLogEntity be) {
        if (Arborealis.RANDOM.nextInt(100) == 69) {
            List<Entity> entities = ArborealisUtil.getEntitiesInRadius(world, Vec3d.ofCenter(pos), be.radius, false);
            int count = entities.size();

            while (count < 20 && count > 0) {
                int index = Arborealis.RANDOM.nextBetween(0, entities.size() - 1);

                Entity entity = entities.get(index);
                if (entity instanceof AnimalEntity animal) {
                    int i = animal.getBreedingAge();
                    if (i == 0 && animal.canEat()) {
                        animal.setLoveTicks(600);
                        world.sendEntityStatus(animal, EntityStatuses.ADD_BREEDING_PARTICLES);
                    }
                }

                count++;
            }
        }
    }

    @Override
    public boolean showRadiusEffect() {
        return true;
    }
}
