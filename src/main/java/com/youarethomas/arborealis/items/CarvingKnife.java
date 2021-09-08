package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CarvingKnife extends ToolItem {

    public CarvingKnife(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Fields
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);

        if (!world.isClient()) {
            if (blockState == Arborealis.CARVED_WOOD.getDefaultState()) {
                if (playerEntity.isSneaking()) {
                    playerEntity.sendMessage(new LiteralText("Rune carved on side " + context.getSide().toString()), false);

                    switch (context.getSide()) {
                        case NORTH -> ((CarvedWoodEntity) Objects.requireNonNull(world.getBlockEntity(blockPos))).performCarve(Direction.NORTH);
                        case EAST -> ((CarvedWoodEntity) Objects.requireNonNull(world.getBlockEntity(blockPos))).performCarve(Direction.EAST);
                        case SOUTH -> ((CarvedWoodEntity) Objects.requireNonNull(world.getBlockEntity(blockPos))).performCarve(Direction.SOUTH);
                        case WEST -> ((CarvedWoodEntity) Objects.requireNonNull(world.getBlockEntity(blockPos))).performCarve(Direction.WEST);
                    }
                }
            }

        }

        // Creating the carved block and entity
        BlockState carvedState = null;

        // If the block clicked on is wood, create a new carved wood block
        if (blockState.isIn(BlockTags.LOGS)) {
            carvedState = Arborealis.CARVED_WOOD.getDefaultState();
        }

        // Get held item and the new block
        ItemStack itemStack = context.getStack();

        if (carvedState != null) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.test((ServerPlayerEntity)playerEntity, blockPos, itemStack);
            }

            world.setBlockState(blockPos, carvedState, 11);

            CarvedWoodEntity carvedEntity = (CarvedWoodEntity) world.getBlockEntity(blockPos);
            carvedEntity.setLogID(String.valueOf(Registry.BLOCK.getId(blockState.getBlock())));
            carvedEntity.faceNorth = new int[] {
                    0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 0, 0, 0,
                    0, 0, 0, 1, 0, 0, 0,
                    0, 2, 0, 1, 1, 1, 0,
                    0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 2, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0,
            };

            if (playerEntity != null) {
                itemStack.damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
            }

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.arborealis.carving_knife.tooltip").formatted(Formatting.GRAY));
        } else {
            //tooltip.add(new LiteralText("Hold 'Shift'...").formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip").formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
        }
    }
}
