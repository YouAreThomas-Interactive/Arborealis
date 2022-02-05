package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.RuneManager;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class StencilBag extends Item {

    public StencilBag(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            // TODO: Open bag
        }

        return TypedActionResult.pass(itemStack);
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
}
