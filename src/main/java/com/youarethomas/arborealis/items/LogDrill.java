package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.blocks.HollowedLog;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class LogDrill extends ToolItem {

    public LogDrill(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();

        // If the block is a log block
        if (blockState.isIn(BlockTags.LOGS) || blockState.isOf(Arborealis.CARVED_LOG) || blockState.isOf(Arborealis.CARVED_NETHER_LOG)) {
            if (world.isClient) {
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            if (blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
                world.setBlockState(blockPos, Arborealis.HOLLOWED_LOG.getDefaultState().with(Properties.HORIZONTAL_FACING, context.getPlayerFacing().getOpposite()));
            } else {
                world.setBlockState(blockPos, Arborealis.HOLLOWED_NETHER_LOG.getDefaultState().with(Properties.HORIZONTAL_FACING, context.getPlayerFacing().getOpposite()));
            }

            HollowedLogEntity be = (HollowedLogEntity)world.getBlockEntity(blockPos);
            be.setLogState(blockState);

            itemStack.damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));


            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.arborealis.log_drill.tooltip1"));
            tooltip.add(new TranslatableText("item.arborealis.log_drill.tooltip2"));
            tooltip.add(new TranslatableText("item.arborealis.log_drill.tooltip3"));
            tooltip.add(new TranslatableText("item.arborealis.log_drill.tooltip4"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}
