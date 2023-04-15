package com.youarethomas.arborealis.items.lenses;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.BeamEmittingBlockEntity;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.runes.Rune;
import com.youarethomas.arborealis.util.ArborealisNbt;
import com.youarethomas.arborealis.util.ArborealisUtil;
import com.youarethomas.arborealis.util.TreeManager;
import com.youarethomas.arborealis.util.TreeStructure;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImplosionLensItem extends Item implements ProjectionModifierItem {

    public ImplosionLensItem(Settings settings) {
        super(settings);
    }

    @Override
    public ArborealisUtil.Colour getLensColor() {
        return new ArborealisUtil.Colour(0x255534);
    }

    @Override
    public void onActivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam) {
        BlockState stateAtPos = world.getBlockState(hitBlockPos);

        if (!stateAtPos.isOf(Arborealis.HOLLOWED_LOG)) return;

        ServerWorldMixinAccess serverWorld = (ServerWorldMixinAccess) world;
        TreeManager treeManager = serverWorld.getTreeManager();

        TreeStructure tree;
        if (treeManager.isBlockInTreeStructure(hitBlockPos))
            tree = treeManager.getTreeStructureFromPos(hitBlockPos, world);
        else
            tree = treeManager.constructTreeStructureFromBlock(hitBlockPos, (ServerWorld) world);

        if (tree != null && tree.isNatural()) {
            // Get all runes from the tree
            List<Rune> runesOnTree = new ArrayList<>();
            for (BlockPos logPos : tree.logs) {
                if (world.getBlockState(logPos).isIn(Arborealis.CARVED_LOGS)) {
                    CarvedLogEntity carvedLog = (CarvedLogEntity)world.getBlockEntity(logPos);
                    runesOnTree.addAll(carvedLog.runesPresentLastCheck.stream().filter(newRune -> runesOnTree.stream().noneMatch(rune -> newRune.name.equals(rune.name))).toList());
                }
            }

            // Build core item with all runes stored on it
            ItemStack implodedCore = Arborealis.LIFE_CORE.getDefaultStack().split(1);

            if (runesOnTree.size() > 0) {
                NbtCompound nbt = implodedCore.getOrCreateNbt();
                NbtElement runeList = ArborealisNbt.serializeRuneList(runesOnTree);
                nbt.put("rune_list", runeList);
                implodedCore.setNbt(nbt);
            }

            // Chop tree and drop core
            tree.chopTreeStructure(world, false);
            Vec3d coreSpawnPos = Vec3d.ofCenter(hitBlockPos);
            world.spawnEntity(new ItemEntity(world, coreSpawnPos.x, coreSpawnPos.y, coreSpawnPos.z, implodedCore));
        }
    }

    @Override
    public void onDeactivated(BlockPos hitBlockPos, World world, BeamEmittingBlockEntity emittingBlock, BeamEmittingBlockEntity.ProjectionBeam projectionBeam) {

    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("item.arborealis.implosion_lens.tooltip1"));
            tooltip.add(Text.translatable("item.arborealis.implosion_lens.tooltip2"));
            tooltip.add(Text.translatable("item.arborealis.implosion_lens.tooltip3"));
        } else {
            tooltip.add(Text.translatable("item.arborealis.hidden_tooltip"));
        }
    }
}
