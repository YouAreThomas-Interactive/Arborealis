package com.youarethomas.arborealis.runes;

import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class Load extends Rune {

    @Override
    public void onRuneFound(World world, BlockPos pos, CarvedLogEntity be) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.setChunkForced(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), true);
        }
    }

    @Override
    public void onRuneLost(World world, BlockPos pos, CarvedLogEntity be) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.setChunkForced(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), false);
        }
    }

    @Override
    public void onClientTick(World world, BlockPos pos, CarvedLogEntity be) {
        // Draw chunk outline
        ChunkPos chunkPos = world.getWorldChunk(pos).getPos();
        createChunkBorderParticles(world, pos, chunkPos.getStartX(), chunkPos.getStartZ());
    }

    // I stole this, and don't know how it works, but it's great!
    private Vec2f randomPointNearRect(int x, int z, int xSize, int zSize) {
        if (Math.random() < (double)xSize / (xSize + zSize)) { // top bottom
            x = (int) (Math.random() * xSize + x);
            z = Math.random() < 0.5 ? z : z + zSize -1;
        } else {
            z = (int) (Math.random() * zSize + z);
            x = Math.random() < 0.5 ? x: x + xSize -1;
        }
        return new Vec2f(x, z);
    }

    private void createChunkBorderParticles(World world, BlockPos pos, int x, int z) {
        Vec2f[] points = new Vec2f[200];
        Random random = world.random;

        // Create the square of points to display particles at
        for (int i = 0; i < 200; ++i) {
            Vec2f point = randomPointNearRect(x, z, 17, 17);
            points[i] = point;
        }

        for (Vec2f point : points) {
            System.out.println(point.x + ", " + point.y);

            int randomParticle = random.nextInt(100);

            // With a 1% chance to display a particle...
            if (randomParticle == 1) {
                BlockPos particlePos = new BlockPos(point.x, pos.getY(), point.y);

                // Check from 10 blocks above rune to 10 below
                for (int y = 10; y > -10; y--) {
                    BlockPos testPos = particlePos.withY(pos.getY() + y);

                    // If the block is air or a plant, keep going
                    if (world.getBlockState(testPos).isOf(Blocks.AIR) || world.getBlockState(testPos).isIn(BlockTags.REPLACEABLE_PLANTS)) {
                        particlePos = testPos;
                    } else {
                        break;
                    }
                }

                System.out.println("x: " + particlePos.getX() + " y: " + particlePos.getY() + " z: " + particlePos.getZ());

                world.addParticle(ParticleTypes.COMPOSTER, particlePos.getX(), particlePos.getY(), particlePos.getZ(), -0.5 + random.nextFloat(), random.nextFloat() / 3, -0.5 + random.nextFloat());
            }
        }
    }
}
