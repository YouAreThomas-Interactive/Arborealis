package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ArborealisPersistentState;
import com.youarethomas.arborealis.mixin_access.CameraMixinAccess;
import com.youarethomas.arborealis.util.ArborealisNbt;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import oshi.util.tuples.Pair;

import java.util.*;

public class WarpCoreEntity extends BlockEntity {

    private static final float PASSWORD_RADIUS = 0.6f;
    private static final int PASSWORD_NUM_PARTICLES = 10;

    private List<BlockPos> warpCorePositions = new ArrayList<>();
    private final List<Pair<BlockPos, Direction>> passwordBlockPosList;
    private static HashMap<String, Boolean> allowPlayerTeleport = new HashMap<>();
    public int fadeAmount = 0;
    private BlockPos selectedWarpCore = BlockPos.ORIGIN;
    private static BlockPos currentlyTeleportingTo = null;

    private static boolean playTeleportSound = false;

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
            if (world.getBlockState(passwordPair.getA()).isOf(Arborealis.WARP_WOOD) || world.getBlockState(passwordPair.getA()).isOf(Arborealis.WARP_LOG) || world.getBlockState(passwordPair.getA()).isOf(Arborealis.CARVED_LOG))
                createPasswordParticles(passwordPair.getA(), passwordPair.getB(), world);
        }

        if (playTeleportSound) {
            world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1f, 0.5f, false);
            playTeleportSound = false;
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, WarpCoreEntity be) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Vec3d thisCore = Vec3d.ofCenter(pos);

        CameraMixinAccess cameraAccess = (CameraMixinAccess)MinecraftClient.getInstance().gameRenderer.getCamera();

        if (player != null) {
            if (player.getBoundingBox().intersects(new Box(new Vec3d(thisCore.getX() - 1D, thisCore.getY() + 1D, thisCore.getZ() - 1D), new Vec3d(thisCore.getX() + 1D, thisCore.getY() + 3D, thisCore.getZ() + 1D)))) {
                ArborealisPersistentState worldNbt = ((ServerWorld) world).getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");

                List<BlockPos> positions = new ArrayList<>(worldNbt.getWarpCoreList());
                positions.remove(pos);
                be.setOtherCorePositions(positions);

                if (positions.size() > 0) {
                    // Get player look direction
                    Vec3d playerLookVector = player.getRotationVecClient();
                    double similarity = -1d;
                    be.setSelectedWarpCore(BlockPos.ORIGIN);

                    for (BlockPos corePos : positions) {
                        Vec3d playerToBlock = Vec3d.ofCenter(corePos).subtract(player.getEyePos()).normalize();

                        double dot = playerToBlock.dotProduct(playerLookVector);
                        if (dot > 0.995 && dot > similarity) {
                            similarity = playerToBlock.dotProduct(playerLookVector);
                            be.setSelectedWarpCore(corePos);
                        }
                    }

                    if (player.isSneaking()) {
                        if (be.getSelectedWarpCore() != BlockPos.ORIGIN && allowPlayerTeleport.get(player.getEntityName()) != null && allowPlayerTeleport.get(player.getEntityName())) {
                            currentlyTeleportingTo = be.getSelectedWarpCore();
                        }
                    } else {
                        allowPlayerTeleport.put(player.getEntityName(), true);
                        currentlyTeleportingTo = null;
                    }
                }

                if (currentlyTeleportingTo != null) {
                    ServerPlayerEntity serverPlayer = ArborealisUtil.getServerPlayer(world);
                    if (cameraAccess.getCameraOffset() > -1f) {
                        serverPlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 30, false, false, false));
                        cameraAccess.setCameraOffset(cameraAccess.getCameraOffset() - 0.1f);
                    } else {
                        allowPlayerTeleport.put(player.getEntityName(), false);
                        playTeleportSound = true;
                        serverPlayer.teleport(currentlyTeleportingTo.getX() + 0.5D, currentlyTeleportingTo.up().getY(), currentlyTeleportingTo.getZ() + 0.5D);
                        currentlyTeleportingTo = null;
                    }
                } else {
                    if (cameraAccess.getCameraOffset() < 0f)
                        cameraAccess.setCameraOffset(cameraAccess.getCameraOffset() + 0.05f);
                }
            } else {
                allowPlayerTeleport.put(player.getEntityName(), true);
            }
        }
    }

    public void setOtherCorePositions(List<BlockPos> positions) {
        this.warpCorePositions = positions;
        this.markDirty();
    }

    public List<BlockPos> getOtherCorePositions() {
        return warpCorePositions;
    }

    public void setSelectedWarpCore(BlockPos pos) {
        this.selectedWarpCore = pos;
        this.markDirty();
    }

    public BlockPos getSelectedWarpCore() {
        return this.selectedWarpCore;
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        tag.put("warp_cores", ArborealisNbt.serializeBlockPosList(warpCorePositions));
        tag.put("selected_core", NbtHelper.fromBlockPos(selectedWarpCore));
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        warpCorePositions = ArborealisNbt.deserializeBlockPosList(tag.getList("warp_cores", NbtElement.COMPOUND_TYPE));
        selectedWarpCore = NbtHelper.toBlockPos(tag.getCompound("selected_core"));

        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.REDRAW_ON_MAIN_THREAD);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    public static void createPasswordParticles(BlockPos pos, Direction direction, World world) {
        // Create points randomly on the circumference of the circle.
        for (int i = 0; i < PASSWORD_NUM_PARTICLES; ++i) {
            int particleSample = Arborealis.RANDOM.nextInt(100);

            if(particleSample < 3) {
                double angle = 2.0f * Math.PI * Arborealis.RANDOM.nextDouble();

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

                world.addParticle(Arborealis.WARP_TREE_PARTICLE, particlePos.x, particlePos.y - 0.1, particlePos.z, velocity.x, velocity.y, velocity.z);
            }
        }
    }
}
