package codechicken.nei.api;

import codechicken.nei.VisibilityData;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * If this is implemented on a gui, it will be automatically registered
 */
public interface INEIGuiHandler {

    default VisibilityData modifyVisibility(GuiContainer gui, VisibilityData currentVisibility) {
        return currentVisibility;
    }

    /**
     * NEI will give the specified item to the InventoryRange returned if the player's inventory is full.
     * Should not return null, just an empty list
     */
    default Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return Collections.emptyList();
    }

    /**
     * @return A list of TaggedInventoryAreas that will be used with the savestates.
     */
    default List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    /**
     * Handles clicks while an itemstack has been dragged from the item panel. Use this to set configurable slots and the like.
     * Changes made to the stackSize of the dragged stack will be kept
     *
     * @param gui          The current gui instance
     * @param mouseX       The x position of the mouse
     * @param mouseY       The y position of the mouse
     * @param draggedStack The stack being dragged from the item panel
     * @param button       The button presed
     * @return True if the drag n drop was handled. False to resume processing through other routes. The held stack will be deleted if draggedStack.stackSize == 0
     */
    default boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        return false;
    }

}
