package gui;

import com.youarethomas.arborealis.Arborealis;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class StencilBagSlot extends Slot {
    public StencilBagSlot(Inventory inventory, int i, int j, int k) {
        super(inventory, i, j, k);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.isOf(Arborealis.CARVED_STENCIL);
    }
}
