package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.block.BlockState;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TuningForkItem extends ToolItem {

    public TuningForkItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();

        if (!world.isClient) {
            BlockState state = world.getBlockState(blockPos);

            if (state.isOf(Arborealis.PROJECTOR)) {
                world.setBlockState(blockPos, state.with(HorizontalFacingBlock.FACING, context.getSide()));
            } else
                return ActionResult.PASS;

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.arborealis.tuning_fork.tooltip1"));
            tooltip.add(Text.translatable("item.arborealis.tuning_fork.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.tuning_fork.tooltip3"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }
}
