package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedLogEntity;
import com.youarethomas.arborealis.mixin_access.ServerWorldMixinAccess;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class CarvedLog extends BlockWithEntity implements BlockEntityProvider {

    public static BooleanProperty LIT = BooleanProperty.of("lit");

    public CarvedLog(Settings settings) {
        super(settings.luminance(createLightLevelFromLitBlockState(15)).strength(2.0F));
        setDefaultState(getStateManager().getDefaultState().with(LIT, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CarvedLogEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(LIT);
    }

    private static ToIntFunction<BlockState> createLightLevelFromLitBlockState(int litLevel) {
        return (state) -> (Boolean)state.get(Properties.LIT) ? litLevel : 0;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        CarvedLogEntity be = (CarvedLogEntity) world.getBlockEntity(pos);

        Direction hitSide = hit.getSide();
        int[] faceArray = be.getFaceArray(hitSide);

        if (world instanceof ServerWorld serverWorld) {
            TreeManager treeManager = ((ServerWorldMixinAccess)serverWorld).getTreeManager();

            if (RuneManager.isValidRune(faceArray) && treeManager.getTreeStructureFromBlock(pos, world).isNatural()) {
                if (player.isHolding(RuneManager.getRuneCatalyst(faceArray)) && !be.getFaceCatalysed(hitSide)) {
                    if (world.isClient) {
                        if (!player.isCreative()) {
                            player.getStackInHand(hand).decrement(1);
                        }
                        world.playSound(player, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, 0.5F);
                    } else {
                        be.setFaceCatalysed(hitSide, true);
                        TreeManager.checkLifeForce(world, pos);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }


        if (player.isHolding(Items.GLOW_INK_SAC)) {
            be.setFaceGlow(hitSide, true);
            if (!player.isCreative()) {
                player.getStackInHand(hand).decrement(1);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        CarvedLogEntity be = (CarvedLogEntity)world.getBlockEntity(pos);

        world.setBlockState(pos, be.getLogState());
        world.breakBlock(pos, !player.isCreative());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Arborealis.CARVED_LOG_ENTITY, world.isClient ? CarvedLogEntity::clientTick : CarvedLogEntity::serverTick);
    }
}
