package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.CarvedWoodEntity;
import com.youarethomas.arborealis.util.RuneManager;
import com.youarethomas.arborealis.util.TreeManager;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

public class CarvedWood extends BlockWithEntity implements BlockEntityProvider {

    public static BooleanProperty LIT = BooleanProperty.of("lit");

    public CarvedWood(Settings settings) {
        super(settings.luminance(createLightLevelFromLitBlockState(15)).strength(2.0F));
        setDefaultState(getStateManager().getDefaultState().with(LIT, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CarvedWoodEntity(pos, state);
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

        CarvedWoodEntity be = (CarvedWoodEntity) world.getBlockEntity(pos);

        Direction hitSide = hit.getSide();
        int[] faceArray = be.getFaceArray(hitSide);

        if (RuneManager.isValidRune(faceArray) && TreeManager.getTreeStructureFromBlock(pos, world).isNatural()) {
            if (player.isHolding(RuneManager.getRuneCatalyst(faceArray))) {
                if (!world.isClient) {
                    be.setFaceActive(hitSide, true);
                    be.checkForRunes();
                }
                return ActionResult.SUCCESS;
            }
        }
        if (player.isHolding(Items.GLOW_INK_SAC)) {
            be.setFaceGlow(hitSide, true);
        }

        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!player.isCreative()) {
            CarvedWoodEntity entity = (CarvedWoodEntity)world.getBlockEntity(pos);
            dropStack(world, pos, Registry.ITEM.get(new Identifier(entity.getLogID())).getDefaultStack());
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Arborealis.CARVED_WOOD_ENTITY, world.isClient ? CarvedWoodEntity::clientTick : CarvedWoodEntity::serverTick);
    }
}
