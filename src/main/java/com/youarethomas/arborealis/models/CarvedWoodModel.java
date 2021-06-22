package com.youarethomas.arborealis.models;

import com.youarethomas.arborealis.blocks.CarvedWood;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class CarvedWoodModel extends DynamicModel {

    private final String log;
    private final String strippedLog;
    private final String logTop;

    String logID = "minecraft:block/oak_log";

    public CarvedWoodModel() {
        String[] idParts = logID.split("/");

        log = logID;
        strippedLog = idParts[0] + "/stripped_" + idParts[1];
        logTop = logID + "_top";

        loadModel();
    }

    @Override
    public void loadModel() {
        setBreakTexture(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));

        DynamicCuboid core = new DynamicCuboid(1, 1, 1, 14, 14, 14);
        core.applyTextureToAllSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(strippedLog)));
        addCuboid(core);

        DynamicCuboid top = new DynamicCuboid(0, 15, 0, 16, 1, 16);
        top.applyTextureToAllSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
        top.applyTexture(Direction.UP, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
        addCuboid(top);

        DynamicCuboid bottom = new DynamicCuboid(0, 0, 0, 16, 1, 16);
        bottom.applyTextureToAllSides(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(log)));
        bottom.applyTexture(Direction.DOWN, new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier(logTop)));
        addCuboid(bottom);
    }

}
