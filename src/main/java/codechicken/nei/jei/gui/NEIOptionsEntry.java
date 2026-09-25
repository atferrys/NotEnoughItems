package codechicken.nei.jei.gui;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.config.GuiOptionList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

public class NEIOptionsEntry extends GuiConfigEntries.CategoryEntry {

    public NEIOptionsEntry(GuiConfig screen, GuiConfigEntries entries, IConfigElement element) {
        super(screen, entries, element);
    }

    @Override
    protected GuiScreen buildChildScreen() {
        return new GuiOptionList(owningScreen, NEIClientConfig.getOptionList(), false);
    }

}
