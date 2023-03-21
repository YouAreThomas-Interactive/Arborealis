package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ImplementedInventory;
import net.minecraft.block.*;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class ProjectorBlockEntity extends BeamEmittingBlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public ProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(Arborealis.PROJECTOR_ENTITY, pos, state);

        setBeamActive(state.get(HorizontalFacingBlock.FACING), true);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);

        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        // Assign pattern if stencil
        if (stack.isOf(Arborealis.CARVED_STENCIL)) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("pattern")) {
                int[] pattern = nbt.getIntArray("pattern");
                setStencilPattern(pattern);
            }
            setBeamModifier(BeamModifier.STENCIL);
        } else if (stack.isOf(Arborealis.INFUSION_LENS)) {
            setBeamModifier(BeamModifier.INFUSION);
        }

        markDirty();
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removedStack = Inventories.removeStack(getItems(), slot);
        setStencilPattern(new int[49]);
        setBeamModifier(BeamModifier.NONE);
        markDirty();
        recalculateAllBeams();
        return removedStack;
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ProjectorBlockEntity be) {
        be.createBeamParticles(world, pos, state, be);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ProjectorBlockEntity pbe) {
        BlockPos blockBehind = pos.offset(state.get(HorizontalFacingBlock.FACING).getOpposite());

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

        pbe.recalculateAllBeams();
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, inventory);
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        inventory.clear(); // Got to clear the inventory first
        Inventories.readNbt(tag, inventory);

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
}
