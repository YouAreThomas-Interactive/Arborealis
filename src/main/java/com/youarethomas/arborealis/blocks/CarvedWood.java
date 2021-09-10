package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;

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
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult pixelHit = client.crosshairTarget;

        if (!world.isClient) {
            if (player.isHolding(Arborealis.CARVING_KNIFE)) {
                switch (hit.getSide()) {
                    case NORTH:
                        break;
                    case EAST:
                        break;
                    case SOUTH:
                        break;
                    case WEST:
                        break;
                }

                player.sendMessage(new LiteralText((pixelHit.getPos().toString())), false);

                return ActionResult.SUCCESS;
            }
        } else {
            player.sendMessage(new LiteralText("log_id: " + BLOCK_ENTITY.getLogID()), false);
        }

        return ActionResult.PASS;
    }

}
