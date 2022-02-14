package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.models.model_utils.DynamicCuboid;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class WarpCoreDModel extends DynamicModel {
@Override
public void createBlockQuads(CuboidBuilder builder, BlockRenderView renderView, BlockPos pos) {
    BlockState lodestoneState = Blocks.LODESTONE.getDefaultState();
    BlockState warpWoodState = Arborealis.WARP_WOOD.getDefaultState();

    DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
    core.applyTexturesFromBlock(lodestoneState);
    builder.addCuboid(core);

    BakedModel frame = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/warp_core_frame"));
    builder.addBakedModel(frame, new CuboidBuilder.RetextureFromBlock(warpWoodState, false));
}

    @Override
    public void createItemQuads(CuboidBuilder builder, ItemStack itemStack) {
        BlockState lodestoneState = Blocks.LODESTONE.getDefaultState();
        BlockState warpWoodState = Arborealis.WARP_WOOD.getDefaultState();

        DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
        core.applyTexturesFromBlock(lodestoneState);
        builder.addCuboid(core);

        BakedModel frame = builder.getModel(new Identifier(Arborealis.MOD_ID, "block/warp_core_frame"));
        builder.addBakedModel(frame, new CuboidBuilder.RetextureFromBlock(warpWoodState, false));
    }

    @Override
    public SpriteIdentifier getParticleSpriteId() {
        return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "block/warp_log"));
    }
}
