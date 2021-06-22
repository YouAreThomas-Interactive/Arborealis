package com.youarethomas.arborealis.items;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
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
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class CarvingKnife extends ToolItem {

    public CarvingKnife(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);

        Optional<BlockState> carvedState = Optional.empty();

        if (blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
            carvedState = getCarvedState(blockState); // TODO: Learn what an optional is
            playerEntity.sendMessage(new LiteralText(String.valueOf(Registry.BLOCK.getId(blockState.getBlock()))), false);
        } else if (String.valueOf(Registry.BLOCK.getId(blockState.getBlock())).equals("arborealis:carved_wood")) {
            playerEntity.sendMessage(new LiteralText(String.valueOf(Registry.BLOCK.getId(blockState.getBlock()))), false);
        }

        ItemStack itemStack = context.getStack();
        Optional<BlockState> newBlock = Optional.empty();

        if (carvedState.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            newBlock = carvedState;
        }

        if (newBlock.isPresent()) {
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.test((ServerPlayerEntity)playerEntity, blockPos, itemStack);
            }

            world.setBlockState(blockPos, newBlock.get(), 11);
            if (playerEntity != null) {
                itemStack.damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
            }

            return ActionResult.success(world.isClient);
        } else {
            return ActionResult.PASS;
        }
    }

    private Optional<BlockState> getCarvedState(BlockState state) {
//        return Optional.ofNullable(STRIPPED_BLOCKS.get(state.getBlock())).map((block) -> { // TODO: Learn what the -> symbol does
//            return block.getDefaultState();
//        });

        return Optional.ofNullable(Arborealis.CARVED_WOOD.getDefaultState());
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
