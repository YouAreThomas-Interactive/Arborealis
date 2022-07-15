package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.ArborealisClient;
import com.youarethomas.arborealis.misc.ArborealisPersistentState;
import com.youarethomas.arborealis.mixin_access.CameraMixinAccess;
import com.youarethomas.arborealis.util.ArborealisConstants;
import com.youarethomas.arborealis.util.ArborealisNbt;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

public class WarpCoreEntity extends BlockEntity {

    private static final float PASSWORD_RADIUS = 0.6f;
    private static final int PASSWORD_NUM_PARTICLES = 10;

    private Map<BlockPos, String> otherWarpCores = new HashMap<>();
    private final Map<BlockPos, Direction> corePassPosList;

    public int fadeAmount = 0;

    private BlockPos selectedWarpCore = BlockPos.ORIGIN;
    private static boolean justTeleported = false;

    public WarpCoreEntity(BlockPos pos, BlockState state) {
        super(Arborealis.WARP_CORE_ENTITY, pos, state);

        corePassPosList = new HashMap<>();

        for(Direction dir : new Direction[]{ Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST }) {
            Vec3i dirVec = dir.getVector().multiply(2).add(Direction.UP.getVector().multiply(2));

            corePassPosList.put(pos.add(dirVec), dir.getOpposite());
        }
    }

