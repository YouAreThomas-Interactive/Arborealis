package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Random;

@Mixin(Recipe.class)
public interface RecipeMixin {

    /*@ModifyArg(method = "getRemainder", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"), index = 1)
    private Object remainderDamage(Object object) {
        if (object instanceof ItemStack itemStack) {
            if (itemStack.isOf(Arborealis.CARVING_KNIFE)) {
                itemStack.damage(1, new Random(), (ServerPlayerEntity)itemStack.getHolder());
                return itemStack;
            }
        }
        return object;
    }*/

    // TODO: There may be a better way to do this... see above

    @Overwrite
    default DefaultedList<ItemStack> getRemainder(Inventory inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for(int i = 0; i < defaultedList.size(); ++i) {
            Item item = inventory.getStack(i).getItem();
            if (item.hasRecipeRemainder()) {
                if (inventory.getStack(i).isOf(Arborealis.CARVING_KNIFE)) {
                    ItemStack knifeStack = inventory.getStack(i);
                    knifeStack.damage(1, new Random(), (ServerPlayerEntity)knifeStack.getHolder());
                    defaultedList.set(i, knifeStack.copy());
                } else {
                    defaultedList.set(i, new ItemStack(item.getRecipeRemainder()));
                }
            }
        }

        return defaultedList;
    }
}
