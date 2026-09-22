package codechicken.nei.api;

import codechicken.nei.client.render.HighlightHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public interface IHighlightHandler {

    ItemStack identifyHighlight(World world, EntityPlayer player, RayTraceResult hit);

    List<String> handleTextData(ItemStack stack, World world, EntityPlayer player, RayTraceResult hit, List<String> tooltip, HighlightHandler.Layout layout);

}
