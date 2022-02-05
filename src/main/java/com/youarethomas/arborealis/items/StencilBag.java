package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.ImplementedInventory;
import com.youarethomas.arborealis.util.RuneManager;
import gui.StencilBagScreenHandler;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class StencilBag extends Item implements NamedScreenHandlerFactory, ImplementedInventory {
    private ItemStack openBag;
    //private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

    public StencilBag(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        openBag = player.getStackInHand(hand);

        if (!world.isClient) {
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
        //System.out.println("Get items");
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);

        NbtCompound nbt = openBag.getOrCreateNbt();
        Inventories.readNbt(nbt, inventory);

        return inventory;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        System.out.println("Set Stack");
        DefaultedList<ItemStack> inventory = this.getItems();

        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        saveItems(inventory);
    }

    @Override
    public ItemStack removeStack(int slot) {
        System.out.println("Remove stack 1");
        DefaultedList<ItemStack> inventory = this.getItems();
        ItemStack removedStack = Inventories.removeStack(inventory, slot);

        saveItems(inventory);

        return removedStack;
    }

    @Override
    public ItemStack removeStack(int slot, int count) {
        System.out.println("Remove stack 2");
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
        return new TranslatableText(this.getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new StencilBagScreenHandler(syncId, inv, this);
    }
}
