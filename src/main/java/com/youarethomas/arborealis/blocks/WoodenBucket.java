package com.youarethomas.arborealis.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WoodenBucket extends Block {

    public WoodenBucket(Settings settings) {
        super(settings.nonOpaque());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        double pixelSize = 1.0D / 16.0D;

        return VoxelShapes.cuboid(2D * pixelSize, 0D, 2f * pixelSize, 1D - (2D * pixelSize), 12D * pixelSize, 1D - (2D * pixelSize));
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("block.arborealis.wooden_bucket.tooltip1"));
            tooltip.add(new TranslatableText("block.arborealis.wooden_bucket.tooltip2"));
            tooltip.add(new TranslatableText("block.arborealis.wooden_bucket.tooltip3"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}
