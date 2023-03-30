package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Recipe.class)
public interface RecipeMixin {

    @Inject(method = "getRemainder", at = @At("RETURN"))
    private void remainderDamage(Inventory inventory, CallbackInfoReturnable<DefaultedList<ItemStack>> cir) {
        DefaultedList<ItemStack> defaultedList = cir.getReturnValue();

        for(int i = 0; i < defaultedList.size(); ++i) {
            // Carving knife damage
            if (inventory.getStack(i).isOf(Arborealis.CARVING_KNIFE)) {
                ItemStack knifeStack = inventory.getStack(i);
                knifeStack.damage(1, Arborealis.RANDOM, (ServerPlayerEntity)knifeStack.getHolder());

                if (knifeStack.getDamage() < knifeStack.getMaxDamage()) {
                    defaultedList.set(i, knifeStack.copy());
                }
            }
            // Bottled saps (only if the return item isn't another bottle)
            else if (inventory.getStack(i).isIn(Arborealis.SAPS) && !((Recipe<?>)(Object)this).getOutput(DynamicRegistryManager.of(Registries.REGISTRIES)).isIn(Arborealis.SAPS)) {
                defaultedList.set(i, Items.GLASS_BOTTLE.getDefaultStack().split(1));
            }
        }
    }
}
