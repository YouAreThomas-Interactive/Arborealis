package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CarvedWood extends Block implements BlockEntityProvider {

    private CarvedWoodEntity BLOCK_ENTITY;

    public CarvedWood(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        BLOCK_ENTITY = new CarvedWoodEntity(pos, state);

        return BLOCK_ENTITY;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            player.sendMessage(new LiteralText(BLOCK_ENTITY.getLogID()), false);
        }

        return ActionResult.SUCCESS;
    }
}
