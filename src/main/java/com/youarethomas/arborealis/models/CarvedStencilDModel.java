package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.models.model_utils.DynamicModel;
import com.youarethomas.arborealis.models.model_utils.DynamicPlane;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

public class CarvedStencilDModel extends DynamicModel {

    private final SpriteIdentifier STENCIL_TEXTURE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(Arborealis.MOD_ID, "item/stencil_carved"));

    @Override
    public void createBlockQuads(DynamicModelBuilder builder, BlockRenderView renderView, BlockPos pos) {
        // Nope! Item time
    }

    @Override
    public void createItemQuads(DynamicModelBuilder builder, ItemStack itemStack) {
        builder.addBakedModel(builder.getModelFromItem(Arborealis.BLANK_STENCIL));

        NbtCompound nbt = itemStack.getNbt();
        if (nbt != null && nbt.contains("pattern")) {
            int[] pattern = nbt.getIntArray("pattern");

            // TODO: make pixels 2x2 instead of 1x1 with new 5x5 runes
            for (int y = 1; y < 6; y++) {
                for (int x = 1; x < 6; x++) {
                    int carveState = pattern[(x - 1) + ((y - 1) * 5)];

                    if (carveState != 0) {
                        DynamicPlane runePlane = new DynamicPlane(Direction.SOUTH, x + 4, y + 5, x + 5, y + 4, 7.4f);
                        runePlane.applyTexture(STENCIL_TEXTURE.getSprite());
                        builder.addPlane(runePlane);
                    }
                }
            }
        }
    }

    @Override
    public boolean renderItemAsBlock() { return false; }
}
