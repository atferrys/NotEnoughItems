package codechicken.nei.layout;

import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIController;
import codechicken.nei.VisibilityData;
import codechicken.nei.jei.JEIIntegrationManager;
import java.awt.Rectangle;
import codechicken.nei.util.NEIClientUtils;
import codechicken.nei.widget.Button;
import codechicken.nei.widget.action.NEIActions;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;

import static codechicken.lib.gui.GuiDraw.drawStringC;
import static codechicken.lib.gui.GuiDraw.getStringWidth;
import static codechicken.nei.LayoutManager.*;
import static codechicken.nei.NEIClientConfig.*;

public final class LayoutStyle {

    private int stateButtonCount;
    private int clickButtonCount;

    public void init() {
        delete.icon = new Rectangle4i(144, 12, 12, 12);
        gamemode.icons[0] = new Rectangle4i(132, 12, 12, 12);
        gamemode.icons[1] = new Rectangle4i(156, 12, 12, 12);
        gamemode.icons[2] = new Rectangle4i(168, 12, 12, 12);
        rain.icon = new Rectangle4i(120, 12, 12, 12);
        magnet.icon = new Rectangle4i(180, 24, 12, 12);
        timeButtons[0].icon = new Rectangle4i(132, 24, 12, 12);
        timeButtons[1].icon = new Rectangle4i(120, 24, 12, 12);
        timeButtons[2].icon = new Rectangle4i(144, 24, 12, 12);
        timeButtons[3].icon = new Rectangle4i(156, 24, 12, 12);
        heal.icon = new Rectangle4i(168, 24, 12, 12);
        dropDown.x = 90;
    }

    private void reset() {
        stateButtonCount = clickButtonCount = 0;
    }

    private void layoutButton(Button button) {
        if ((button.state & 0x4) != 0) {
            button.x = 6 + stateButtonCount * 20;
            button.y = 3;
            stateButtonCount++;
        } else {
            button.x = 6 + (clickButtonCount % 4) * 20;
            button.y = 3 + (1 + clickButtonCount / 4) * 18;
            clickButtonCount++;
        }

        button.h = 17;
        button.w = button.contentWidth() + 6;
    }

    public void drawButton(Button b, int mousex, int mousey) {
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);

        int tex;
        if ((b.state & 0x3) == 2) {
            tex = 0;
        } else if ((b.state & 0x4) == 0 && b.contains(mousex, mousey) ||//not a state button and mouseover
                (b.state & 0x3) == 1)//state active
        {
            tex = 2;
        } else {
            tex = 1;
        }
        LayoutManager.drawButtonBackground(b.x, b.y, b.w, b.h, true, tex);

