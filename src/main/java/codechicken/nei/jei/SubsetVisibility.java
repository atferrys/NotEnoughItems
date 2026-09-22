package codechicken.nei.jei;

import codechicken.lib.item.filtering.IItemFilter;
import codechicken.nei.util.ItemList;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.runtime.JeiRuntime;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubsetVisibility {

    private final IIngredientRegistry registry;
    private final IIngredientBlacklist blacklist;
    private final IngredientFilter ingredientFilter;
    private final IngredientListOverlay overlay;
    private final Map<String, Object> hiddenByNEI = new HashMap<>();

    private boolean dirty = true;
    private boolean enabled;

    public SubsetVisibility(JeiRuntime runtime, IIngredientRegistry registry, IIngredientBlacklist blacklist) {
        this.registry = registry;
        this.blacklist = blacklist;
        this.ingredientFilter = runtime.getIngredientFilter();
        this.overlay = runtime.getIngredientListOverlay();
    }

    public void invalidate() {
        dirty = true;
    }

    public List<ItemStack> getSubsetItems() {

        Map<String, ItemStack> items = new LinkedHashMap<>();

        for(Object ingredient : getIngredients()) {

            // Exclude items already hidden by JEI (with stuff like CraftTweaker or the API), but keep the items hidden
            // by filters with the subsets available for unhiding
            if(blacklist.isIngredientBlacklisted(ingredient) && !hiddenByNEI.containsKey(uid(ingredient))) {
                continue;
            }

            ItemStack stack = getItemStack(ingredient);

            if(!stack.isEmpty()) {
                String itemUid = registry.getIngredientHelper(VanillaTypes.ITEM).getUniqueId(stack);
                items.putIfAbsent(itemUid, stack);
            }

        }

        return new ArrayList<>(items.values());

    }

    public void update(boolean enabled) {

        if(!dirty && this.enabled == enabled) {
            return;
        }

        dirty = false;
        this.enabled = enabled;

        IItemFilter filter = ItemList.getItemListFilter();
        boolean hasVisibleItems = getSubsetItems().stream().anyMatch(filter::matches);
        boolean changed = false;

        Iterator<Object> hidden = hiddenByNEI.values().iterator();

        while(hidden.hasNext()) {
            Object ingredient = hidden.next();
            if(!enabled || matchesFilter(ingredient, filter, hasVisibleItems)) {
                blacklist.removeIngredientFromBlacklist(ingredient);
                hidden.remove();
                changed = true;
            }
        }

        if(enabled) {
            for(Object ingredient : getIngredients()) {
                if(!matchesFilter(ingredient, filter, hasVisibleItems) && !blacklist.isIngredientBlacklisted(ingredient)) {
                    blacklist.addIngredientToBlacklist(ingredient);
                    hiddenByNEI.put(uid(ingredient), ingredient);
                    changed = true;
                }
            }
        }

        if(changed) {
            ingredientFilter.updateHidden();
            ingredientFilter.invalidateCache();
            overlay.updateLayout(true);
        }

    }

    private List<Object> getIngredients() {

        List<Object> ingredients = new ArrayList<>();

        for(IIngredientType<?> type : registry.getRegisteredIngredientTypes()) {
            ingredients.addAll(registry.getAllIngredients(type));
        }

        return ingredients;

    }

    private ItemStack getItemStack(Object ingredient) {

        if(ingredient instanceof ItemStack) {
            return (ItemStack) ingredient;
        }

        // For EnchantmentData and FluidStacks
        return registry.getIngredientHelper(ingredient).getCheatItemStack(ingredient);

    }

    private boolean matchesFilter(Object ingredient, IItemFilter filter, boolean hasVisibleItems) {

        ItemStack stack = getItemStack(ingredient);

        // Ingredients without an item representation cannot be selected individually,
        // so we still exclude them if one at least matches
        return stack.isEmpty() ? hasVisibleItems : filter.matches(stack);

    }

    private String uid(Object ingredient) {
        return registry.getIngredientHelper(ingredient).getUniqueId(ingredient);
    }

}
