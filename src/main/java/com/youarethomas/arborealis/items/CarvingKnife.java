package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.util.hit.HitResult;
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
        // Get all the things
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        ItemStack itemStack = context.getStack();

        // If the block clicked on is wood, create a new carved wood block
        if (blockState.isIn(BlockTags.LOGS) || blockState.isOf(Blocks.PUMPKIN)) {
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.test((ServerPlayerEntity)playerEntity, blockPos, itemStack);
            }

            world.setBlockState(blockPos, Arborealis.CARVED_WOOD.getDefaultState(), 11);

            CarvedWoodEntity carvedEntity = (CarvedWoodEntity) world.getBlockEntity(blockPos);

            if (blockState.isIn(BlockTags.LOGS)) {
                carvedEntity.setLogID(String.valueOf(Registry.BLOCK.getId(blockState.getBlock())));
            } else if (blockState.isOf(Blocks.PUMPKIN)) {
                carvedEntity.setLogID("pumpkin");
            }

            if (playerEntity != null) {
                itemStack.damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
            }

            drawCarvePlan(carvedEntity, context.getSide(), world, playerEntity); // Draw rune after creating the block

            return ActionResult.success(world.isClient);
        }

        // If shift-right click on a carved wood block, turn all carving plans into actual carvings
        if (blockState == Arborealis.CARVED_WOOD.getDefaultState()) {
            if (playerEntity.isSneaking()) {
                if (!world.isClient()) {
                    //playerEntity.sendMessage(new LiteralText("Rune carved on side " + context.getSide().toString()), false);

                    ((CarvedWoodEntity) world.getBlockEntity(blockPos)).performCarve();

                } else {
                    world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            } else {
                drawCarvePlan((CarvedWoodEntity) world.getBlockEntity(blockPos), context.getSide(), world, playerEntity);
            }

            itemStack.damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private void drawCarvePlan(CarvedWoodEntity carvedWoodEntity, Direction side, World world, PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult pixelHit = client.crosshairTarget;
        double pixelSize = 1.0D / 16.0D;

        if (!world.isClient && pixelHit != null) {
            double x = Math.abs(pixelHit.getPos().x % 1);
            double y = Math.abs(pixelHit.getPos().y % 1);
            double z = Math.abs(pixelHit.getPos().z % 1);

            // Translate into 7x7 segments
            int segmentX = (int)Math.ceil((x - pixelSize) * 8);
            int segmentY = (int)Math.ceil((y - pixelSize) * 8);
            int segmentZ = (int)Math.ceil((z - pixelSize) * 8);

            int segmentID = -1;

            switch (side) {
                case NORTH -> segmentID = ((segmentY - 1) * 7) + (7 - segmentX);
                case SOUTH -> segmentID = ((segmentY - 1) * 7) + (segmentX - 1);
                case EAST -> segmentID = ((segmentY - 1) * 7) + (segmentZ - 1);
                case WEST -> segmentID = ((segmentY - 1) * 7) + (7 - segmentZ);
            }

            int[] faceArray = carvedWoodEntity.getFaceArray(side);
            if (segmentID != -1) {
                if (faceArray[segmentID] == 0) {
                    faceArray[segmentID] = 2;
                } else if (faceArray[segmentID] == 2) {
                    faceArray[segmentID] = 0;
                }
            }

            carvedWoodEntity.setFaceArray(side, faceArray);

            player.sendMessage(new LiteralText("%s, %s, %s - %s".formatted(segmentX, segmentY, segmentZ, segmentID)), false);
        }
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
