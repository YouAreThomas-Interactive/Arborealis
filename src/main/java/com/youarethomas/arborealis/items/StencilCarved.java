package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.runes.AbstractRune;
import com.youarethomas.arborealis.util.RuneManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class StencilCarved extends Item {

    public StencilCarved(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();
        BlockState blockState = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();

        // Get pattern
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("pattern")) {
            int[] pattern = nbt.getIntArray("pattern");

            // Apply the pattern
            if (world.getBlockEntity(pos) instanceof CarvedWoodEntity be) {
                be.markRune(context.getSide(), pattern.clone());
                be.checkForRunes();

                return ActionResult.SUCCESS;
            } else if (world.getBlockState(pos).isIn(BlockTags.LOGS)) {
                if (blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
                    world.setBlockState(pos, Arborealis.CARVED_WOOD.getDefaultState());
                } else {
                    world.setBlockState(pos, Arborealis.CARVED_NETHER_WOOD.getDefaultState());
                }

                CarvedWoodEntity be = (CarvedWoodEntity) world.getBlockEntity(pos);

                // ... and assign relevant NBT data
                if (be != null) {
                    if (blockState.isIn(BlockTags.LOGS)) {
                        be.setLogID(String.valueOf(Registry.BLOCK.getId(blockState.getBlock())));
                    } else if (blockState.isOf(Blocks.PUMPKIN)) {
                        be.setLogID("pumpkin");
                    }
                }

                be.markRune(context.getSide(), pattern.clone());
                be.checkForRunes();

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();

        if (nbt != null && nbt.contains("pattern")) {
            int[] pattern = nbt.getIntArray("pattern");
            pattern = Arrays.stream(pattern).map(i -> i == 2 ? 1 : i).toArray();

            if (RuneManager.isValidRune(pattern)) {
                AbstractRune rune = RuneManager.getRuneFromArray(pattern);

                tooltip.add(new TranslatableText("rune.arborealis." + rune.name));
            } else {
                tooltip.add(new TranslatableText("rune.arborealis.unknown"));
            }
        } else {
            tooltip.add(new TranslatableText("rune.arborealis.unknown"));
        }
    }
}