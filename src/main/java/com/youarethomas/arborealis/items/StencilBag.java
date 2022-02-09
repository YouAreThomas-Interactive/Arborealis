package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.ImplementedInventory;
import com.youarethomas.arborealis.util.RuneManager;
import gui.StencilBagScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
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

public class StencilBag extends Item implements NamedScreenHandlerFactory, ImplementedInventory, DyeableItem {
    public static final int BAG_SLOTS = 18;
    public static ItemStack openBag; // The active bag that's opened by the player

    public StencilBag(Settings settings) {
        super(settings);
    }

    @Override
    public int getColor(ItemStack stack) {
        NbtCompound nbtCompound = stack.getSubNbt(DISPLAY_KEY);
        if (nbtCompound != null && nbtCompound.contains(COLOR_KEY, 99)) {
            return nbtCompound.getInt(COLOR_KEY);
        }
        return 0xFFFFFF;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        // Can probably replace with something else... use is a bit shit
        openBag = player.getStackInHand(hand);

        if (!world.isClient) {
            // If you right-click the bag in the air...
            if (Screen.hasShiftDown()) {
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

        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(StencilBag.BAG_SLOTS, ItemStack.EMPTY);
        NbtCompound nbt = player.getMainHandStack().getNbt();

        if (nbt != null) {
            Inventories.readNbt(nbt, inventory);

            if (nbt.contains("selected")) {
                int selectedSlot = nbt.getInt("selected");
                ItemStack stack = inventory.get(selectedSlot);

                if (stack.isOf(Arborealis.CARVED_STENCIL)) {
                    StencilCarved stencil = (StencilCarved) stack.getItem();
                    stencil.useStencil(stack, world, blockState, blockPos, context.getSide());
                    if (player.getOffHandStack().isOf(Arborealis.CARVING_KNIFE)) {
                        CarvingKnife.carve(world, (CarvedLogEntity)world.getBlockEntity(blockPos), context.getSide(), player, player.getOffHandStack(), blockPos, Hand.OFF_HAND);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {

            DefaultedList<ItemStack> inventory = DefaultedList.ofSize(StencilBag.BAG_SLOTS, ItemStack.EMPTY);

            NbtCompound nbt = stack.getNbt();
            String selectedRuneName = new TranslatableText("item.arborealis.stencil_bag.none").getString();
            int numberOfRunes = 0;

            if (nbt != null) {
                Inventories.readNbt(nbt, inventory);

                for (int i = 0; i < StencilBag.BAG_SLOTS; i++) {
                    if (!getStack(i).isEmpty()) {
                        numberOfRunes++;
                    }
                }

                if (nbt.contains("selected")) {
                    // Get the item stack at the specified spot
                    ItemStack selectedStack = inventory.get(nbt.getInt("selected"));
                    if (!selectedStack.isEmpty()) {
                        NbtCompound stencilNbt = selectedStack.getNbt();

                        // Same code as in StencilCarved to get the rune from the pattern
                        if (stencilNbt != null && stencilNbt.contains("pattern")) {
                            int[] pattern = stencilNbt.getIntArray("pattern");
                            pattern = Arrays.stream(pattern).map(i -> i == 2 ? 1 : i).toArray();

                            if (RuneManager.isValidRune(pattern)) {
                                AbstractRune rune = RuneManager.getRuneFromArray(pattern);

                                selectedRuneName = new TranslatableText("rune.arborealis." + rune.name).getString(); // Set to the rune name (retains colour too)
                            } else {
                                selectedRuneName = new TranslatableText("rune.arborealis.unknown").getString();
                            }
                        }

                        // If the stencil is renamed, append the name of the item
                        if (selectedStack.hasCustomName()) {
                            selectedRuneName = String.format("%s %s", selectedRuneName, String.format("(%s)", selectedStack.getName().getString()));
                        }
                    }
                }
            }

            tooltip.add(new TranslatableText("item.arborealis.stencil_bag.tooltip1", numberOfRunes, selectedRuneName));
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
