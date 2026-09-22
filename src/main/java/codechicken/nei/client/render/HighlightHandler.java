package codechicken.nei.client.render;

import codechicken.nei.api.IHighlightHandler;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class HighlightHandler {

    public enum Layout {

        HEADER,
        BODY,
        FOOTER;

        public static final Layout[] VALUES = values();

    }

    public static final ArrayListMultimap<Layout, IHighlightHandler> highlightHandlers = ArrayListMultimap.create();
    public static final ArrayListMultimap<Block, IHighlightHandler> highlightIdentifiers = ArrayListMultimap.create();

    static {
        registerHighlightHandler(new DefaultHighlightHandler(), Layout.HEADER);
    }

    public static List<ItemStack> getIdentifierItems(World world, EntityPlayer player, RayTraceResult hit) {

        List<ItemStack> items = new ArrayList<>();

        if(hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return items;
        }

        IBlockState state = world.getBlockState(hit.getBlockPos());
        Block block = state.getBlock();

        List<IHighlightHandler> handlers = new ArrayList<>(highlightIdentifiers.get(null));
        handlers.addAll(highlightIdentifiers.get(block));

        for(IHighlightHandler handler : handlers) {
            ItemStack stack = handler.identifyHighlight(world, player, hit);
            if(stack != null && !stack.isEmpty()) {
                items.add(stack);
            }
        }

        if(!items.isEmpty()) {
            return items;
        }

        try {

            ItemStack stack = block.getPickBlock(state, hit, world, hit.getBlockPos(), player);

            if(!stack.isEmpty()) {
                items.add(stack);
            }

        } catch(Exception ignored) {}

        if(items.isEmpty()) {

            try {

                NonNullList<ItemStack> drops = NonNullList.create();
                block.getDrops(drops, world, hit.getBlockPos(), state, 0);

                for(ItemStack stack : drops) {
                    if(!stack.isEmpty()) {
                        items.add(stack);
                    }
                }

            } catch(Exception ignored) {}

        }

        if(items.isEmpty()) {

            ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));

            if(!stack.isEmpty()) {
                items.add(stack);
            }

        }

        return items;

    }

    public static void registerHighlightHandler(IHighlightHandler handler, Layout... layouts) {
        for(Layout layout : layouts) {
            highlightHandlers.put(layout, handler);
        }
    }

    public static List<String> getText(ItemStack stack, World world, EntityPlayer player, RayTraceResult hit) {

        List<String> tooltip = new ArrayList<>();

        for(Layout layout : Layout.VALUES) {
            for(IHighlightHandler handler : highlightHandlers.get(layout)) {
                tooltip = handler.handleTextData(stack, world, player, hit, tooltip, layout);
            }
        }

        return tooltip;

    }

}
