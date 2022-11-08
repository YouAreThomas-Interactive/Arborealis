package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.items.StencilBag;
import com.youarethomas.arborealis.misc.StencilBagInventory;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.ArborealisConstants;
import com.youarethomas.arborealis.runes.RuneManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"), cancellable = true)
    private void abrOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && player.isSneaking()) {
            ItemStack itemInHand = player.getStackInHand(Hand.MAIN_HAND);
            StencilBagInventory bagInventory = new StencilBagInventory(itemInHand);

            // If player is holding shift and holding a stencil bag, cancel the normal hotbar scroll and instead...
            if (itemInHand.isOf(Arborealis.STENCIL_BAG)) {
                NbtCompound nbt = itemInHand.getNbt();
                int selectedStencilSlot = 0;

                if (nbt != null) {
                    if (nbt.contains("selected")) {
                        // Get the currently selected slot
                        selectedStencilSlot = nbt.getInt("selected");

                        // Move to the next slot with an item in it, wrapping around to the start if none found
                        if (vertical > 0) {
                            for (int i = 1; i <= StencilBag.BAG_SLOTS; i++) {
                                int newSlot = (selectedStencilSlot + i) % StencilBag.BAG_SLOTS;
                                if (bagInventory.getItems().get(newSlot).isOf(Arborealis.CARVED_STENCIL)) {
                                    selectedStencilSlot = newSlot;
                                    nbt.putInt("selected", selectedStencilSlot);
                                    break;
                                }
                            }
                        } else {
                            for (int i = StencilBag.BAG_SLOTS - 1; i >= 0; i--) {
                                int newSlot = (selectedStencilSlot + i) % StencilBag.BAG_SLOTS;
                                if (bagInventory.getItems().get(newSlot).isOf(Arborealis.CARVED_STENCIL)) {
                                    selectedStencilSlot = newSlot;
                                    nbt.putInt("selected", selectedStencilSlot);
                                    break;
                                }
                            }
                        }

                    } else {
                        nbt.putInt("selected", selectedStencilSlot);
                    }

                    // Save the new slot
                    //itemInHand.setNbt(nbt);

                    // Update the item nbt
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeNbt(nbt);
                    ClientPlayNetworking.send(ArborealisConstants.SCROLL_BAG_UPDATE, buf);
                }

                // Get the item stack at the specified spot
                ItemStack selectedStack = bagInventory.getItems().get(selectedStencilSlot);
                String selectedMsg = Text.translatable("item.arborealis.stencil_bag.none").getString(); // If there are no runes in the bag

                if (!selectedStack.isEmpty()) {
                    if (selectedStack.isOf(Arborealis.CARVED_STENCIL)) {
                        NbtCompound stencilNbt = selectedStack.getNbt();

                        selectedMsg = Text.translatable("rune.arborealis.unknown").getString(); // If the rune isn't a known rune

                        // Same code as in StencilCarved to get the rune from the pattern
                        if (stencilNbt != null && stencilNbt.contains("pattern")) {
                            int[] pattern = stencilNbt.getIntArray("pattern");
                            pattern = Arrays.stream(pattern).map(i -> i == 2 ? 1 : i).toArray();

                            if (RuneManager.isValidRune(pattern)) {
                                Rune rune = RuneManager.getRuneFromArray(pattern);

                                selectedMsg = Text.translatable("rune.arborealis." + rune.name).getString(); // Set to the rune name (retains colour too)
                            }
                        }

                        // If the stencil is renamed, append the name of the item
                        if (selectedStack.hasCustomName()) {
                            selectedMsg = String.format("%s %s", selectedMsg, String.format("(%s)", selectedStack.getName().getString()));
                        }
                    }
                }

                // Send a message to the player on their hud that displays which rune is now selected
                player.sendMessage(Text.translatable("item.arborealis.stencil_bag.selected", selectedMsg), true);
                info.cancel();
            }
        }
    }
}
