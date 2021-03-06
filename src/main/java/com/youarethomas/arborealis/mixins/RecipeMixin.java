package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Recipe.class)
public interface RecipeMixin {

    @Inject(method = "getRemainder", at = @At("RETURN"))
    private void remainderDamage(Inventory inventory, CallbackInfoReturnable<DefaultedList<ItemStack>> cir) {
        DefaultedList<ItemStack> defaultedList = cir.getReturnValue();

        for(int i = 0; i < defaultedList.size(); ++i) {
            if (inventory.getStack(i).isOf(Arborealis.CARVING_KNIFE)) {
                ItemStack knifeStack = inventory.getStack(i);
                knifeStack.damage(1, Arborealis.RANDOM, (ServerPlayerEntity)knifeStack.getHolder());

                if (knifeStack.getDamage() < knifeStack.getMaxDamage()) {
                    defaultedList.set(i, knifeStack.copy());
                }
            }
        }
    }
}
