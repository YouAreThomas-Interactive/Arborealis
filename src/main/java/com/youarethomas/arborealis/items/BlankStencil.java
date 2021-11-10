package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class BlankStencil extends Item {

    public BlankStencil(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();

        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof CarvedWoodEntity be) {
                // Create pattern
                int[] pattern = be.getFaceArray(context.getSide());
                pattern = Arrays.stream(pattern).map(i -> i == 1 ? 2 : i).toArray();

                // Create the carved stencil
                ItemStack carvedStencil = Arborealis.CARVED_STENCIL.getDefaultStack();
                NbtCompound nbt = carvedStencil.getOrCreateNbt();
                nbt.putIntArray("pattern", pattern);
                carvedStencil.setNbt(nbt);

                // Replace blank stencil with the carved one
                if (!player.isCreative()) {
                    stack.decrement(1);
                }
                player.giveItemStack(carvedStencil);

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("item.arborealis.stencil_blank.blank"));
    }

}