    @Environment(EnvType.CLIENT)
    public static void clientTick(World world, BlockPos pos, BlockState state, WarpCoreEntity be) {
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;

        // Only run on cores where the player is in the chamber
        if (clientPlayer != null && ArborealisUtil.isWithinRadius(clientPlayer.getPos(), Vec3d.ofCenter(pos), 2)) {
            // Create the password particle rings
            for(Map.Entry<BlockPos, Direction> passwordBlock : be.corePassPosList.entrySet()) {
                if (world.getBlockState(passwordBlock.getKey()).isOf(Arborealis.WARP_WOOD) || world.getBlockState(passwordBlock.getKey()).isOf(Arborealis.WARP_LOG) || world.getBlockState(passwordBlock.getKey()).isOf(Arborealis.CARVED_LOG))
                    createPasswordParticles(passwordBlock.getKey(), passwordBlock.getValue(), world);
            }

            CameraMixinAccess cameraAccess = (CameraMixinAccess)MinecraftClient.getInstance().gameRenderer.getCamera();

            if (clientPlayer.isSneaking() && !justTeleported) {
                if (!be.getSelectedWarpCore().equals(BlockPos.ORIGIN)) {
                    if (cameraAccess.getCameraOffset() > -1f) {
                        cameraAccess.setCameraOffset(cameraAccess.getCameraOffset() - 0.1f);
                    } else {
                        System.out.println("Teleport! Boom!");
                        world.playSound(clientPlayer, be.getSelectedWarpCore(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.BLOCKS, 0.6f, 0.7f);
                        justTeleported = true;

                        // Send teleport request
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeBlockPos(be.getSelectedWarpCore());
                        ClientPlayNetworking.send(ArborealisConstants.WARP_TREE_TELEPORT, buf);
                    }
                }
            } else {
                if (cameraAccess.getCameraOffset() < 0f)
                    cameraAccess.setCameraOffset(cameraAccess.getCameraOffset() + 0.05f);
                else
                    justTeleported = false;
            }
        }

    }

    public static void serverTick(World world, BlockPos pos, BlockState state, WarpCoreEntity be) {
        Vec3d thisCore = Vec3d.ofCenter(pos);

        // Warp core list stored in world nbt
        ArborealisPersistentState worldNbt = ((ServerWorld) world).getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");

        // Get all warp cores
        Map<BlockPos, String> otherCorePositions = new HashMap<>(worldNbt.getWarpCoreList());
        otherCorePositions.remove(pos); // remove this core

        for (Entity playerEntity : ArborealisUtil.getEntitiesInRadius(world, Vec3d.ofCenter(pos), 2, true)) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) playerEntity;

            // If the player is within the warp chamber...
            if (serverPlayer.getBoundingBox().intersects(new Box(new Vec3d(thisCore.getX() - 1D, thisCore.getY() + 1D, thisCore.getZ() - 1D), new Vec3d(thisCore.getX() + 1D, thisCore.getY() + 3D, thisCore.getZ() + 1D)))) {

                // Get a list of this warp core's password blocks
                Map<Direction, int[]> thisCorePasses = new HashMap<>();
                for (Map.Entry<BlockPos, Direction> thisCorePass : be.corePassPosList.entrySet()) {
                    if (world.getBlockState(thisCorePass.getKey()).isOf(Arborealis.CARVED_LOG)) {
                        CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(thisCorePass.getKey());
                        thisCorePasses.put(thisCorePass.getValue(), carvedLog.getFaceArray(thisCorePass.getValue()));
                    }
                }

                List<BlockPos> corePosList = new ArrayList<>(otherCorePositions.keySet());
                List<BlockPos> passedCorePosList = new ArrayList<>();

                // Filter to only show cores that contain a matching symbol
                // Also filter out all cores that would appear clumped up (take the closest one in the clump).
                for (BlockPos corePos : corePosList) {
                    boolean otherCoreSharesNetwork = false;
                    WarpCoreEntity otherCoreEntity = (WarpCoreEntity)world.getBlockEntity(corePos);

                    if (otherCoreEntity != null) {
                        // Get all password blocks from the other boyo
                        Map<BlockPos, Direction> otherCorePasses = otherCoreEntity.corePassPosList;

                        Map<Direction, int[]> otherCorePassRunes = new HashMap<>();
                        for (Map.Entry<BlockPos, Direction> thisCorePass : otherCorePasses.entrySet()) {
                            if (world.getBlockState(thisCorePass.getKey()).isOf(Arborealis.CARVED_LOG)) {
                                CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(thisCorePass.getKey());
                                otherCorePassRunes.put(thisCorePass.getValue(), carvedLog.getFaceArray(thisCorePass.getValue()));
                            }
                        }

                        if (!otherCorePassRunes.values().stream().allMatch(ints -> Arrays.equals(ints, new int[49]))) {
                            // Check all blocks to see if any of our current tree's password runes are found
                            for (Map.Entry<BlockPos, Direction> otherCorePass : otherCorePasses.entrySet()) {
                                if (world.getBlockState(otherCorePass.getKey()).isOf(Arborealis.CARVED_LOG)) {
                                    CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(otherCorePass.getKey());
                                    if (thisCorePasses.values().stream().anyMatch(ints -> Arrays.equals(ints, carvedLog.getFaceArray(otherCorePass.getValue())))) {
                                        otherCoreSharesNetwork = true;
                                        break;
                                    }
                                }
                            }

                            if (!otherCoreSharesNetwork)
                                otherCorePositions.remove(corePos);
                        }

                        // Only check if it is clumped up if it has the same password.
                        if(otherCorePositions.containsKey(corePos)) {
                            for(BlockPos passedCorePos : new ArrayList<>(passedCorePosList)) {
                                Vec3d playerToPassed = Vec3d.ofCenter(passedCorePos).subtract(serverPlayer.getEyePos());
                                Vec3d playerToOther = Vec3d.ofCenter(corePos).subtract(serverPlayer.getEyePos());
                                double dot = playerToPassed.normalize().dotProduct(playerToOther.normalize());

                                if(dot > 0.99) {
                                    // Keep the element that closer, remove the further one.
                                    if(playerToPassed.lengthSquared() > playerToOther.lengthSquared()) {
                                        // Keeping the other core (the one being filtered).
                                        passedCorePosList.remove(passedCorePos);
                                        otherCorePositions.remove(passedCorePos);
                                    } else {
                                        // Keeping the one that is passed.
                                        otherCorePositions.remove(corePos);
                                    }
                                }
                            }
                        }

                        // Keep track of the cores that pass the filter.
                        if(otherCorePositions.containsKey(corePos)) {
                            passedCorePosList.add(corePos.mutableCopy());
                        }
                    }
                }

                be.setOtherCorePositions(otherCorePositions);

                if (otherCorePositions.size() > 0) {
                    // Get player look direction
                    Vec3d playerLookVector = serverPlayer.getRotationVecClient();
                    double similarity = -1d;
                    be.setSelectedWarpCore(BlockPos.ORIGIN); // Reset selected core

                    for (Map.Entry<BlockPos, String> entry : otherCorePositions.entrySet()) {
                        BlockPos corePos = entry.getKey();
                        Vec3d playerToBlock = Vec3d.ofCenter(corePos).subtract(serverPlayer.getEyePos()).normalize();

                        double dot = playerToBlock.dotProduct(playerLookVector);
                        if (dot > 0.995 && dot > similarity) {
                            similarity = playerToBlock.dotProduct(playerLookVector);
                            be.setSelectedWarpCore(corePos);
                        }
                    }
                }
            }
        }
    }

    public static void teleportPlayer(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Vec3d pos = Vec3d.ofCenter(buf.readBlockPos().up()); // Get centre of block above core to teleport to

        player.teleport(pos.getX(), pos.getY(), pos.getZ());
    }

    public void setOtherCorePositions(Map<BlockPos, String> positions) {
        this.otherWarpCores = positions;
        this.markDirty();
    }

    public Map<BlockPos, String>getOtherCorePositions() {
        return otherWarpCores;
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

        tag.put("warp_cores", ArborealisNbt.serializeCorePosList(otherWarpCores));
        tag.put("selected_core", NbtHelper.fromBlockPos(selectedWarpCore));
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        otherWarpCores = ArborealisNbt.deserializeCorePosList(tag.getList("warp_cores", NbtElement.COMPOUND_TYPE));
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
