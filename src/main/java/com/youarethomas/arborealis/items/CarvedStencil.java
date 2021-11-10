package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.block.BlockState;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CarvedStencil extends Item {

    public CarvedStencil(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();

        if (!world.isClient) {
            // Get pattern
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("pattern")) {
                int[] pattern = nbt.getIntArray("pattern");

                // Apply the pattern
                if (world.getBlockEntity(pos) instanceof CarvedWoodEntity be) {
                    be.setFaceArray(context.getSide(), pattern);

                    return ActionResult.SUCCESS;
                } else if (world.getBlockState(pos).isIn(BlockTags.LOGS)) {
                    if (world.getBlockState(pos).isIn(BlockTags.LOGS_THAT_BURN)) {
                        world.setBlockState(pos, Arborealis.CARVED_WOOD.getDefaultState());
                    } else {
                        world.setBlockState(pos, Arborealis.CARVED_NETHER_WOOD.getDefaultState());
                    }

                    CarvedWoodEntity be = (CarvedWoodEntity) world.getBlockEntity(pos);
                    be.setFaceArray(context.getSide(), pattern);

                    return ActionResult.SUCCESS;
                }


            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("rune.arborealis.unknown"));
    }
}
