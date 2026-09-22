package codechicken.nei.api;

import codechicken.lib.item.filtering.IItemFilter;
import codechicken.lib.item.filtering.IItemFilterProvider;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.Option;
import codechicken.nei.handler.FastTransferManager;
import codechicken.nei.util.ItemList;
import codechicken.nei.util.ItemStackSet;
import codechicken.nei.widget.SubsetWidget;
import codechicken.nei.widget.SubsetWidget.SubsetTag;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * This is the main class that handles item property configuration.
 * WARNING: DO NOT access this class until the world has been loaded
 * These methods should be called from INEIConfig implementors
 */
public class API {

    public static void registerNEIGuiHandler(INEIGuiHandler handler) {
        GuiInfo.guiHandlers.add(handler);
    }

    public static void addOption(Option option) {
        NEIClientConfig.getOptionList().addOption(option);
    }

    /**
     * Tells NEI not to perform any Fast Transfer operations on slots of a particular class
     *
     * @param slotClass The class of slot to be exempted
     */
    public static void addFastTransferExemptSlot(Class<? extends Slot> slotClass) {
        FastTransferManager.fastTransferExemptions.add(slotClass);
    }

    /**
     * Tells NEI not to perform any Fast Transfer operations on a GuiContainer of a specific class.
     *
     * @param guiClass The class of the container to be exempted
     */
    public static void addFastTransferExemptContainer(Class<? extends GuiContainer> guiClass) {
        FastTransferManager.fastTransferContainerExemptions.add(guiClass);
    }

    /**
     * Register a filter provider for the item panel.
     *
     * @param filterProvider The filter provider to be registered.
     */
    public static void addItemFilter(IItemFilterProvider filterProvider) {
        ItemList.registerIItemFilterProvider(filterProvider);
    }

    /**
     * Adds a new tag to the item subset dropdown.
     *
     * @param name   The fully qualified name, Eg Blocks.MobSpawners. NOT case sensitive
     * @param filter A filter for matching items that fit in this subset
     */
    public static void addSubset(String name, IItemFilter filter) {
        addSubset(new SubsetTag(name, filter));
    }

    /**
     * Adds a new tag to the item subset dropdown.
     *
     * @param name  The fully qualified name, Eg Blocks.MobSpawners. NOT case sensitive
     * @param items An iterable of itemstacks to be added as a subset
     */
    public static void addSubset(String name, Iterable<ItemStack> items) {
        ItemStackSet filter = new ItemStackSet();
        for (ItemStack item : items) {
            filter.add(item);
        }
        addSubset(new SubsetTag(name, filter));
    }

    /**
     * Adds a new tag to the item subset dropdown.
     */
    public static void addSubset(SubsetTag tag) {
        SubsetWidget.addTag(tag);
    }

}
