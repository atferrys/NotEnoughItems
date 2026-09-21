package codechicken.nei;

import codechicken.lib.CodeChickenLib;
import codechicken.lib.internal.ModDescriptionEnhancer;
import codechicken.nei.proxy.Proxy;
import mezz.jei.config.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static codechicken.lib.CodeChickenLib.MC_VERSION_DEP;
import static codechicken.nei.NotEnoughItems.*;
import static codechicken.nei.Tags.*;

/**
 * Created by covers1624 on 29/03/2017.
 */
@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION, dependencies = DEPENDENCIES, acceptedMinecraftVersions = MC_VERSION_DEP)
public class NotEnoughItems {

    public static final String MOD_VERSION_DEP = "required-after:nei@[" + VERSION + ",);";
    public static final String DEPENDENCIES = CodeChickenLib.MOD_VERSION_DEP + ";required-after:jei@[" + Constants.VERSION + ",);required-after:forge@[14.23.5.2768,)";

    @SidedProxy (clientSide = "codechicken.nei.proxy.ProxyClient", serverSide = "codechicken.nei.proxy.Proxy")
    public static Proxy proxy;

    @Instance
    public static NotEnoughItems instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        ModDescriptionEnhancer.registerEnhancement(MOD_ID, "NotEnoughItems");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }
}
