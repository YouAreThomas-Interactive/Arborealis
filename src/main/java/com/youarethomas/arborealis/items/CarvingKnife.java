package com.youarethomas.arborealis.items;

import com.google.common.collect.ImmutableMap;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CarvingKnife extends ToolItem {

    protected static final Map<Block, Block> STRIPPED_BLOCKS;

    public CarvingKnife(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        BlockState blockState = world.getBlockState(blockPos);

        Optional<BlockState> strippedState = this.getStrippedState(blockState); // TODO: Learn what an optional is
        ItemStack itemStack = context.getStack();
        Optional<BlockState> newBlock = Optional.empty();

        if (strippedState.isPresent()) {
            world.playSound(playerEntity, blockPos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            newBlock = strippedState;
        }

        if (newBlock.isPresent()) {
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.ITEM_USED_ON_BLOCK.test((ServerPlayerEntity)playerEntity, blockPos, itemStack);
            }

            world.setBlockState(blockPos, newBlock.get(), 11);
            if (playerEntity != null) {
                itemStack.damage(1, playerEntity, (p) -> {
                    p.sendToolBreakStatus(context.getHand());
                });
            }

            return ActionResult.success(world.isClient);
        } else {
            return ActionResult.PASS;
        }
    }

    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(STRIPPED_BLOCKS.get(state.getBlock())).map((block) -> { // TODO: Learn what the -> symbol does
            return block.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS));
        });
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

    static {
        STRIPPED_BLOCKS = (new ImmutableMap.Builder()).put(Blocks.OAK_LOG, Blocks.QUARTZ_PILLAR).put(Blocks.QUARTZ_PILLAR, Blocks.OAK_LOG).build();
    }
}
