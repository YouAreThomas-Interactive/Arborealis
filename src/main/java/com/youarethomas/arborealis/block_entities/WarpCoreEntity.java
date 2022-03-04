package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ArborealisPersistentState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import oshi.util.tuples.Pair;

import java.util.*;

public class WarpCoreEntity extends BlockEntity {

    private static boolean allowTeleport = true; // This likely won't work for multiplayer

    private final List<Pair<BlockPos, Direction>> passwordBlockPosList;

    private static final float PASSWORD_RADIUS = 0.6f;
    private static final int PASSWORD_NUM_PARTICLES = 14;

    public WarpCoreEntity(BlockPos pos, BlockState state) {
        super(Arborealis.WARP_CORE_ENTITY, pos, state);

        passwordBlockPosList = new ArrayList<>();

        for(Direction dir : new Direction[]{ Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST }) {
            Vec3i dirVec = dir.getVector().multiply(2).add(Direction.UP.getVector().multiply(2));

            passwordBlockPosList.add(new Pair<>(pos.add(dirVec), dir.getOpposite()));
        }
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, WarpCoreEntity be) {
        for(Pair<BlockPos, Direction> passwordPair : be.passwordBlockPosList) {
            createPasswordParticles(passwordPair.getA(), passwordPair.getB(), world);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, WarpCoreEntity be) {
        List<PlayerEntity> players = (List<PlayerEntity>) world.getPlayers();

        for (PlayerEntity player : players) {
            if (world.getBlockState(player.getBlockPos().down()).isOf(Arborealis.WARP_CORE) && player.getBlockPos().down().equals(pos) && player.isSneaking() && allowTeleport) {
                ArborealisPersistentState worldNbt = ((ServerWorld)world).getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");

                List<BlockPos> corePositions = new ArrayList<>(worldNbt.getWarpCoreList());
                if (corePositions.size() > 1) {
                    corePositions.remove(pos);
                    BlockPos randomPos = corePositions.get(Arborealis.RANDOM.nextInt(corePositions.size())).up();

                    player.teleport(randomPos.getX() + 0.5D, randomPos.getY(), randomPos.getZ() + 0.5D);
                }

                allowTeleport = false;
            } else if (world.getBlockState(player.getBlockPos().down()).isOf(Arborealis.WARP_CORE) && !player.isSneaking()) {
                allowTeleport = true;
            }
        }
    }

    public static void createPasswordParticles(BlockPos pos, Direction direction, World world) {
        Random random = Arborealis.RANDOM;

        // Create points randomly on the circumference of the circle.
        for (int i = 0; i < PASSWORD_NUM_PARTICLES; ++i) {
            int particleSample = random.nextInt(100);

            if(particleSample == 0) {
                double angle = 2.0f * Math.PI * random.nextDouble();

                Vec2f point = new Vec2f(
                        (float) Math.cos(angle) * (PASSWORD_RADIUS),
                        (float) Math.sin(angle) * (PASSWORD_RADIUS)
                );

                Vec3d rotatedPoint = new Vec3d(point.x, point.y, 0);
                Vec3d velocity = rotatedPoint;

                switch(direction) {
                    case NORTH -> {
                        velocity = rotatedPoint.rotateZ(-(float) Math.PI / 2.f);
                    }
                    case SOUTH -> {
                        rotatedPoint = rotatedPoint.rotateY((float) Math.PI);
                        velocity = rotatedPoint.rotateZ((float) Math.PI / 2.f);
                    }
                    case EAST -> {
                        rotatedPoint = rotatedPoint.rotateY((float) Math.PI / 2.f);
                        velocity = rotatedPoint.rotateX((float) Math.PI / 2.f);
                    }
                    case WEST -> {
                        rotatedPoint = rotatedPoint.rotateY(-(float) Math.PI / 2.f);
                        velocity = rotatedPoint.rotateX(-(float) Math.PI / 2.f);
                    }
                }

                Vec3d directionOffset = new Vec3d(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()).multiply(0.6);
                Vec3d particlePos = Vec3d.ofCenter(pos).add(directionOffset).add(rotatedPoint);

                world.addParticle(ParticleTypes.COMPOSTER, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
            }
        }
    }
}
