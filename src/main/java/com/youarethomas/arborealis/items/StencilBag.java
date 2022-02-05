package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.util.ImplementedInventory;
import gui.StencilBagScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StencilBag extends Item implements NamedScreenHandlerFactory, ImplementedInventory {
    public static final int BAG_SLOTS = 18;
    public static ItemStack openBag; // The active bag that's opened by the player

    public StencilBag(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        // Can probably replace with something else... use is a bit shit
        openBag = player.getStackInHand(hand);

        if (!world.isClient) {
            // If you shift-right click the bag...
            if (player.isSneaking()) {
                player.openHandledScreen(this);
                return TypedActionResult.success(openBag);
            }
        }

        return TypedActionResult.pass(openBag);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        BlockState blockState = world.getBlockState(blockPos);

        // If you right click the bag on a block...
        // TODO: apply active stencil

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            // TODO: Load tooltip based on NBT
            tooltip.add(new TranslatableText("item.arborealis.stencil_bag.tooltip1"));
            tooltip.add(new TranslatableText("item.arborealis.stencil_bag.tooltip2"));
            tooltip.add(new TranslatableText("item.arborealis.stencil_bag.tooltip3"));
            tooltip.add(new TranslatableText("item.arborealis.stencil_bag.tooltip4"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(StencilBag.BAG_SLOTS, ItemStack.EMPTY);

        NbtCompound nbt = openBag.getOrCreateNbt();
        Inventories.readNbt(nbt, inventory);

        return inventory;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        DefaultedList<ItemStack> inventory = this.getItems();

        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        saveItems(inventory);
    }

    @Override
    public ItemStack removeStack(int slot) {
        DefaultedList<ItemStack> inventory = this.getItems();
        ItemStack removedStack = Inventories.removeStack(inventory, slot);

        saveItems(inventory);

        return removedStack;
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        DefaultedList<ItemStack> inventory = this.getItems();

        ItemStack result = Inventories.splitStack(inventory, slot, count);
        if (!result.isEmpty()) {
            markDirty();
        }

        saveItems(inventory);

        return result;
    }

    private void saveItems(DefaultedList<ItemStack> inventory) {
        NbtCompound nbt = openBag.getOrCreateNbt();
        Inventories.writeNbt(nbt, inventory);
        openBag.setNbt(nbt);
    }

    @Override
    public Text getDisplayName() {
        return this.getName(openBag); // Copies bag item name
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new StencilBagScreenHandler(syncId, inv, this);
    }
}
