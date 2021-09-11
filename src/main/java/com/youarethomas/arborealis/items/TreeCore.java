package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.blocks.HollowedLog;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreeCore extends Item {

    public TreeCore(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isIn(BlockTags.LOGS)) {
            String idString = String.valueOf(Registry.BLOCK.getId(blockState.getBlock()));
            HollowedLog.LogIDs logID = HollowedLog.LogIDs.OAK;

            if (idString.contains("spruce_log")) { logID = HollowedLog.LogIDs.SPRUCE; }
            else if (idString.contains("birch_log")) { logID = HollowedLog.LogIDs.BIRCH; }
            else if (idString.contains("jungle_log")) { logID = HollowedLog.LogIDs.JUNGLE; }
            else if (idString.contains("dark_oak_log")) { logID = HollowedLog.LogIDs.DARK_OAK; }
            else if (idString.contains("acacia_log")) { logID = HollowedLog.LogIDs.ACACIA; }
            else if (idString.contains("crimson_stem")) { logID = HollowedLog.LogIDs.CRIMSON; }
            else if (idString.contains("warped_stem")) { logID = HollowedLog.LogIDs.WARPED; }

            world.setBlockState(blockPos, Arborealis.HOLLOWED_LOG.getDefaultState().with(HollowedLog.LOG_ID, logID.ordinal()));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.arborealis.tree_core.tooltip1"));
            tooltip.add(new TranslatableText("item.arborealis.tree_core.tooltip2"));
            tooltip.add(new TranslatableText("item.arborealis.tree_core.tooltip3"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}
