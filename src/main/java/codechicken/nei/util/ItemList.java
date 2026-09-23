package codechicken.nei.util;

import codechicken.lib.item.filtering.IItemFilter;
import codechicken.lib.item.filtering.IItemFilterProvider;
import codechicken.nei.jei.JEIIntegrationManager;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemList {

    public static volatile List<ItemStack> items = Collections.emptyList();
    private static final List<IItemFilterProvider> itemFilterProviders = new ArrayList<>();
    private static final List<ItemsLoadedCallback> loadCallbacks = new ArrayList<>();

    public static void registerIItemFilterProvider(IItemFilterProvider provider) {

        synchronized(itemFilterProviders) {
            itemFilterProviders.add(provider);
        }

        JEIIntegrationManager.refreshItemVisibility();

    }

    public static void registerLoadCallback(ItemsLoadedCallback callback) {
        synchronized(loadCallbacks) {
            loadCallbacks.add(callback);
        }
    }

    public static class NothingItemFilter implements IItemFilter {

        @Override
        public boolean matches(@Nonnull ItemStack item) {
            return false;
        }

    }

    public interface ItemsLoadedCallback {
        void itemsLoaded();
    }

    public static IItemFilter getItemListFilter() {

        List<IItemFilter> filters = new ArrayList<>();

        synchronized(itemFilterProviders) {
            for(IItemFilterProvider provider : itemFilterProviders) {
                filters.add(provider.getFilter());
            }
        }

        return item -> {

            for(IItemFilter filter : filters) {
                try {
                    if(!filter.matches(item)) {
                        return false;
                    }
                } catch(Exception e) {
                    LogHelper.errorOnce(e, filter.toString(), "Exception filtering %s", item);
                }
            }

            return true;

        };

    }

    public static void refresh() {

        List<ItemStack> registered = JEIIntegrationManager.getSubsetItems();

        if(sameItems(items, registered)) {
            return;
        }

        items = Collections.unmodifiableList(registered);

        synchronized(loadCallbacks) {
            for(ItemsLoadedCallback callback : loadCallbacks) {
                callback.itemsLoaded();
            }
        }

        JEIIntegrationManager.refreshItemVisibility();

    }

    private static boolean sameItems(List<ItemStack> previous, List<ItemStack> current) {

        if(previous.size() != current.size()) {
            return false;
        }

        for(int i = 0; i < previous.size(); i++) {
            if(!ItemStack.areItemStacksEqual(previous.get(i), current.get(i))) {
                return false;
            }
        }

        return true;

    }

}
