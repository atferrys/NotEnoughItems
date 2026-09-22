package codechicken.nei.client.render;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.render.state.GlStateTracker;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.KeyBindings;
import codechicken.nei.handler.KeyManager.IKeyStateTracker;
import codechicken.nei.util.helper.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Comparator;
import java.util.List;

public class HUDRenderer implements IKeyStateTracker {

    public static final HUDRenderer INSTANCE = new HUDRenderer();

    @Override
    public void tickKeyStates() {
        if(KeyBindings.get("nei.options.keys.world.highlight_tips").isPressed()) {
            NEIClientConfig.getSetting("world.highlight_tips")
                    .setBooleanValue(!NEIClientConfig.getBooleanSetting("world.highlight_tips"));
        }
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post event) {

        Minecraft mc = Minecraft.getMinecraft();

        if(event.getType() != RenderGameOverlayEvent.ElementType.ALL || !NEIClientConfig.isEnabled()
                || mc.currentScreen != null || mc.world == null || mc.player == null || mc.gameSettings.hideGUI
                || mc.gameSettings.keyBindPlayerList.isKeyDown()
                || !NEIClientConfig.getBooleanSetting("world.highlight_tips")
                || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }

        List<ItemStack> items = HighlightHandler.getIdentifierItems(mc.world, mc.player, mc.objectMouseOver);
        ItemStack stack = items.stream().min(Comparator.comparingInt(ItemStack::getItemDamage)).orElse(ItemStack.EMPTY);

        if(!stack.isEmpty()) {
            renderOverlay(stack, HighlightHandler.getText(stack, mc.world, mc.player, mc.objectMouseOver), getPositioning());
        }

    }

    private static Point getPositioning() {
        return new Point(
                NEIClientConfig.getSetting("world.highlight_tips.x").getIntValue(),
                NEIClientConfig.getSetting("world.highlight_tips.y").getIntValue()
        );
    }

    public static Dimension getSize(List<String> text) {

        int width = 29;

        for(String line : text) {
            width = Math.max(width, GuiDraw.getStringWidth(line) + 29);
        }

        return new Dimension(width, Math.max(24, 10 + 10 * text.size()));

    }

    public static Rectangle getBounds(List<String> text, Point pos) {

        Dimension screen = GuiDraw.getDisplaySize();
        Dimension size = getSize(text);

        return new Rectangle(
                (screen.width - size.width - 1) * pos.x / 10000,
                (screen.height - size.height - 1) * pos.y / 10000,
                size.width,
                size.height
        );

    }

    public static void renderOverlay(ItemStack stack, List<String> text, Point pos) {

        if(stack.isEmpty() && text.isEmpty()) {
            return;
        }

        GlStateTracker.pushState();
        GlStateManager.pushMatrix();

        GlStateManager.disableRescaleNormal();
        GuiHelper.enable2DRender();

        Rectangle bounds = getBounds(text, pos);
        drawTooltipBox(bounds);

        int textY = bounds.y + (bounds.height - (text.size() * 10 - 2)) / 2;

        for(int i = 0; i < text.size(); i++) {
            GuiDraw.drawString(text.get(i), bounds.x + 24, textY + 10 * i, 0xFFA0A0A0, true);
        }

        if(!stack.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GuiHelper.drawItem(bounds.x + 5, bounds.y + bounds.height / 2 - 8, stack);
        }

        GlStateManager.popMatrix();
        GlStateTracker.popState();

    }

    private static void drawTooltipBox(Rectangle bounds) {

        int x = bounds.x, y = bounds.y;
        int w = bounds.width,h = bounds.height;

        int background = 0xF0100010;
        int top = 0x505000FF;
        int bottom = 0x5028007F;

        GuiDraw.drawRect(x + 1, y, w - 2, h, background);
        GuiDraw.drawRect(x, y + 1, 1, h - 2, background);
        GuiDraw.drawRect(x + w - 1, y + 1, 1, h - 2, background);
        GuiDraw.drawRect(x + 1, y + 1, w - 2, 1, top);
        GuiDraw.drawRect(x + 1, y + h - 2, w - 2, 1, bottom);
        GuiDraw.drawGradientRect(x + 1, y + 2, 1, h - 4, top, bottom);
        GuiDraw.drawGradientRect(x + w - 2, y + 2, 1, h - 4, top, bottom);

    }

}
