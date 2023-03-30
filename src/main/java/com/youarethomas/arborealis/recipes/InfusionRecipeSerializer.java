package com.youarethomas.arborealis.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class InfusionRecipeSerializer implements RecipeSerializer<InfusionRecipe> {
    private InfusionRecipeSerializer() {}

    public static final InfusionRecipeSerializer INSTANCE = new InfusionRecipeSerializer();

    public static final Identifier ID = new Identifier("arborealis:infusion_recipe");

    @Override
    public InfusionRecipe read(Identifier id, JsonObject json) {
        // TODO: Data validation
        int xp = JsonHelper.getInt(json, "xp_required");
        DefaultedList<Ingredient> ingredients = getIngredients(JsonHelper.getArray(json, "ingredients"));

        String outputID = JsonHelper.getString(json, "result");
        Item outputItem = Registries.ITEM.getOrEmpty(new Identifier(outputID)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + outputID + "'"));
        if (outputItem == Items.AIR)
            throw new JsonSyntaxException("Invalid item: " + outputID);
        ItemStack output = new ItemStack(outputItem, 1);

        return new InfusionRecipe(id, xp, ingredients, output);
    }

    private static DefaultedList<Ingredient> getIngredients(JsonArray json) {
        DefaultedList<Ingredient> defaultedList = DefaultedList.of();
        for (int i = 0; i < json.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(json.get(i));
            if (ingredient.isEmpty()) continue;
            defaultedList.add(ingredient);
        }
        return defaultedList;
    }

    @Override
    public void write(PacketByteBuf buf, InfusionRecipe recipe) {
        buf.writeInt(recipe.getXpRequired());
        buf.writeInt(recipe.getIngredients().size());
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.write(buf);
        }
        buf.writeItemStack(recipe.getOutput(DynamicRegistryManager.of(Registries.REGISTRIES)));
    }

    @Override
    public InfusionRecipe read(Identifier id, PacketByteBuf buf) {
        int xp = buf.readInt();
        int inputSize = buf.readInt();
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(inputSize, Ingredient.EMPTY);
        for (int item = 0; item < ingredients.size(); ++item) {
            ingredients.set(item, Ingredient.fromPacket(buf));
        }
        ItemStack output = buf.readItemStack();
        return new InfusionRecipe(id, xp, ingredients, output);
    }
}
