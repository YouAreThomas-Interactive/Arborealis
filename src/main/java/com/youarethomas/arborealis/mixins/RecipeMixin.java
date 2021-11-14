package com.youarethomas.arborealis.mixins;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Random;

@Mixin(Recipe.class)
public interface RecipeMixin {

    @ModifyArg(method = "getRemainder", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;"), index = 1)
    private Object remainderDamage(Object object) {
        if (object instanceof ItemStack itemStack) {
            if (itemStack.isOf(Arborealis.CARVING_KNIFE)) {
                itemStack.damage(1, new Random(), (ServerPlayerEntity)itemStack.getHolder());
                return itemStack;
            }
        }
        return object;
    }
}
