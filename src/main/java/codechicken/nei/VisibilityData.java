package codechicken.nei;

public class VisibilityData {

    public boolean showUtilityButtons = true;
    public boolean showStateButtons = true;
    public boolean showSubsets = true;
    public boolean showQuantity = true;
    public boolean showWidgets = true;
    public boolean showNEI = true;
    public boolean enableDeleteMode = true;

    public void translateDependencies() {

        if(!showNEI) {
            showWidgets = false;
        }

        if(!showWidgets) {
            showSubsets = false;
            showQuantity = false;
            showUtilityButtons = false;
            showStateButtons = false;
        }

    }

}
