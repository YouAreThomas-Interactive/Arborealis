package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ImplementedInventory;
import com.youarethomas.arborealis.util.RuneManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ProjectorBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private int lightLevel;
    private int throwDistance;
    private int infusionTime = 0;
    private int infusionTotalTime = 80; // Time in ticks

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PROJECTOR_ENTITY, pos, state);
    }

    public void setInfusionTime(int infusionTime) {
        this.infusionTime = infusionTime;
        markDirty();
    }

    public int getInfusionTime() {
        return this.infusionTime;
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
        markDirty();
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public void setThrowDistance(int throwDistance) {
        this.throwDistance = throwDistance;
        markDirty();
    }

    public int getThrowDistance() {
        return this.throwDistance;
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ProjectorBlockEntity be) {
        Random random = world.random;

        // Particle chance scales depending on how long the beam is
        if (be.getThrowDistance() > 0 && be.getLightLevel() > 0 && random.nextInt(be.getLightLevel() * 4) < be.getThrowDistance()) {
            // Get the box for the beam
            Direction.Axis facingAxis = state.get(HorizontalFacingBlock.FACING).getAxis();
            Box beamBox = new Box(pos.offset(facingAxis, 1), pos.offset(state.get(HorizontalFacingBlock.FACING), 1 + be.getThrowDistance()));

            // Calculate a random coordinate within that box
            double randX = beamBox.minX + (((beamBox.maxX + 1) - beamBox.minX) * random.nextFloat());
            double randY = beamBox.minY + (((beamBox.maxY + 1) - beamBox.minY) * random.nextFloat());
            double randZ = beamBox.minZ + (((beamBox.maxZ + 1) - beamBox.minZ) * random.nextFloat());

            world.addParticle(ParticleTypes.END_ROD, randX, randY, randZ, 0, 0, 0);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ProjectorBlockEntity pbe) {
        // TODO: Change to lit and unlit server tick like in CampfireBlock
        BlockPos blockBehind = pos.offset(state.get(HorizontalFacingBlock.FACING).getOpposite());
        Direction facing = state.get(HorizontalFacingBlock.FACING);

        if (!world.getBlockState(blockBehind).isOf(Blocks.AIR)) {
            int lightBehind = world.getLightLevel(LightType.BLOCK, blockBehind);
            if (lightBehind != pbe.getLightLevel()) {
                // TODO: Would be nice to move this into neighbour update, but the light level doesn't update before it's called
                pbe.setLightLevel(lightBehind);
            }
        } else {
            if (pbe.getLightLevel() != 0) {
                pbe.setLightLevel(0);
            }
        }

        if (pbe.getLightLevel() != 0) {

            boolean blockFound = false;
            for (int i = 0; i < pbe.getLightLevel(); i++) {
                BlockPos testPos = pos.offset(facing, i + 1);

                if (!world.getBlockState(testPos).isOf(Blocks.AIR)) {
                    pbe.setThrowDistance(i);
                    blockFound = true;
                    break;
                }
            }

            if (!blockFound)
                pbe.setThrowDistance(pbe.getLightLevel());
        }

        // Get current item
        ItemStack itemStack = pbe.getStack(0);

        // Project the light map thing onto the blocko
        if (pbe.getLightLevel() > 0) {
            if (itemStack.isOf(Arborealis.CARVED_STENCIL)) {
                NbtCompound nbt = itemStack.getNbt();
                if (nbt != null && nbt.contains("pattern")) {
                    int[] pattern = nbt.getIntArray("pattern");

                    BlockPos posInFront = pos.offset(facing, 1);
                    BlockPos endPos = posInFront.offset(facing, pbe.getThrowDistance() + 1);
                    for (BlockPos testPos : BlockPos.iterate(posInFront, endPos)) {
                        // TODO: Optimise
                        BlockState stateAtPos = world.getBlockState(testPos);

                        // If the block clicked on is wood, create a new carved wood block
                        if (stateAtPos.isIn(BlockTags.LOGS) || stateAtPos.isOf(Blocks.PUMPKIN)) {
                            // Swap the block out with a carved wood block...
                            if (stateAtPos.isIn(BlockTags.LOGS_THAT_BURN)) {
                                world.setBlockState(testPos, Arborealis.CARVED_LOG.getDefaultState());
                            } else {
                                world.setBlockState(testPos, Arborealis.CARVED_NETHER_LOG.getDefaultState());
                            }

                            CarvedLogEntity carvedLogEntity = (CarvedLogEntity) world.getBlockEntity(testPos);

                            // ... and assign relevant NBT data
                            if (carvedLogEntity != null) {
                                carvedLogEntity.setLogState(stateAtPos);
                            }

                            if (pbe.getThrowDistance() > 0 && pbe.getLightLevel() > 0) {
                                carvedLogEntity.setFaceCatalysed(facing.getOpposite(), true);
                                carvedLogEntity.projectLightRune(facing.getOpposite(), pattern);
                            } else {
                                carvedLogEntity.setFaceCatalysed(facing.getOpposite(), false); // Light catalysed should be different to normal
                                carvedLogEntity.projectLightRune(facing.getOpposite(), new int[49]);
                            }
                        } else if (stateAtPos.isOf(Arborealis.CARVED_LOG) || stateAtPos.isOf(Arborealis.CARVED_NETHER_LOG)) {
                            CarvedLogEntity be = (CarvedLogEntity) world.getBlockEntity(testPos);

                            if (pbe.getThrowDistance() > 0 && pbe.getLightLevel() > 0) {
                                be.setFaceCatalysed(facing.getOpposite(), true);
                                be.projectLightRune(facing.getOpposite(), pattern);
                            } else {
                                be.setFaceCatalysed(facing.getOpposite(), false);
                                be.projectLightRune(facing.getOpposite(), new int[49]);
                            }
                        }
                    }
                }
            } else if (itemStack.isOf(Arborealis.INFUSION_LENS)) {
                // TODO: Reset infusion if interrupted
                BlockPos posInFront = pos.offset(facing, 1);
                BlockPos endPos = posInFront.offset(facing, pbe.getThrowDistance() + 1);
                for (BlockPos testPos : BlockPos.iterate(posInFront, endPos)) {
                    BlockState stateAtPos = world.getBlockState(testPos);

                    if (stateAtPos.isOf(Arborealis.HOLLOWED_LOG)) {
                        HollowedLogEntity hollowedLogEntity = (HollowedLogEntity)world.getBlockEntity(testPos);

                        if (hollowedLogEntity.getStack(0).isOf(Arborealis.BOTTLED_SAP)) {
                            pbe.setInfusionTime(pbe.getInfusionTime() + 1);
                            if (pbe.getInfusionTime() > pbe.infusionTotalTime) {
                                hollowedLogEntity.setStack(0, Arborealis.INFUSED_SAP.getDefaultStack().split(1));
                                pbe.setInfusionTime(0);
                                world.updateListeners(testPos, stateAtPos, stateAtPos, Block.NOTIFY_ALL);
                                world.emitGameEvent(GameEvent.BLOCK_CHANGE, testPos, GameEvent.Emitter.of(stateAtPos));
                            }
                        }
                    }
                }
            }
        }
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, inventory);

        tag.putInt("infusion_time", infusionTime);
        tag.putInt("light_level", lightLevel);
        tag.putInt("throw_distance", throwDistance);
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        inventory.clear(); // Got to clear the inventory first
        Inventories.readNbt(tag, inventory);

        infusionTime = tag.getInt("infusion_time");
        lightLevel = tag.getInt("light_level");
        throwDistance = tag.getInt("throw_distance");

        this.markDirty();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL | Block.FORCE_STATE);
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

    public static void updateProjector(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        BlockPos pos = packetByteBuf.readBlockPos();
        ProjectorBlockEntity pbe = (ProjectorBlockEntity) minecraftClient.world.getBlockEntity(pos);

        minecraftClient.execute(() -> {
            pbe.setLightLevel(packetByteBuf.readInt());
        });
    }
}
