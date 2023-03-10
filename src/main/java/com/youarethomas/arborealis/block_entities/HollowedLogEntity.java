package com.youarethomas.arborealis.block_entities;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ImplementedInventory;
import com.youarethomas.arborealis.recipes.InfusionRecipe;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.Optional;

public class HollowedLogEntity extends BlockEntity implements ImplementedInventory {
    public DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    private BlockState logState = Blocks.OAK_LOG.getDefaultState();
    private int xpRequired = 0;
    private int xpConsumed = 0;

    public HollowedLogEntity(BlockPos pos, BlockState state) {
        super(Arborealis.HOLLOWED_LOG_ENTITY, pos, state);
    }

    public void setLogState(BlockState logState) {
        this.logState = logState;
        this.markDirty();
    }

    public BlockState getLogState() {
        return logState;
    }

    public void setXpRequired(int xpRequired) {
        this.xpRequired = xpRequired;
        this.markDirty();
    }

    public int getXpRequired() {
        return xpRequired;
    }

    public void setXpConsumed(int xpConsumed) {
        this.xpConsumed = xpConsumed;
        this.markDirty();
    }

    public int getXpConsumed() {
        return xpConsumed;
    }

    // Serialize the BlockEntity - storing data
    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        Inventories.writeNbt(tag, inventory, true);

        tag.put("log_state", NbtHelper.fromBlockState(logState));
        tag.putInt("xp_required", xpRequired);
        tag.putInt("xp_consumed", xpConsumed);
    }

    // Deserialize the BlockEntity - retrieving data
    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        inventory.clear(); // Got to clear the inventory first
        Inventories.readNbt(tag, inventory);

        logState = NbtHelper.toBlockState(tag.getCompound("log_state"));
        xpRequired = tag.getInt("xp_required");
        xpConsumed = tag.getInt("xp_consumed");
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction side) {
        return getStack(0).isEmpty();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient())
                ((ServerWorld) world).getChunkManager().markForUpdate(getPos());
            else
                world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
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

    public static void clientTick(World world, BlockPos pos, BlockState blockState, HollowedLogEntity hollowedLogEntity) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (hollowedLogEntity.getXpRequired() != 0 && player.totalExperience > 0) {
            world.playSound(player, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.3f, (Arborealis.RANDOM.nextFloat() - Arborealis.RANDOM.nextFloat()) * 0.35f + 0.6f);
        }
    }

    public static void serverTick(World world, BlockPos pos, BlockState blockState, HollowedLogEntity hollowedLogEntity) {
        // If an item is trying to be infused...
        if (hollowedLogEntity.getXpRequired() != 0) {
            List<Entity> playersAround = ArborealisUtil.getEntitiesInRadius(world, Vec3d.ofCenter(pos), CarvedLogEntity.RUNE_BASE_RADIUS, true);
            Optional<InfusionRecipe> recipeMatch = world.getRecipeManager().getFirstMatch(InfusionRecipe.Type.INSTANCE, hollowedLogEntity, world);

            if (recipeMatch.isPresent()) {
                for (Entity entity : playersAround) {
                    if (entity instanceof PlayerEntity player) {
                        if (player.totalExperience > 0) {
                            player.addExperience(-1);
                            hollowedLogEntity.setXpConsumed(hollowedLogEntity.getXpConsumed() + 1);
                        }

                        if (hollowedLogEntity.getXpConsumed() >= hollowedLogEntity.getXpRequired()) {
                            hollowedLogEntity.setStack(0, recipeMatch.get().getOutput().copy());

                            hollowedLogEntity.setXpRequired(0);
                            hollowedLogEntity.setXpConsumed(0);
                            world.updateListeners(pos, blockState, blockState, Block.NOTIFY_ALL);
                            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(blockState));
                        }
                    }
                }
            } else {
                divideXP(hollowedLogEntity, playersAround);
            }
        } else if (hollowedLogEntity.getXpConsumed() != 0) {
            // If the required is zero, but there is XP consumed (infusion cancelled)
            List<Entity> playersAround = ArborealisUtil.getEntitiesInRadius(world, Vec3d.ofCenter(pos), CarvedLogEntity.RUNE_BASE_RADIUS, true);
            divideXP(hollowedLogEntity, playersAround);
        }
    }

    private static void divideXP(HollowedLogEntity hollowedLogEntity, List<Entity> playersAround) {
        // Redistribute XP to all surrounding players
        if (playersAround.size() > 0) {
            int divideXp = hollowedLogEntity.getXpConsumed() / playersAround.size();

            for (Entity entity : playersAround) {
                if (entity instanceof PlayerEntity player) {
                    player.addExperience(divideXp);
                }
            }

            hollowedLogEntity.setXpRequired(0);
            hollowedLogEntity.setXpConsumed(0);
        }
    }
}
