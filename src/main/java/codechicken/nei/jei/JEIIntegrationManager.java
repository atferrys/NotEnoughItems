package codechicken.nei.jei;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.LayoutManager;
import codechicken.nei.util.NEIClientUtils;
import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IGuiProperties;
import mezz.jei.config.Config;
import mezz.jei.config.ServerInfo;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.util.CommandUtilServer;
import mezz.jei.util.GiveMode;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import java.awt.Rectangle;
import codechicken.nei.util.ItemList;
import codechicken.nei.util.LogHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.IngredientGridWithNavigation;
import mezz.jei.input.GuiTextFieldFilter;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.client.Minecraft;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public final class JEIIntegrationManager {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static JeiRuntime runtime;
    private static IIngredientRegistry ingredientRegistry;
    private static SubsetVisibility subsetVisibility;
    private static Set<Rectangle> lastExtraAreas = Collections.emptySet();
    private static GuiScreen lastGui;

    private static Field guiScreenHelperField;
    private static Method isSearchBarCenteredMethod;
    private static Field searchField;
    private static Field ingredientContents;

    private static List<?> lastSearchResults;
    private static final Set<String> searchMatches = new HashSet<>();

    public static void onRuntimeAvailable(IJeiRuntime jeiRuntime, IIngredientRegistry ingredients, IIngredientBlacklist ingredientBlacklist) {

        runtime = (JeiRuntime) jeiRuntime;
        ingredientRegistry = ingredients;
        subsetVisibility = new SubsetVisibility(runtime, ingredients, ingredientBlacklist);

        lastSearchResults = null;
        lastGui = null;
        lastExtraAreas = Collections.emptySet();

        ItemList.refresh();

    }

    public static List<ItemStack> getSubsetItems() {
        return subsetVisibility == null
                ? Collections.emptyList()
                : subsetVisibility.getSubsetItems();
    }

    public static void refreshItemVisibility() {
        MC.addScheduledTask(() -> {
            if(subsetVisibility != null) {
                subsetVisibility.invalidate();
            }
        });
    }

    public static void tick() {

        if(runtime == null) {
            return;
        }

        subsetVisibility.update(
                MC.world != null && NEIClientConfig.isEnabled()
                && NEIClientConfig.getBooleanSetting("inventory.showSubsetsWidget")
        );

        GuiScreen gui = MC.currentScreen;
        Set<Rectangle> areas = gui instanceof GuiContainer && LayoutManager.instance() != null
                ? new HashSet<>(LayoutManager.getGUIAreas((GuiContainer) gui))
                : Collections.emptySet();

        if(gui != lastGui || !areas.equals(lastExtraAreas)) {
            lastGui = gui;
            lastExtraAreas = areas;
            runtime.getIngredientListOverlay().updateScreen(gui, true);
        }

    }

    private static String uid(ItemStack stack) {
        return ingredientRegistry.getIngredientHelper(VanillaTypes.ITEM).getUniqueId(stack);
    }

    @SuppressWarnings("rawtypes")
    public static boolean matchesSearch(ItemStack stack) {

        if(runtime == null || runtime.getIngredientFilter().getFilterText().isEmpty()) {
            return true;
        }

        if(stack.isEmpty()) {
            return false;
        }

        List<IIngredientListElement> results = runtime.getIngredientFilter().getIngredientList();

        if(results != lastSearchResults) {

            lastSearchResults = results;
            searchMatches.clear();

            for(IIngredientListElement element : results) {

                Object ingredient = element.getIngredient();

                if(ingredient instanceof ItemStack) {
                    searchMatches.add(uid((ItemStack) ingredient));
                }

            }

        }

        return searchMatches.contains(uid(stack));

    }

    public static void setFilterText(String text) {
        if(runtime != null) {
            runtime.getIngredientFilter().setFilterText(text);
        }
    }

    public static List<ItemStack> getFilteredItemStacks() {

        List<ItemStack> items = new ArrayList<>();

        if(runtime == null) {
            return items;
        }

        for(Object ingredient : runtime.getIngredientFilter().getFilteredIngredients()) {
            if(ingredient instanceof ItemStack) {
                items.add(((ItemStack) ingredient).copy());
            }
        }

        return items;

    }

    public static ItemStack getItemUnderMouse() {

        if(runtime == null) {
            return ItemStack.EMPTY;
        }

        Object ingredient = runtime.getIngredientListOverlay().getIngredientUnderMouse();

        if(ingredient instanceof ItemStack) {
            return (ItemStack) ingredient;
        }

        return ItemStack.EMPTY;

    }

    public static void giveSelectedQuantity(ItemStack ingredient) {

        GiveMode mode = Config.getGiveMode();

        ItemStack stack = ingredient.copy();
        stack.setCount(NEIClientConfig.getItemQuantity());

        if(MC.currentScreen instanceof GuiContainerCreative && mode == GiveMode.MOUSE_PICKUP) {
            CommandUtilServer.mousePickupItemStack(MC.player, stack);
        } else if(ServerInfo.isJeiOnServer()) {
            JustEnoughItems.getProxy().sendPacketToServer(new PacketGiveItemStack(stack, mode));
        } else {
            NEIClientUtils.giveStack(stack, stack.getCount());
        }

    }

    public static boolean isOverlayDisplayed() {
        return runtime != null && runtime.getIngredientListOverlay().isListDisplayed();
    }

    public static boolean isSearchBarCentered(GuiContainer gui) {

        if(!isOverlayDisplayed()) {
            return false;
        }

        try {

            if(guiScreenHelperField == null) {
                guiScreenHelperField = IngredientListOverlay.class.getDeclaredField("guiScreenHelper");
                guiScreenHelperField.setAccessible(true);
            }

            GuiScreenHelper guiScreenHelper = (GuiScreenHelper) guiScreenHelperField.get(runtime.getIngredientListOverlay());
            IGuiProperties guiProperties = guiScreenHelper.getGuiProperties(gui);

            if(guiProperties == null) {
                return false;
            }

            if(isSearchBarCenteredMethod == null) {
                isSearchBarCenteredMethod = IngredientListOverlay.class.getDeclaredMethod(
                        "isSearchBarCentered",
                        IGuiProperties.class
                );
                isSearchBarCenteredMethod.setAccessible(true);
            }

            return (Boolean) isSearchBarCenteredMethod.invoke(null, guiProperties);

        } catch(ReflectiveOperationException e) {
            LogHelper.errorOnce(e, "JEISearchCentered", "Unable to access JEI search bar centered state");
            return false;
        }

    }

    public static Rectangle getItemPanelBounds() {

        if(!isOverlayDisplayed()) {
            return null;
        }

        try {

            if(ingredientContents == null) {
                ingredientContents = IngredientListOverlay.class.getDeclaredField("contents");
                ingredientContents.setAccessible(true);
            }

            IngredientGridWithNavigation contents = (IngredientGridWithNavigation) ingredientContents.get(
                    runtime.getIngredientListOverlay()
            );

            Rectangle area = contents.getArea();

            return area.width > 0 ? area : null;

        } catch(ReflectiveOperationException e) {
            LogHelper.errorOnce(e, "JEIItemPanelBounds", "Unable to access JEI Item Panel bounds");
            return null;
        }

    }

    public static GuiTextFieldFilter getTextFieldFilter() {

        if(!isOverlayDisplayed()) {
            return null;
        }

        try {

            if(searchField == null) {
                searchField = IngredientListOverlay.class.getDeclaredField("searchField");
                searchField.setAccessible(true);
            }

            GuiTextFieldFilter field = (GuiTextFieldFilter) searchField.get(runtime.getIngredientListOverlay());

            return field.getVisible() ? field : null;

        } catch(ReflectiveOperationException e) {
            LogHelper.errorOnce(e, "JEISearchField", "Unable to access JEI search field for inventory highlighting");
            return null;
        }

    }

}
