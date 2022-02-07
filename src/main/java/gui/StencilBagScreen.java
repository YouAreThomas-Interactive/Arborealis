package gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.items.StencilBag;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StencilBagScreen extends HandledScreen<StencilBagScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Arborealis.MOD_ID, "textures/gui/stencil_bag.png");

    public StencilBagScreen(StencilBagScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, StencilBag.openBag.getName());
        this.backgroundHeight = 148;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        drawTexture(matrices, x + 8, y + 18, 182, 18, 16, 16);
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
