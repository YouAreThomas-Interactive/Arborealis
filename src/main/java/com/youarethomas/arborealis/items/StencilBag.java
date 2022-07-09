package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.misc.StencilBagInventory;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.gui.StencilBagScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class StencilBag extends Item implements DyeableItem {
    public static final int BAG_SLOTS = 18;

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
        ItemStack openBag = player.getStackInHand(hand);

        // If you right-click the bag in the air...
        if (player.isSneaking()) {
            player.openHandledScreen(createScreenHandlerFactory(openBag));
            return TypedActionResult.success(openBag);
        }

        return TypedActionResult.pass(openBag);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        BlockState blockState = world.getBlockState(blockPos);

        NbtCompound nbt = player.getMainHandStack().getNbt();

        if (nbt != null) {
            StencilBagInventory bagInventory = new StencilBagInventory(player.getMainHandStack());

            if (nbt.contains("selected")) {
                int selectedSlot = nbt.getInt("selected");
                ItemStack stack = bagInventory.getItems().get(selectedSlot);

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
            NbtCompound nbt = stack.getNbt();
            String selectedRuneName = Text.translatable("item.arborealis.stencil_bag.none").getString();
            int numberOfRunes = 0;

            if (nbt != null) {
                StencilBagInventory bagInventory = new StencilBagInventory(stack);

                for (int i = 0; i < StencilBag.BAG_SLOTS; i++) {
                    if (!bagInventory.getStack(i).isEmpty()) {
                        numberOfRunes++;
                    }
                }

                if (nbt.contains("selected")) {
                    // Get the item stack at the specified spot
                    ItemStack selectedStack = bagInventory.getItems().get(nbt.getInt("selected"));
                    if (!selectedStack.isEmpty()) {
                        NbtCompound stencilNbt = selectedStack.getNbt();

                        // Same code as in StencilCarved to get the rune from the pattern
                        if (stencilNbt != null && stencilNbt.contains("pattern")) {
                            int[] pattern = stencilNbt.getIntArray("pattern");
                            pattern = Arrays.stream(pattern).map(i -> i == 2 ? 1 : i).toArray();

                            if (RuneManager.isValidRune(pattern)) {
                                Rune rune = RuneManager.getRuneFromArray(pattern);

                                selectedRuneName = Text.translatable("rune.arborealis." + rune.name).getString(); // Set to the rune name (retains colour too)
                            } else {
                                selectedRuneName = Text.translatable("rune.arborealis.unknown").getString();
                            }
                        }

                        // If the stencil is renamed, append the name of the item
                        if (selectedStack.hasCustomName()) {
                            selectedRuneName = String.format("%s %s", selectedRuneName, String.format("(%s)", selectedStack.getName().getString()));
                        }
                    }
                }
            }

            tooltip.add(Text.translatable("item.arborealis.stencil_bag.tooltip1", numberOfRunes, selectedRuneName));
            tooltip.add(Text.translatable("item.arborealis.stencil_bag.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.stencil_bag.tooltip3"));
            tooltip.add(Text.translatable("item.arborealis.stencil_bag.tooltip4"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(ItemStack stack) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new StencilBagScreenHandler(syncId, inventory, new StencilBagInventory(stack)), stack.getName());
    }
}
