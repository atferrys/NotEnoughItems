package codechicken.nei.jei;

import codechicken.nei.ItemMobSpawner;
import codechicken.nei.jei.gui.NEIGuiIntegrationHandler;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

@JEIPlugin
public class NEIJeiPlugin implements IModPlugin {

    private IIngredientRegistry ingredients;
    private IJeiHelpers helpers;

    @Override
    public void registerItemSubtypes(ISubtypeRegistry registry) {
        if(!registry.hasSubtypeInterpreter(new ItemStack(Blocks.MOB_SPAWNER))) {
            registry.registerSubtypeInterpreter(
                    Item.getItemFromBlock(Blocks.MOB_SPAWNER),
                    stack -> ItemMobSpawner.getSpawnData(stack).toString()
            );
        }
    }

    @Override
    public void register(IModRegistry registry) {

        ingredients = registry.getIngredientRegistry();
        helpers = registry.getJeiHelpers();

        registry.addGlobalGuiHandlers(new NEIGuiIntegrationHandler());

    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime runtime) {
        ingredients.addIngredientsAtRuntime(VanillaTypes.ITEM, ItemMobSpawner.getSpawnerVariants());
        JEIIntegrationManager.onRuntimeAvailable(runtime, ingredients, helpers.getIngredientBlacklist());
    }

}