        Rectangle4i icon = b.getRenderIcon();
        if (icon == null) {
            int colour = tex == 2 ? 0xffffa0 : tex == 0 ? 0x601010 : 0xe0e0e0;

            drawStringC(b.getRenderLabel(), b.x + b.w / 2, b.y + (b.h - 8) / 2, colour);
        } else {
            GlStateManager.color(1, 1, 1, 1);

            int iconx = b.x + (b.w - icon.w) / 2;
            int icony = b.y + (b.h - icon.h) / 2;
            LayoutManager.drawIcon(iconx, icony, icon);
        }
    }

    public void drawSubsetTag(String text, int x, int y, int w, int h, int state, boolean mouseover) {
        if (state == 1) {
            GlStateManager.color(0.65F, 0.65F, 0.65F, 1.0F);
        } else {
            GlStateManager.color(1, 1, 1, 1);
        }
        LayoutManager.drawButtonBackground(x, y, w, h, false, state == 0 ? 0 : 1);
        if (text != null) {
            drawStringC(text, x, y, w, h, mouseover ? 0xFFFFFFA0 : (state == 2 ? 0xFFE0E0E0 : 0xFFA0A0A0));
        }
    }

    public void layout(GuiContainer gui, VisibilityData visiblity) {
        int windowWidth = gui.width;
        int windowHeight = gui.height;
        int containerWidth = gui.getXSize();
        int containerLeft = gui.getGuiLeft();

        reset();

        more.w = more.h = less.w = less.h = 16;
        Rectangle panel = JEIIntegrationManager.getItemPanelBounds();
        int pickerLeft = panel == null ? containerLeft + containerWidth + 3 : panel.x;
        int pickerWidth = panel == null ? windowWidth - pickerLeft - 2 : panel.width;
        less.x = pickerLeft;
        more.x = pickerLeft + pickerWidth - more.w;

        int bottomPadding = JEIIntegrationManager.isSearchBarCentered(gui) ? 4 : 26;
        more.y = less.y = windowHeight - more.h - bottomPadding;

        quantity.x = less.x + less.w + 2;
        quantity.y = less.y;
        quantity.w = more.x - quantity.x - 2;
        quantity.h = less.h;

        delete.state = 0x4;
        if (NEIController.getDeleteMode()) {
            delete.state |= 1;
        } else if (!visiblity.enableDeleteMode) {
            delete.state |= 2;
        }

        rain.state = 0x4;
        if (disabledActions.contains("rain")) {
            rain.state |= 2;
        } else if (NEIClientUtils.isRaining()) {
            rain.state |= 1;
        }

        gamemode.state = 0x4;
        if (NEIClientUtils.getGamemode() != 0) {
            gamemode.state |= 0x1;
            gamemode.index = NEIClientUtils.getGamemode() - 1;
        } else {
            if (NEIClientUtils.isValidGamemode("creative")) {
                gamemode.index = 0;
            } else if (NEIClientUtils.isValidGamemode("creative+")) {
                gamemode.index = 1;
            } else if (NEIClientUtils.isValidGamemode("adventure")) {
                gamemode.index = 2;
            }
        }

        magnet.state = 0x4 | (isMagnetModeEnabled() ? 1 : 0);

        if (canPerformAction("delete")) {
            layoutButton(delete);
        }
        if (canPerformAction("rain")) {
            layoutButton(rain);
        }
        if (NEIClientUtils.isValidGamemode("creative") || NEIClientUtils.isValidGamemode("creative+") || NEIClientUtils.isValidGamemode("adventure")) {
            layoutButton(gamemode);
        }
        if (canPerformAction("magnet")) {
            layoutButton(magnet);
        }
        if (canPerformAction("time")) {
            for (int i = 0; i < 4; i++) {
                timeButtons[i].state = disabledActions.contains(NEIActions.timeZones[i]) ? 2 : 0;
                layoutButton(timeButtons[i]);
            }
        }
        if (canPerformAction("heal")) {
            layoutButton(heal);
        }

        dropDown.h = 20;
        dropDown.w = Math.max(0, containerLeft + containerWidth - dropDown.x - 3);
        dropDown.y = 2;

        int maxWidth = 0;
        for (int i = 0; i < 7; i++) {
            deleteButtons[i].w = 16;
            deleteButtons[i].h = 16;

            NBTTagCompound statelist = global.nbt.getCompoundTag("statename");
            global.nbt.setTag("statename", statelist);
            String name = statelist.getString("" + i);
            if (statelist.getTag("" + i) == null) {
                name = "" + (i + 1);
                statelist.setString("" + i, name);
            }
            stateButtons[i].label = name;
            stateButtons[i].saved = isStateSaved(i);

            int width = getStringWidth(stateButtons[i].getRenderLabel()) + 26;
            if (width + 22 > containerLeft) {
                width = containerLeft - 22;
            }

            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        for (int i = 0; i < 7; i++) {
            stateButtons[i].x = 0;
            stateButtons[i].y = 58 + i * 22;
            stateButtons[i].h = 20;

            stateButtons[i].x = 0;
            stateButtons[i].w = maxWidth;
            deleteButtons[i].x = stateButtons[i].w + 3;
            deleteButtons[i].y = stateButtons[i].y + 2;
        }
    }

}
