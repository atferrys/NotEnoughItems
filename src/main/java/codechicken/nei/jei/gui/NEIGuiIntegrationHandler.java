package codechicken.nei.jei.gui;

import codechicken.nei.LayoutManager;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;

public class NEIGuiIntegrationHandler implements IGlobalGuiHandler {

    private static final Minecraft MC = Minecraft.getMinecraft();

    // Make JEI account for NEI widgets like Item Subsets, Utility buttons etc...
    @MethodsReturnNonnullByDefault
    @Override
    public Collection<Rectangle> getGuiExtraAreas() {

        if(!(MC.currentScreen instanceof GuiContainer) || LayoutManager.instance() == null) {
            return Collections.emptyList();
        }

        return LayoutManager.getGUIAreas((GuiContainer) MC.currentScreen);

    }

    // Make items in NEI widgets (e.g. Item Subsets) interactable from JEI to view recipes etc...
    @Override
    public Object getIngredientUnderMouse(int mouseX, int mouseY) {

        if(!(MC.currentScreen instanceof GuiContainer) || LayoutManager.instance() == null) {
            return null;
        }

        ItemStack stack = LayoutManager.instance().getStackUnderMouse((GuiContainer) MC.currentScreen, mouseX, mouseY);

        if(stack.isEmpty()) {
            return null;
        }

        return stack;

    }

}
