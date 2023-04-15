package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.BeamEmittingBlockEntity;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.items.lenses.ProjectionModifierItem;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.runes.RuneManager;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class StencilCarved extends Item implements ProjectionModifierItem {

    public StencilCarved(Settings settings) {
        super(settings);
    }

    @Override
    public ArborealisUtil.Colour getLensColor() {
        return null;
    }

    @Override
    public void onActivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam) {
        BlockState stateAtEndPos = world.getBlockState(hitBlockPos);
        int[] pattern = projectionBeam.getBeamItemStack().getNbt().getIntArray("pattern");

        if ((stateAtEndPos.isIn(BlockTags.LOGS) || stateAtEndPos.isOf(Blocks.PUMPKIN)) && pattern != null) {
            // Swap the block out with a carved wood block...
            if (stateAtEndPos.isIn(BlockTags.LOGS_THAT_BURN)) {
                world.setBlockState(hitBlockPos, Arborealis.CARVED_LOG.getDefaultState());
            } else {
                world.setBlockState(hitBlockPos, Arborealis.CARVED_NETHER_LOG.getDefaultState());
            }

            CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(hitBlockPos);

            // ... and assign relevant NBT data
            if (carvedLog != null) {
                carvedLog.setLogState(stateAtEndPos);

                carvedLog.setFaceCatalysed(projectionBeam.getDirection().getOpposite(), true);
                carvedLog.showProjectedRune(projectionBeam.getDirection().getOpposite(), pattern);
            }
        } else if (stateAtEndPos.isIn(Arborealis.CARVED_LOGS) && pattern != null) {
            CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(hitBlockPos);

            if (carvedLog != null) {
                carvedLog.setFaceCatalysed(projectionBeam.getDirection().getOpposite(), true);
                carvedLog.showProjectedRune(projectionBeam.getDirection().getOpposite(), pattern);
            }
        }
    }

    @Override
    public void onDeactivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam) {
        BlockState stateAtEndPos = world.getBlockState(hitBlockPos);

        if (stateAtEndPos.isIn(Arborealis.CARVED_LOGS)) {
            CarvedLogEntity carvedLog = (CarvedLogEntity) world.getBlockEntity(hitBlockPos);
            Direction dir = projectionBeam.getDirection();

            if (carvedLog != null) {
                carvedLog.setFaceCatalysed(dir.getOpposite(), false);
                carvedLog.showProjectedRune(dir.getOpposite(), new int[25]);

                boolean blockReset = true;
                for (Direction faceDir : Direction.values()) {
                    if (!Arrays.deepEquals(ArrayUtils.toObject(carvedLog.getFaceArray(faceDir)), ArrayUtils.toObject(new int[25]))) {
                        blockReset = false;
                    }
                }

                // If no sides are carved, reset to respective log block.
                if (blockReset) {
                    if (!world.isClient) {
                        world.setBlockState(hitBlockPos, carvedLog.getLogState());
                    }
                }
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState blockState = world.getBlockState(pos);

        return useStencil(context.getStack(), world, blockState, pos, context.getSide());
    }

    public ActionResult useStencil(ItemStack stack, World world, BlockState blockState, BlockPos pos, Direction side) {
        // Get pattern
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("pattern")) {
            int[] pattern = nbt.getIntArray("pattern");

            // Apply the pattern
            if (world.getBlockEntity(pos) instanceof CarvedLogEntity be) {
                be.markRune(side, pattern.clone());
                be.checkForRunes();

                return ActionResult.SUCCESS;
            } else if (world.getBlockState(pos).isIn(BlockTags.LOGS) || world.getBlockState(pos).isOf(Blocks.PUMPKIN)) {
                if (blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
                    world.setBlockState(pos, Arborealis.CARVED_LOG.getDefaultState());
                } else {
                    world.setBlockState(pos, Arborealis.CARVED_NETHER_LOG.getDefaultState());
                }

                CarvedLogEntity be = (CarvedLogEntity) world.getBlockEntity(pos);

                // ... and assign relevant NBT data
                if (be != null)
                    be.setLogState(blockState);

                be.markRune(side, pattern.clone());
                be.checkForRunes();

                return ActionResult.SUCCESS;
            }
        }

        return  ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();

        if (nbt != null && nbt.contains("pattern")) {
            int[] pattern = nbt.getIntArray("pattern");
            pattern = Arrays.stream(pattern).map(i -> i == 2 ? 1 : i).toArray();

            if (RuneManager.isValidRune(pattern)) {
                Rune rune = RuneManager.getRuneFromArray(pattern);

                tooltip.add(Text.translatable("rune.arborealis." + rune.name));
            } else {
                tooltip.add(Text.translatable("rune.arborealis.unknown"));
            }
        } else {
            tooltip.add(Text.translatable("rune.arborealis.unknown"));
        }
    }
}
