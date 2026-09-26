package codechicken.nei.gui;

import codechicken.lib.gui.GuiCCButton;
import codechicken.lib.gui.GuiScrollSlot;
import codechicken.lib.inventory.container.GuiContainerWidget;
import codechicken.lib.texture.TextureUtils;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.container.ContainerEnchantmentModifier;
import codechicken.nei.network.NEIClientPacketHandler;
import codechicken.nei.util.NEIServerUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.Dimension;
import java.util.*;

import static codechicken.nei.util.NEIClientUtils.translate;

public class GuiEnchantmentModifier extends GuiContainerWidget {

    private int level = 5;
    private GuiCCButton levelDown;
    private GuiCCButton levelUp;
    private GuiCCButton validationButton;
    private GuiSlotEnchantments enchantments;
    private ItemStack lastStack = ItemStack.EMPTY;
    private boolean lastValidation;

    public GuiEnchantmentModifier(InventoryPlayer inventoryplayer, World world) {
        super(new ContainerEnchantmentModifier(inventoryplayer, world), 176, 166);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        fontRenderer.drawString(translate("enchant"), 12, 6, 0x404040);
        fontRenderer.drawString(translate("enchant.level"), 19, 20, 0x404040);
    }

    @Override
    public void drawBackground() {
        GlStateManager.color(1, 1, 1, 1);
        TextureUtils.changeTexture("textures/gui/container/enchanting_table.png");
        drawTexturedModalRect(0, 0, 0, 0, xSize, ySize);

        String levelstring = Integer.toString(level);
        fontRenderer.drawString(levelstring, 33 - fontRenderer.getStringWidth(levelstring) / 2, 34, 0xFF606060);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void addWidgets() {
        add(levelDown = new GuiCCButton(10, 31, 12, 12, "<").setActionCommand("levelDown"));
        add(levelUp = new GuiCCButton(44, 31, 12, 12, ">").setActionCommand("levelUp"));
        add(validationButton = new GuiCCButton(8, 68, 50, 12, lockDisplayString()).setActionCommand("validation"));
        add(enchantments = new GuiSlotEnchantments());
        refreshEnchantments();
        updateLevelButtons();
    }

    private String lockDisplayString() {
        return validateEnchantments() ? translate("enchant.locked") : translate("enchant.unlocked");
    }

    public static boolean validateEnchantments() {
        return NEIClientConfig.world.nbt.getBoolean("validateenchantments");
    }

    public static void toggleEnchantmentValidation() {
        NEIClientConfig.world.nbt.setBoolean("validateenchantments", !validateEnchantments());
        NEIClientConfig.world.saveNBT();
    }

    @Override
    public void actionPerformed(String ident, Object... params) {

        switch(ident) {
            case "levelDown":
                level = Math.max(1, level - 1);
                break;
            case "levelUp":
                level = Math.min(10, level + 1);
                break;
            case "validation":
                toggleEnchantmentValidation();
                refreshEnchantments();
                break;
        }

        updateLevelButtons();

    }

    private void updateLevelButtons() {
        levelDown.setEnabled(level > 1);
        levelUp.setEnabled(level < 10);
    }

    private void refreshEnchantments() {
        lastStack = inventorySlots.getSlot(0).getStack().copy();
        lastValidation = validateEnchantments();
        enchantments.refresh(lastStack, lastValidation);
        validationButton.text = lockDisplayString();
    }

    @Override
    public void updateScreen() {

        super.updateScreen();

        if(!ItemStack.areItemStacksEqual(lastStack, inventorySlots.getSlot(0).getStack()) || lastValidation != validateEnchantments()) {
            refreshEnchantments();
        }

    }

    private static class EnchantmentOption {

        private final Enchantment enchantment;
        private final int state;
        private final int appliedLevel;

        private EnchantmentOption(Enchantment enchantment, int state, int appliedLevel) {
            this.enchantment = enchantment;
            this.state = state;
            this.appliedLevel = appliedLevel;
        }

    }

    public class GuiSlotEnchantments extends GuiScrollSlot {

        private final LinkedHashMap<String, String> ENCHANTMENT_SHORTHANDS = new LinkedHashMap<String, String>() {{
            put("Projectile", "Proj");
            put("Protection", "Protect");
            put("Bane of ", "");
        }};

        private final List<EnchantmentOption> options = new ArrayList<>();

        public GuiSlotEnchantments() {
            super(60, 14, 108, 57);
            setSmoothScroll(false);
            setMargins(0, 0, 0, 0);
        }

        @Override
        public int getSlotHeight(int slot) {
            return 19;
        }

        @Override
        protected int getNumSlots() {
            return options.size();
        }

        @Override
        public int scrollbarGuideAlignment() {
            return 0;
        }

        @Override
        public Dimension scrollbarDim() {
            Dimension dim = super.scrollbarDim();
            dim.width = 7;
            return dim;
        }

        @Override
        public void drawScrollbar(float frame) {
            if(hasScrollbar()) {
                super.drawScrollbar(frame);
            }
        }

        @Override
        public void drawOverlay(float frame) {

        }

        @Override
        public void mouseScrolled(int mouseX, int mouseY, int direction) {
            if(contains(mouseX - guiLeft, mouseY - guiTop) && hasScrollbar()) {
                scroll(-direction);
            }
        }

        @Override
        public void drawBackground(float frame) {
            drawRect(x, y, x + width, y + height, 0xFF202020);
            for(int row = 0; row < 3; row++) {
                drawEntryBackground(x, y + row * 19, width, 1);
            }
        }

        private void drawEntryBackground(int x, int y, int width, int state) {

            TextureUtils.changeTexture("textures/gui/container/enchanting_table.png");
            GlStateManager.color(1, 1, 1, 1);

            if(!hasScrollbar()) {
                drawTexturedModalRect(x, y, 0, 166 + 19 * state, width, 19);
                return;
            }

            drawTexturedModalRect(x, y, 0, 166 + 19 * state, width - 30, 19);
            drawTexturedModalRect(x + width - 30, y, width - 23, 166 + 19 * state, 30, 19);

        }

        @Override
        protected void drawSlot(int slot, int x, int y, int mx, int my, float frame) {

            EnchantmentOption option = options.get(slot);
            int width = windowBounds().width;
            drawEntryBackground(x, y, width, option.state);

            String text = option.enchantment.getTranslatedName(option.appliedLevel == -1 ? level : option.appliedLevel);

            for(Map.Entry<String, String> shorthands : ENCHANTMENT_SHORTHANDS.entrySet()) {
                if(text.contains(shorthands.getKey())) {
                    text = text.replace(shorthands.getKey(), shorthands.getValue());
                    break;
                }
            }

            int availableWidth = width - 6;

            if(fontRenderer.getStringWidth(text) > availableWidth) {
                text = fontRenderer.trimStringToWidth(
                        text,
                        availableWidth - fontRenderer.getStringWidth("...")
                );
                text += "...";
            }

            int colour = option.state == 0 ? 0x685e4a : option.state == 1 ? 0x407f10 : 0xffff80;
            fontRenderer.drawString(text, x + 4, y + 5, colour);

        }

        @Override
        protected void slotClicked(int slot, int button, int mx, int my, int count) {

            EnchantmentOption option = options.get(slot);

            if(option.state != 1) {
                boolean add = option.state != 2;
                NEIClientPacketHandler.sendModifyEnchantment(option.enchantment, add ? level : 0, add);
            }

        }

        public void refresh(ItemStack stack, boolean validate) {

            int oldSize = options.size();
            options.clear();

            if(!stack.isEmpty() && (!validate || stack.getItem().getItemEnchantability(stack) != 0)) {

                for(Enchantment enchantment : Enchantment.REGISTRY) {

                    if(enchantment == null || enchantment.type == null) {
                        continue;
                    }

                    if(validate && !enchantment.type.canEnchantItem(stack.getItem())) {
                        continue;
                    }

                    int state = 0;
                    int appliedLevel = -1;

                    if(NEIServerUtils.stackHasEnchantment(stack, enchantment)) {
                        state = 2;
                        appliedLevel = NEIServerUtils.getEnchantmentLevel(stack, enchantment);
                    } else if(validate && NEIServerUtils.doesEnchantmentConflict(NEIServerUtils.getEnchantments(stack).keySet(), enchantment)) {
                        state = 1;
                    }

                    options.add(new EnchantmentOption(enchantment, state, appliedLevel));

                }

            }

            setMargins(0, 0, options.size() > 3 ? 7 : 0, 0);

            if(oldSize != options.size()) {
                percentscrolled = 0;
            }

        }

    }

}
