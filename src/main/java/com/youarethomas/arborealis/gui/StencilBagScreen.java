package com.youarethomas.arborealis.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.items.StencilBag;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StencilBagScreen extends HandledScreen<StencilBagScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Arborealis.MOD_ID, "textures/gui/stencil_bag.png");
    private ItemStack openBag;

    public StencilBagScreen(StencilBagScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, inventory.getStack(inventory.selectedSlot).getName());
        openBag = inventory.getStack(inventory.selectedSlot);
        this.backgroundHeight = 148;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        NbtCompound nbt = openBag.getNbt();
        if (nbt != null && nbt.contains("selected")) {
            int selectedSlot = nbt.getInt("selected");
            drawTexture(matrices, x + ((selectedSlot % 9) * 18) + 7, y + ((selectedSlot / 9) * 18) + 17, 181, 17, 18, 18);
        }
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        // Override to draw only the bag name, but in white, and not the player inventory title
        this.textRenderer.draw(matrices, this.title, (float)this.titleX, (float)this.titleY, 0xe4e4e4);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
