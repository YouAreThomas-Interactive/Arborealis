package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.blocks.CarvedWood;
import com.youarethomas.arborealis.blocks.HollowedLog;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class WoodDrill extends ToolItem {

    public WoodDrill(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        PlayerEntity playerEntity = context.getPlayer();

        // If the block is a log block
        if (blockState.isIn(BlockTags.LOGS)) {
            if (!world.isClient()) {
                String idString = String.valueOf(Registry.BLOCK.getId(blockState.getBlock()));
                HollowedLog.LogIDs logID = HollowedLog.LogIDs.OAK;

                if (idString.contains("spruce_log")) {
                    logID = HollowedLog.LogIDs.SPRUCE;
                } else if (idString.contains("birch_log")) {
                    logID = HollowedLog.LogIDs.BIRCH;
                } else if (idString.contains("jungle_log")) {
                    logID = HollowedLog.LogIDs.JUNGLE;
                } else if (idString.contains("dark_oak_log")) {
                    logID = HollowedLog.LogIDs.DARK_OAK;
                } else if (idString.contains("acacia_log")) {
                    logID = HollowedLog.LogIDs.ACACIA;
                } else if (idString.contains("crimson_stem")) {
                    logID = HollowedLog.LogIDs.CRIMSON;
                } else if (idString.contains("warped_stem")) {
                    logID = HollowedLog.LogIDs.WARPED;
                }

                world.setBlockState(blockPos, Arborealis.HOLLOWED_LOG.getDefaultState().with(HollowedLog.LOG_ID, logID.ordinal()));
            } else {
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        // If the block is a carved wood block
        } else if (blockState.isOf(Arborealis.CARVED_WOOD)) {
            CarvedWoodEntity carvedEntity = (CarvedWoodEntity) world.getBlockEntity(blockPos);

            if (carvedEntity != null) {
                if (!Objects.equals(carvedEntity.getLogID(), "pumpkin")) {
                    if (!world.isClient()) {
                        String idString = String.valueOf(Registry.BLOCK.getId(blockState.getBlock()));
                        HollowedLog.LogIDs logID = HollowedLog.LogIDs.OAK;

                        if (idString.contains("spruce_log")) {
                            logID = HollowedLog.LogIDs.SPRUCE;
                        } else if (idString.contains("birch_log")) {
                            logID = HollowedLog.LogIDs.BIRCH;
                        } else if (idString.contains("jungle_log")) {
                            logID = HollowedLog.LogIDs.JUNGLE;
                        } else if (idString.contains("dark_oak_log")) {
                            logID = HollowedLog.LogIDs.DARK_OAK;
                        } else if (idString.contains("acacia_log")) {
                            logID = HollowedLog.LogIDs.ACACIA;
                        } else if (idString.contains("crimson_stem")) {
                            logID = HollowedLog.LogIDs.CRIMSON;
                        } else if (idString.contains("warped_stem")) {
                            logID = HollowedLog.LogIDs.WARPED;
                        }

                        world.setBlockState(blockPos, Arborealis.HOLLOWED_LOG.getDefaultState().with(HollowedLog.LOG_ID, logID.ordinal()));
                    } else {
                        world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        } else {
            return ActionResult.PASS;
        }

        return ActionResult.SUCCESS;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.arborealis.wood_drill.tooltip1"));
            tooltip.add(new TranslatableText("item.arborealis.wood_drill.tooltip2"));
            tooltip.add(new TranslatableText("item.arborealis.wood_drill.tooltip3"));
            tooltip.add(new TranslatableText("item.arborealis.wood_drill.tooltip4"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}