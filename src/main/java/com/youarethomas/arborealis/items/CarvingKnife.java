package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CarvingKnife extends ToolItem {

    public CarvingKnife(ToolMaterial material, Settings settings) {
        super(material, settings.maxCount(1));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Get all the things
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);
        ItemStack knifeStack = playerEntity.getMainHandStack();

        if (knifeStack.isOf(Arborealis.CARVING_KNIFE)) {
            // If the block clicked on is wood, create a new carved wood block
            if (blockState.isIn(BlockTags.LOGS) || blockState.isOf(Blocks.PUMPKIN)) {
                // Swap the block out with a carved wood block...
                if (blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
                    world.setBlockState(blockPos, Arborealis.CARVED_LOG.getDefaultState());
                } else {
                    world.setBlockState(blockPos, Arborealis.CARVED_NETHER_LOG.getDefaultState());
                }

                CarvedLogEntity be = (CarvedLogEntity) world.getBlockEntity(blockPos);

                // ... and assign relevant NBT data
                if (be != null)
                    be.setLogState(blockState);

                return drawCarvePlan(be, context.getSide(), world, playerEntity); // Draw rune after creating the block
            }

            // If shift-right click on a carved wood block, turn all carving plans into actual carvings
            if (blockState.isOf(Arborealis.CARVED_LOG) || blockState.isOf(Arborealis.CARVED_NETHER_LOG)) {
                if (playerEntity.isSneaking()) {
                    carve(world, (CarvedLogEntity) world.getBlockEntity(blockPos), context.getSide(), playerEntity, knifeStack, blockPos, context.getHand());
                    return ActionResult.SUCCESS;
                } else {
                    return drawCarvePlan((CarvedLogEntity) world.getBlockEntity(blockPos), context.getSide(), world, playerEntity);
                }
            }
        }

        return ActionResult.PASS;
    }

    //carve(world, be, context.getSide(), playerEntity, knifeStack, blockPos, context.getHand());

    public static void carve(World world, CarvedLogEntity be, Direction side, PlayerEntity player, ItemStack knifeStack, BlockPos pos, Hand hand) {
        if (!world.isClient()) {
            be.performCarve();
            be.setFaceCatalysed(side, false);
            knifeStack.damage(1, player, (p) -> p.sendToolBreakStatus(hand)); // Damage carving knife when carving is applied
        } else {
            world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    public static ActionResult drawCarvePlan(CarvedLogEntity carvedWoodEntity, Direction side, World world, PlayerEntity player) {
        // Get the raytrace hit
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult pixelHit = client.crosshairTarget;
        double pixelSize = 1.0D / 16.0D;

        if (!world.isClient && pixelHit != null) {
            // Grab the decimal part of the block hit
            double x = pixelHit.getPos().x % 1;
            double y = pixelHit.getPos().y % 1;
            double z = pixelHit.getPos().z % 1;

            // Turn negative numbers into non-negatives and uniform
            if (x < 0) x = 1 - Math.abs(x);
            if (y < 0) y = 1 - Math.abs(y);
            if (z < 0) z = 1 - Math.abs(z);

            // Convert into 7x7 segments
            int segmentX = (int)Math.ceil((x - pixelSize) * 8);
            int segmentY = (int)Math.ceil((y - pixelSize) * 8);
            int segmentZ = (int)Math.ceil((z - pixelSize) * 8);

            // Stop clicking on the left or right of the valid area from registering
            if ((segmentX == 0 || segmentX == 8) && segmentZ == 0) return ActionResult.FAIL;
            if ((segmentZ == 0 || segmentZ == 8) && segmentX == 0) return ActionResult.FAIL;
            if ((segmentZ == 0 || segmentZ == 8) && segmentY == 0) return ActionResult.FAIL;

            int segmentID = -1;

            // Convert array position
            switch (side) {
                case NORTH -> segmentID = ((7 - segmentY) * 7) + (7 - segmentX);
                case SOUTH -> segmentID = ((7 - segmentY) * 7) + (segmentX - 1);
                case EAST  -> segmentID = ((7 - segmentY) * 7) + (7 - segmentZ);
                case WEST  -> segmentID = ((7 - segmentY) * 7) + (segmentZ - 1);
                case UP, DOWN -> segmentID = ((7 - segmentX) * 7) + (segmentZ - 1);
            }

            // Stop click on the top or bottom of the valid area from registering
            if (segmentID > 48 || segmentID < 0) return ActionResult.FAIL;

            // Then set array position value to highlighted if normal, normal if highlighted, or do nothing if carved out
            int[] faceArray = carvedWoodEntity.getFaceArray(side);
            if (faceArray[segmentID] == 0) {
                faceArray[segmentID] = 2;
            } else if (faceArray[segmentID] == 2) {
                faceArray[segmentID] = 0;
            } else {
                return ActionResult.FAIL;
            }

            carvedWoodEntity.setFaceArray(side, faceArray);

            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    // Append tooltip when pressing shift key
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.arborealis.carving_knife.tooltip1"));
            tooltip.add(new TranslatableText("item.arborealis.carving_knife.tooltip2"));
            tooltip.add(new TranslatableText("item.arborealis.carving_knife.tooltip3"));
        } else {
            tooltip.add(new TranslatableText("item.arborealis.hidden_tooltip"));
        }
    }
}
