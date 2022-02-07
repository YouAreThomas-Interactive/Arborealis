package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.items.StencilBag;
import com.youarethomas.arborealis.items.StencilCarved;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.RuneManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        PlayerEntity serverPlayer = MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(player.getEntityName());

        if (serverPlayer != null && player.isSneaking()) {
            ItemStack itemInHand = serverPlayer.getStackInHand(Hand.MAIN_HAND);

            // If player is holding shift and holding a stencil bag, cancel the normal hotbar scroll and instead...
            if (itemInHand.isOf(Arborealis.STENCIL_BAG)) {
                DefaultedList<ItemStack> inventory = DefaultedList.ofSize(StencilBag.BAG_SLOTS, ItemStack.EMPTY);

                NbtCompound nbt = itemInHand.getNbt();
                int selectedStencilSlot = 0;

                if (nbt != null) {
                    Inventories.readNbt(nbt, inventory);

                    if (nbt.contains("selected")) {
                        // Get the currently selected slot
                        selectedStencilSlot = nbt.getInt("selected");

                        // Move to the next slot with an item in it, wrapping around to the start if none found
                        for (int i = 1; i <= StencilBag.BAG_SLOTS; i++) {
                            int newSlot = (selectedStencilSlot + i) % StencilBag.BAG_SLOTS;
                            if (inventory.get(newSlot).isOf(Arborealis.CARVED_STENCIL)) {
                                selectedStencilSlot = newSlot;
                                nbt.putInt("selected", selectedStencilSlot);
                                break;
                            }
                        }
                    } else {
                        nbt.putInt("selected", selectedStencilSlot);
                    }

                    // Save the new slot
                    itemInHand.setNbt(nbt);
                }

                // Get the item stack at the specified spot
                ItemStack selectedStack = inventory.get(selectedStencilSlot);
                String selectedMsg = new TranslatableText("item.arborealis.stencil_bag.none").getString(); // If there are no runes in the bag

                if (!selectedStack.isEmpty()) {
                    if (selectedStack.isOf(Arborealis.CARVED_STENCIL)) {
                        NbtCompound stencilNbt = selectedStack.getNbt();

                        selectedMsg = new TranslatableText("rune.arborealis.unknown").getString(); // If the rune isn't a known rune

                        // Same code as in StencilCarved to get the rune from the pattern
                        if (stencilNbt != null && stencilNbt.contains("pattern")) {
                            int[] pattern = stencilNbt.getIntArray("pattern");
                            pattern = Arrays.stream(pattern).map(i -> i == 2 ? 1 : i).toArray();

                            if (RuneManager.isValidRune(pattern)) {
                                AbstractRune rune = RuneManager.getRuneFromArray(pattern);

                                selectedMsg = new TranslatableText("rune.arborealis." + rune.name).getString(); // Set to the rune name (retains colour too)
                            }
                        }

                        // If the stencil is renamed, append the name of the item
                        if (selectedStack.hasCustomName()) {
                            selectedMsg = String.format("%s %s", selectedMsg, String.format("(%s)", selectedStack.getName().getString()));
                        }
                    }
                }

                // Send a message to the player on their hud that displays which rune is now selected
                player.sendMessage(new TranslatableText("item.arborealis.stencil_bag.selected", selectedMsg), true);
                info.cancel();
            }
        }
    }
}
