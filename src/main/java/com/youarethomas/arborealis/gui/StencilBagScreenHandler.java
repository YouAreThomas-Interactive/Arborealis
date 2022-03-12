package com.youarethomas.arborealis.gui;

import com.youarethomas.arborealis.Arborealis;
import com.youarethomas.arborealis.items.StencilBag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class StencilBagScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    // This constructor gets called on the client when the server wants it to open the screenHandler,
    // The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    // sync this empty inventory with the inventory on the server.
    public StencilBagScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(StencilBag.BAG_SLOTS));
    }

    // This constructor gets called from the StencilBag on the server without calling the other constructor first, the server knows the inventory of the container
    // and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public StencilBagScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(Arborealis.STENCIL_BAG_SCREEN_HANDLER, syncId);
        checkSize(inventory, StencilBag.BAG_SLOTS);
        this.inventory = inventory;

        inventory.onOpen(playerInventory.player); // Extra logic can be applied here

        // Bag inventory
        for (int y = 0; y < 2; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new StencilBagSlot(inventory, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }
        // The player inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 66 + y * 18));
            }
        }
        // The player hotbar
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 124));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack itemStack2 = slot.getStack();
            itemStack = itemStack2.copy();
            if (index < this.inventory.size() ? !this.insertItem(itemStack2, this.inventory.size(), this.slots.size(), true) : !this.insertItem(itemStack2, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return itemStack;
    }
}
