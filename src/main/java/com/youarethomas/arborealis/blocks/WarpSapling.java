package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ArborealisPersistentState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Optional;

public class WarpSapling extends Block {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public WarpSapling(Settings settings) {
        super(settings.nonOpaque().sounds(BlockSoundGroup.SLIME).ticksRandomly());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world instanceof ServerWorld serverWorld) {
            trySpawnWarpTree(serverWorld, pos);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.offset(Direction.DOWN);
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, Direction.UP);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // TODO: Could add in a minimum required time?
        if (random.nextInt(50) == 0 && !world.isClient) {
            trySpawnWarpTree(world, pos);
        }
    }

    public void trySpawnWarpTree(ServerWorld serverWorld, BlockPos pos) {
        StructureTemplateManager structureManager = serverWorld.getStructureTemplateManager();
        Optional<StructureTemplate> warpTreeStructure = structureManager.getTemplate(new Identifier(Arborealis.MOD_ID, "warp_tree"));

        // Check two cylinders for spawn conditions - first for the base is 9x5x9, second for the top is 15x10x15
        boolean canBePlaced = true;
        baseCylinder:
        for (int yAdd = 0; yAdd < 6; yAdd++) {
            Iterable<BlockPos> baseCylinder =  BlockPos.iterateOutwards(pos.add(0, yAdd, 0), 4, 0, 4);

            for (BlockPos baseTestPos : baseCylinder) {
                System.out.println("testing base pos: " + baseTestPos.toString());
                if (!serverWorld.getBlockState(baseTestPos).isOf(Blocks.AIR) && !serverWorld.getBlockState(baseTestPos).isIn(BlockTags.REPLACEABLE_PLANTS) && !serverWorld.getBlockState(baseTestPos).isOf(Arborealis.WARP_SAPLING)) {
                    canBePlaced = false;
                    break baseCylinder; // if can't be placed, break out of base cylinder
                }
            }
        }

        // If base cylinder didn't fail, try the top cylinder
        if (canBePlaced) {
            topCylinder:
            for (int yAdd = 6; yAdd < 16; yAdd++) {
                Iterable<BlockPos> topCylinder =  BlockPos.iterateOutwards(pos.add(0, yAdd, 0), 7, 0, 7);

                for (BlockPos topTestPos : topCylinder) {
                    System.out.println("testing top pos: " + topTestPos.toString());
                    if (!serverWorld.getBlockState(topTestPos).isOf(Blocks.AIR) && !serverWorld.getBlockState(topTestPos).isIn(BlockTags.REPLACEABLE_PLANTS)) {
                        canBePlaced = false;
                        break topCylinder; // if can't be placed, break out of base cylinder
                    }
                }
            }
        }

        // If the structure was loaded successfully
        if (warpTreeStructure.isPresent() && canBePlaced)
            place(serverWorld, pos, warpTreeStructure.get());

    }

    public boolean place(ServerWorld world, BlockPos pos, StructureTemplate structure) {
        // Get size and rotation
        Vec3i structureSize = structure.getSize();
        StructurePlacementData placementData = (new StructurePlacementData()).setPosition(new BlockPos(structureSize.getX() / 2, 0, structureSize.getZ() / 2)).setRotation(BlockRotation.random(Arborealis.RANDOM));

        // Place tree at centred position
        BlockPos placementPos = new BlockPos(pos.getX() - structureSize.getX() / 2, pos.getY(), pos.getZ() - structureSize.getZ() / 2);
        structure.place(world, placementPos, placementPos, placementData, Arborealis.RANDOM, 2);

        // Add core to the list
        if (world.getBlockState(pos).isOf(Arborealis.WARP_CORE)) {
            ArborealisPersistentState worldNbt = world.getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");
            worldNbt.addWarpCore(pos, "Warp Tree");
        }
        return true;
    }
}
