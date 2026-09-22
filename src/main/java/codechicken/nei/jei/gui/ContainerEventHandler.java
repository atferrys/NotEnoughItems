package codechicken.nei.jei.gui;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.jei.JEIIntegrationManager;
import codechicken.nei.util.NEIClientUtils;
import mezz.jei.config.Config;
import mezz.jei.input.GuiTextFieldFilter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.awt.Point;

public class ContainerEventHandler {

    private long lastSearchBoxClickTime;
    private GuiScreen pendingQuantityReleaseGui;

    private static boolean active() {
        return NEIClientConfig.world != null && NEIClientConfig.isEnabled() && !NEIClientConfig.isHidden();
    }

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            JEIIntegrationManager.tick();
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        pendingQuantityReleaseGui = null;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMouseInput(MouseInputEvent.Pre event) {

        if(!active() || !(event.getGui() instanceof GuiContainer)) {
            return;
        }

        int button = Mouse.getEventButton();
        Point mouse = GuiDraw.getMousePosition();

        if(button == -1) {
            preventScrollingBehindSubsets(event, mouse);
            return;
        }

        // We only care about left clicks
        if(button != 0) {
            return;
        }

        boolean mousePressed = Mouse.getEventButtonState();

        // Prevent dropping the stack outside the inventory when picking it from the item panel
        if(!mousePressed) {
            handleQuantityClickRelease(event);
            return;
        }

        handleSearchHighlight(mouse);
        handleQuantityClick(event);

    }

    private void preventScrollingBehindSubsets(MouseInputEvent.Pre event, Point mouse) {
        int wheel = Mouse.getEventDWheel();

        // Prevent scroll when subsets are open
        if(wheel != 0 && LayoutManager.dropDown.contains(mouse.x, mouse.y)
                && LayoutManager.dropDown.onMouseWheel(wheel > 0 ? 1 : -1, mouse.x, mouse.y)
        ) {
            event.setCanceled(true);
        }
    }

    private void handleSearchHighlight(Point mouse) {

        GuiTextFieldFilter searchField = JEIIntegrationManager.getTextFieldFilter();

        if(searchField == null || !searchField.isMouseOver(mouse.x, mouse.y)) {
            return;
        }

        long now = System.currentTimeMillis();

        if(searchField.isFocused() && now - lastSearchBoxClickTime < 500) {
            NEIClientConfig.world.nbt.setBoolean("searchinventories", !NEIClientConfig.world.nbt.getBoolean("searchinventories"));
            NEIClientConfig.world.saveNBT();
            lastSearchBoxClickTime = 0;
            return;
        }

        lastSearchBoxClickTime = now;

    }

    private void handleQuantityClick(MouseInputEvent.Pre event) {

        if(NEIClientConfig.getItemQuantity() <= 0 || !Config.isCheatItemsEnabled() || Config.isEditModeEnabled()) {
            return;
        }

        ItemStack stack = JEIIntegrationManager.getItemUnderMouse();

        if(stack.isEmpty() || !NEIClientConfig.canCheatItem(stack) || !NEIClientUtils.getHeldItem().isEmpty()) {
            return;
        }

        JEIIntegrationManager.giveSelectedQuantity(stack);

        // Remember the gui we handled the press in, so we can cancel the release event and prevent the items from dropping
        pendingQuantityReleaseGui = event.getGui();
        event.setCanceled(true);

    }

    private void handleQuantityClickRelease(MouseInputEvent.Pre event) {

        if(pendingQuantityReleaseGui == null) {
            return;
        }

        if(pendingQuantityReleaseGui == event.getGui()) {
            event.setCanceled(true);
        }

        pendingQuantityReleaseGui = null;

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void drawSearchHighlight(BackgroundDrawnEvent event) {

        if(!active() || !NEIClientConfig.world.nbt.getBoolean("searchinventories")) {
            return;
        }

        GuiTextFieldFilter field = JEIIntegrationManager.getTextFieldFilter();

        if(field == null) {
            return;
        }

        int x = field.x, y = field.y;
        int w = field.width, h = field.height;

        GuiDraw.drawGradientRect(x - 1, y - 1, 1, h + 2, 0xFFFFFF00, 0xFFC0B000);
        GuiDraw.drawGradientRect(x - 1, y - 1, w + 2, 1, 0xFFFFFF00, 0xFFC0B000);
        GuiDraw.drawGradientRect(x + w, y - 1, 1, h + 2, 0xFFFFFF00, 0xFFC0B000);
        GuiDraw.drawGradientRect(x - 1, y + h, w + 2, 1, 0xFFFFFF00, 0xFFC0B000);

    }

}