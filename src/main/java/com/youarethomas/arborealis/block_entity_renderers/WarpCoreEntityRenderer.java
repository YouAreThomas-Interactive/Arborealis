package com.youarethomas.arborealis.block_entity_renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.block_entities.WarpCoreEntity;
import com.youarethomas.arborealis.util.ArborealisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.joml.Quaternionf;

import java.util.Map;

public class WarpCoreEntityRenderer implements BlockEntityRenderer<WarpCoreEntity> {
    TextRenderer textRenderer;

    public WarpCoreEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.textRenderer = ctx.getTextRenderer();
    }

    @Override
    public void render(WarpCoreEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Vec3d thisCorePos = Vec3d.ofCenter(entity.getPos());

        // If the player is in the warp chamber box
        if (player != null) {
            if (player.getBoundingBox().intersects(new Box(new Vec3d(thisCorePos.getX() - 1D, thisCorePos.getY() + 1D, thisCorePos.getZ() - 1D), new Vec3d(thisCorePos.getX() + 1D, thisCorePos.getY() + 3D, thisCorePos.getZ() + 1D)))) {
                entity.fadeAmount = Math.min(100, entity.fadeAmount + 5);
            } else {
                entity.fadeAmount = Math.max(0, entity.fadeAmount - 5);
            }

            if (entity.fadeAmount > 0f) {
                for (Map.Entry<BlockPos, String> entry : entity.getOtherCorePositions().entrySet()) {
                    BlockPos corePos = entry.getKey();
                    Vec3d otherCorePos = new Vec3d(corePos.getX() + 0.5d, corePos.getY() + 0.5d, corePos.getZ() + 0.5d);
                    Vec3d coreToOther = otherCorePos.subtract(thisCorePos);

                    double xzMagnitude = Math.sqrt(coreToOther.x * coreToOther.x + coreToOther.z * coreToOther.z);
                    float x = (float)((MathHelper.atan2(coreToOther.z, coreToOther.x)) - Math.PI / 2f);
                    float y = (float)(-(MathHelper.atan2(coreToOther.y, xzMagnitude)));
                    float z = 0f;
                    Quaternionf angleToPos = new Quaternionf().rotationXYZ(-x, y, z);

                    float hoverScaleMultiplier = 1f;
                    boolean showText = false;
                    if (entity.getSelectedWarpCore() != BlockPos.ORIGIN && corePos.equals(entity.getSelectedWarpCore()) ) {
                        hoverScaleMultiplier = 1.2f;
                        showText = true;
                    }

                    float scaleFactor = (float)coreToOther.length() * 0.004f * hoverScaleMultiplier;

                    // Warp Tree Icon
                    matrices.push();
                    matrices.translate(0.5f, 1.5f, 0.5f);
                    matrices.translate(coreToOther.x, coreToOther.y, coreToOther.z);
                    matrices.multiply(angleToPos);
                    matrices.scale(-scaleFactor, -scaleFactor, scaleFactor);
                    matrices.translate(0f, -20f, 0f);

                    RenderSystem.setShaderTexture(0, new Identifier(Arborealis.MOD_ID, "textures/item/warp_sapling.png"));
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(entity.fadeAmount / 100f, entity.fadeAmount / 100f, entity.fadeAmount / 100f, entity.fadeAmount / 100f);
                    DrawableHelper.drawTexture(matrices, -8, 0, 0, 16, 16, 16, 16, 16);

                    matrices.pop();

                    // Warp Tree Label
                    if (showText) {
                        matrices.push();
                        matrices.translate(0.5f, 1.5f, 0.5f);
                        matrices.translate(coreToOther.x, coreToOther.y, coreToOther.z);
                        matrices.multiply(angleToPos);
                        matrices.scale(-scaleFactor, -scaleFactor, scaleFactor);

                        TextRenderer textRenderer = this.textRenderer;
                        int colourWithFade = (int) ArborealisUtil.argbToHex((int)(255.0f * (entity.fadeAmount / 100f)), 255, 255, 255);
                        String text = entry.getValue();
                        textRenderer.draw(matrices, text, -(textRenderer.getWidth(text) / 2f), 0, colourWithFade);

                        matrices.pop();
                    }
                }
            }
        }
    }

}
