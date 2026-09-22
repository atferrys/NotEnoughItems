package codechicken.nei.client.render;

import codechicken.nei.api.IHighlightHandler;
import codechicken.nei.util.helper.GuiHelper;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public class DefaultHighlightHandler implements IHighlightHandler {

    @Override
    public ItemStack identifyHighlight(World world, EntityPlayer player, RayTraceResult hit) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<String> handleTextData(ItemStack stack, World world, EntityPlayer player, RayTraceResult hit, List<String> tooltip, HighlightHandler.Layout layout) {

        if(stack.isEmpty()) {
            return tooltip;
        }

        String name = GuiHelper.itemDisplayNameShort(stack);
        IBlockState state = world.getBlockState(hit.getBlockPos());

        if(state.getBlock() instanceof BlockRedstoneWire) {
            name += " " + state.getValue(BlockRedstoneWire.POWER);
        }

        tooltip.add(name);

        return tooltip;

    }
}
