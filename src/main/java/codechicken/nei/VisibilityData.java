package codechicken.nei;

public class VisibilityData {

    public boolean showNEI = true;
    public boolean showWidgets = true;

    public boolean showSubsets = true;
    public boolean showQuantity = true;
    public boolean showUtilityButtons = true;
    public boolean showStateButtons = true;
    public boolean enableDeleteMode = true;

    public void translateDependencies() {

        if(!showNEI) {
            showWidgets = false;
        }

        showSubsets = showSubsets && NEIClientConfig.getBooleanSetting("inventory.showSubsetsWidget");
        showQuantity = showQuantity && NEIClientConfig.getBooleanSetting("inventory.showItemQuantityWidget");
        showStateButtons = showStateButtons && NEIClientConfig.getBooleanSetting("inventory.showSavesWidget");

        if(!showWidgets) {
            showSubsets = false;
            showQuantity = false;
            showUtilityButtons = false;
            showStateButtons = false;
        }

    }

}
