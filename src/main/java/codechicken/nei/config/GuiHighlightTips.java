package codechicken.nei.config;

import codechicken.lib.gui.GuiDraw;
import codechicken.lib.math.MathHelper;
import codechicken.nei.client.render.HUDRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GuiHighlightTips extends GuiScreen {

    private final Option option;
    private final GuiScreen parent;
    private final String name;
    private final ItemStack sample = new ItemStack(Blocks.REDSTONE_BLOCK);
    private GuiButton toggleButton;
    private Point dragDown;

    public GuiHighlightTips(Option option) {
        this.option = option;
        this.name = option.configName();
        this.parent = Minecraft.getMinecraft().currentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        toggleButton = addButton(new GuiButton(0, width / 2 - 40, height / 2 - 10, 80, 20, ""));
        addButton(new GuiButton(1, width / 2 - 40, height - 30, 80, 20, I18n.format("gui.done")));
        updateNames();
    }

    private boolean show() {
        return option.renderTag().getBooleanValue();
    }

    private void updateNames() {
        toggleButton.displayString = I18n.format("nei.options." + name + (show() ? ".show" : ".hide"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id == 0) {
            option.getTag().setBooleanValue(!show());
            updateNames();
        } else if(button.id == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    private List<String> sampleText() {
        return Arrays.asList(sample.getDisplayName(), TextFormatting.RED + I18n.format("nei.options." + name + ".sample"));
    }

    public Point getPos() {
        return new Point(option.renderTag(name + ".x").getIntValue(), option.renderTag(name + ".y").getIntValue());
    }

    public Point getDrag() {

        Point mouse = GuiDraw.getMousePosition();
        Point drag = new Point(mouse.x - dragDown.x, mouse.y - dragDown.y);
        Dimension size = GuiDraw.getDisplaySize();
        Dimension sample = HUDRenderer.getSize(sampleText());

        drag.x *= 10000;
        drag.y *= 10000;
        drag.x /= (size.width - sample.width);
        drag.y /= (size.height - sample.height);

        Point pos = getPos();
        drag.x = MathHelper.clip(drag.x, -pos.x, 10000 - pos.x);
        drag.y = MathHelper.clip(drag.y, -pos.y, 10000 - pos.y);

        return drag;

    }

    public Point renderPos() {

        Point pos = getPos();

        if(dragDown != null) {
            Point drag = getDrag();
            pos.x += drag.x;
            pos.y += drag.y;
        }

        // Snapping
        for(int i = 25; i < 100; i += 25) {

            if(pos.x / 100 == i) {
                pos.x = i * 100;
            }

            if(pos.y / 100 == i) {
                pos.y = i * 100;
            }

        }

        return pos;

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("nei.options." + name), width / 2, 15, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if(show()) {
            HUDRenderer.renderOverlay(sample, sampleText(), renderPos());
        }

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if(show() && button == 0 && HUDRenderer.getBounds(sampleText(), renderPos()).contains(mouseX, mouseY)) {
            dragDown = new Point(mouseX, mouseY);
        } else {
            super.mouseClicked(mouseX, mouseY, button);
        }
    }

    private void finishDrag() {
        if(dragDown != null) {
            Point pos = renderPos();
            option.getTag().setBooleanValue(show());
            option.getTag(name + ".x").setIntValue(pos.x);
            option.getTag(name + ".y").setIntValue(pos.y);
            dragDown = null;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {

        if(button == 0) {
            finishDrag();
        }

        super.mouseReleased(mouseX, mouseY, button);

    }

    @Override
    public void onGuiClosed() {
        finishDrag();
    }

    @Override
    protected void keyTyped(char character, int keyCode) throws IOException {
        if(keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_BACK) {
            mc.displayGuiScreen(parent);
        } else {
            super.keyTyped(character, keyCode);
        }
    }

}
