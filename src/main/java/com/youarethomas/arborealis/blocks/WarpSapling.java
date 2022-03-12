package com.youarethomas.arborealis.blocks;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.misc.ArborealisPersistentState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Optional;

public class WarpSapling extends Block {
    protected static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public WarpSapling(Settings settings) {
        super(settings.nonOpaque().sounds(BlockSoundGroup.SLIME).noCollision());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world instanceof ServerWorld serverWorld) {
            StructureManager structureManager = serverWorld.getStructureManager();
            Optional<Structure> warpTreeStructure = structureManager.getStructure(new Identifier(Arborealis.MOD_ID, "warp_tree"));
            if (warpTreeStructure.isPresent()) {
                System.out.println("Placing...");
                place(serverWorld, pos, warpTreeStructure.get());
            } else
                System.out.println("Failed");

        }

        return ActionResult.SUCCESS;
    }

    public boolean place(ServerWorld world, BlockPos pos, Structure structure) {
        // Get size and rotation
        Vec3i structureSize = structure.getSize();
        StructurePlacementData placementData = (new StructurePlacementData()).setRotation(BlockRotation.NONE); // TODO: Random rotation

        // Place tree at centred position
        BlockPos placementPos = new BlockPos(pos.getX() - (structureSize.getX() / 2), pos.getY(), pos.getZ() - (structureSize.getZ() / 2));
        structure.place(world, placementPos, placementPos, placementData, Arborealis.RANDOM, 2);

        // Add core to the list
        if (world.getBlockState(pos).isOf(Arborealis.WARP_CORE)) {
            ArborealisPersistentState worldNbt = world.getPersistentStateManager().getOrCreate(ArborealisPersistentState::fromNbt, ArborealisPersistentState::new, "warp_cores");
            worldNbt.addWarpCore(pos);
        }
        return true;
    }
}
