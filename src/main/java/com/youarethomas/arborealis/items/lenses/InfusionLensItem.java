package com.youarethomas.arborealis.items.lenses;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.BeamEmittingBlockEntity;
import com.youarethomas.arborealis.block_entities.HollowedLogEntity;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfusionLensItem extends Item implements ProjectionModifierItem {

    public InfusionLensItem(Settings settings) {
        super(settings);
    }

    @Override
    public ArborealisUtil.Colour getLensColor() {
        return new ArborealisUtil.Colour(0xDEB5FF);
    }

    @Override
    public void onActivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam) {
        BlockState stateAtPos = world.getBlockState(hitBlockPos);
        if (!stateAtPos.isOf(Arborealis.HOLLOWED_LOG)) return;

        HollowedLogEntity hollowedLogEntity = (HollowedLogEntity) world.getBlockEntity(hitBlockPos);
        if (hollowedLogEntity != null) {
            hollowedLogEntity.setHasInfusionBeam(true);
        }
    }

    @Override
    public void onDeactivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam) {
        BlockState stateAtPos = world.getBlockState(hitBlockPos);
        if (!stateAtPos.isOf(Arborealis.HOLLOWED_LOG)) return;

        HollowedLogEntity entity = (HollowedLogEntity) world.getBlockEntity(hitBlockPos);
        if (entity != null)
            entity.setHasInfusionBeam(false);
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.arborealis.infusion_lens.tooltip1"));
            tooltip.add(Text.translatable("item.arborealis.infusion_lens.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.infusion_lens.tooltip3"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }
}
