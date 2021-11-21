package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class RegrowthSpoon extends ToolItem {

    public RegrowthSpoon(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Get all the things
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        ItemStack itemStack = context.getStack();

        if (blockState.isOf(Arborealis.CARVED_WOOD) || blockState.isOf(Arborealis.CARVED_NETHER_WOOD)) {
            CarvedWoodEntity be = (CarvedWoodEntity) world.getBlockEntity(blockPos);

            if (be != null) {
                be.setFaceArray(context.getSide(), new int[49]); // reset side of face
                be.setFaceCatalysed(context.getSide(), false);
                be.setFaceGlow(context.getSide(), false);

                // Check to see if any sides are carved
                boolean blockReset = true;
                for (Direction dir : Direction.values()) {
                    if (!Arrays.deepEquals(ArrayUtils.toObject(be.getFaceArray(dir)), ArrayUtils.toObject(new int[49]))) {
                        blockReset = false;
                    }
                }

                TreeManager.checkLifeForce(world, blockPos);

                // If no sides are carved, reset to respective log block. Otherwise, update runes
                if (blockReset) {
                    if (!world.isClient) {
                        world.setBlockState(blockPos, Registry.BLOCK.get(new Identifier(be.getLogID())).getDefaultState());
                        itemStack.damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand())); // Damage carving knife when carving is applied
                    }
                } else {
                    be.checkForRunes();
                }

                if (world.isClient) {
                    world.playSound(playerEntity, blockPos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 0.5F);
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.arborealis.regrowth_spoon.tooltip1"));
            tooltip.add(new TranslatableText("item.arborealis.regrowth_spoon.tooltip2"));
            tooltip.add(new TranslatableText("item.arborealis.regrowth_spoon.tooltip3"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}
