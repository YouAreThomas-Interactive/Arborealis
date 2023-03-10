package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ImplementedInventory;
import com.youarethomas.arborealis.recipes.InfusionRecipe;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
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
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Optional;

public class ProjectorBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private int lightLevel;
    private int throwDistance;
    private BlockPos lastBlock;

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PROJECTOR_ENTITY, pos, state);
    }

    public void setLightLevel(int lightLevel) {
        this.lightLevel = lightLevel;
        markDirty();
        updateProjector();
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public void setThrowDistance(int throwDistance) {
        this.throwDistance = throwDistance;
        markDirty();
        updateProjector();
    }

    public int getThrowDistance() {
        return this.throwDistance;
    }

    public void setLastBlock(BlockPos lastBlock) {
        this.lastBlock = lastBlock;
        markDirty();
    }

    public BlockPos getLastBlock() {
        return this.lastBlock;
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

    @Override
    public void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);

        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        updateProjector();
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removedStack = Inventories.removeStack(getItems(), slot);
        updateProjector();
        return removedStack;
    }

    public void updateProjector() {
        Direction facing = getCachedState().get(HorizontalFacingBlock.FACING);

        ItemStack itemStack = getStack(0);

        // Clear the run off a carved log, or stop a lens infusion
        if (getLastBlock() != null) {
            BlockState lastState = world.getBlockState(getLastBlock());
            if (lastState.isOf(Arborealis.CARVED_LOG) || lastState.isOf(Arborealis.CARVED_NETHER_LOG)) {
                resetProjectedRune(getLastBlock(), facing);
            } else if (lastState.isOf(Arborealis.HOLLOWED_LOG)) {
                HollowedLogEntity entity = (HollowedLogEntity) world.getBlockEntity(getLastBlock());
                entity.setXpRequired(0);
            }
        }

        if (itemStack.isOf(Arborealis.CARVED_STENCIL) && getLightLevel() > 0 && getThrowDistance() > 0) {
            NbtCompound nbt = itemStack.getNbt();

            if (nbt != null && nbt.contains("pattern")) {
                int[] pattern = nbt.getIntArray("pattern");

                for (int offset = 1; offset <= getThrowDistance() + 1; offset++) {
                    BlockPos testPos = pos.offset(facing, offset);
                    BlockState stateAtPos = world.getBlockState(testPos);

                    // If the block clicked on is wood, create a new carved wood block
                    if (stateAtPos.isIn(BlockTags.LOGS) || stateAtPos.isOf(Blocks.PUMPKIN)) {
                        // Swap the block out with a carved wood block...
                        if (stateAtPos.isIn(BlockTags.LOGS_THAT_BURN)) {
                            world.setBlockState(testPos, Arborealis.CARVED_LOG.getDefaultState());
                        } else {
                            world.setBlockState(testPos, Arborealis.CARVED_NETHER_LOG.getDefaultState());
                        }

                        CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(testPos);

                        // ... and assign relevant NBT data
                        if (carvedLog != null) {
                            carvedLog.setLogState(stateAtPos);

                            carvedLog.setFaceCatalysed(facing.getOpposite(), true);
                            carvedLog.projectLightRune(facing.getOpposite(), pattern);
                        }

                        setLastBlock(testPos); // Store block that was last rune'd
                    } else if (stateAtPos.isIn(Arborealis.CARVED_LOGS)) {
                        CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(testPos);

                        if (carvedLog != null) {
                            carvedLog.setFaceCatalysed(facing.getOpposite(), true);
                            carvedLog.projectLightRune(facing.getOpposite(), pattern);
                        }

                        setLastBlock(testPos); // Store block that was last rune'd
                    }
                }
            }
        } else {
            // If no light, remove all light carvings from blocks
            BlockPos posInFront = pos.offset(facing, 1);
            BlockPos endPos = posInFront.offset(facing, getThrowDistance() + 1);
            for (BlockPos testPos : BlockPos.iterate(posInFront, endPos)) {
                BlockState stateAtPos = world.getBlockState(testPos);

                // If the block clicked on is wood, create a new carved wood block
                if (stateAtPos.isOf(Arborealis.CARVED_LOG) || stateAtPos.isOf(Arborealis.CARVED_NETHER_LOG)) {
                    resetProjectedRune(testPos, facing);
                    break;
                }
            }
        }
    }

    private void resetProjectedRune(BlockPos pos, Direction projectorFacing) {
        CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(pos);

        if (carvedLog != null) {
            carvedLog.setFaceCatalysed(projectorFacing.getOpposite(), false);
            carvedLog.projectLightRune(projectorFacing.getOpposite(), new int[49]);
        }

        boolean blockReset = true;
        for (Direction dir : Direction.values()) {
            if (!Arrays.deepEquals(ArrayUtils.toObject(carvedLog.getFaceArray(dir)), ArrayUtils.toObject(new int[49]))) {
                blockReset = false;
            }
        }

        // If no sides are carved, reset to respective log block.
        if (blockReset) {
            if (!world.isClient) {
                world.setBlockState(pos, carvedLog.getLogState());
            }
        }

        setLastBlock(null);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ProjectorBlockEntity pbe) {
        BlockPos blockBehind = pos.offset(state.get(HorizontalFacingBlock.FACING).getOpposite());
        Direction facing = state.get(HorizontalFacingBlock.FACING);

        if (!world.getBlockState(blockBehind).isOf(Blocks.AIR)) {
            int lightBehind = world.getLightLevel(LightType.BLOCK, blockBehind);
            if (lightBehind != pbe.getLightLevel()) {
                pbe.setLightLevel(lightBehind);
            }
        } else {
            if (pbe.getLightLevel() != 0) {
                pbe.setLightLevel(0);
            }
        }

        if (pbe.getLightLevel() != 0) {
            int beamRange = -1;
            for (int i = 0; i < pbe.getLightLevel(); i++) {
                BlockPos testPos = pos.offset(facing, i + 1);

                if (!world.getBlockState(testPos).isIn(Arborealis.PROJECTOR_TRANSPARENT)) {
                    beamRange = i;
                    break;
                }
            }

            // Set beam length to distance, otherwise if it was never blockers cap it at the light source level
            if (pbe.getThrowDistance() != beamRange)
                pbe.setThrowDistance(beamRange == -1 ? pbe.getLightLevel() : beamRange);
        }

        // Infusion crafting
        // TODO: Doesn't stop when projector is turned off
        if (pbe.getLightLevel() > 0 && pbe.getThrowDistance() > 0 && pbe.getStack(0).isOf(Arborealis.INFUSION_LENS)) {
            BlockPos posInFront = pos.offset(facing, 1);
            BlockPos endPos = posInFront.offset(facing, pbe.getThrowDistance() + 1);

            for (BlockPos testPos : BlockPos.iterate(posInFront, endPos)) {
                BlockState stateAtPos = world.getBlockState(testPos);

                if (stateAtPos.isOf(Arborealis.HOLLOWED_LOG)) {
                    HollowedLogEntity hollowedLogEntity = (HollowedLogEntity) world.getBlockEntity(testPos);

                    if (hollowedLogEntity != null) {
                        Optional<InfusionRecipe> recipeMatch = world.getRecipeManager().getFirstMatch(InfusionRecipe.Type.INSTANCE, hollowedLogEntity, world);
                        recipeMatch.ifPresent(infusionRecipe -> hollowedLogEntity.setXpRequired(infusionRecipe.getXpRequired()));
                    }

                    break;
                }
            }
        }
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, inventory);

        tag.putInt("light_level", lightLevel);
        tag.putInt("throw_distance", throwDistance);
        if (lastBlock != null)
            tag.put("last_block", NbtHelper.fromBlockPos(lastBlock));
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        inventory.clear(); // Got to clear the inventory first
        Inventories.readNbt(tag, inventory);

        lightLevel = tag.getInt("light_level");
        throwDistance = tag.getInt("throw_distance");
        if (tag.contains("last_block"))
            lastBlock = NbtHelper.toBlockPos(tag.getCompound("last_block"));

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
