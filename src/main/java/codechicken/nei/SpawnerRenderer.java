package codechicken.nei;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.state.GlStateTracker;
import codechicken.lib.util.ClientUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.nei.util.LogHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class SpawnerRenderer implements IItemRenderer {

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final ModelResourceLocation SPAWNER_MODEL = new ModelResourceLocation("minecraft:mob_spawner", "inventory");

    private final Map<String, Entity> entities = new HashMap<>();
    private IBakedModel baseModel;
    private World cachedWorld;

    public static void register() {

        SpawnerRenderer renderer = new SpawnerRenderer();

        ModelRegistryHelper.registerPreBakeCallback(registry -> {
            renderer.baseModel = registry.getObject(SPAWNER_MODEL);
            renderer.entities.clear();
        });
        ModelRegistryHelper.registerItemRenderer(Item.getItemFromBlock(Blocks.MOB_SPAWNER), renderer);

        MinecraftForge.EVENT_BUS.register(renderer);

    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if(event.getWorld() == cachedWorld) {
            entities.clear();
            cachedWorld = null;
        }
    }

    private Entity getEntity(NBTTagCompound data, World world) {

        if(cachedWorld != world) {
            entities.clear();
            cachedWorld = world;
        }

        String entityID = data.getString("id");

        if(entityID.isEmpty()) {
            return null;
        }

        if(!entities.containsKey(entityID)) {

            Entity entity = null;

            try {
                entity = EntityList.createEntityFromNBT(data.copy(), world);
            } catch (Exception e) {
                LogHelper.warn("Unable to create spawner entity for {}", data, e);
            }

            // Also store the entity if it's null, as it's unlikely that trying again would work anyway
            entities.put(entityID, entity);

        }

        return entities.get(entityID);

    }

    @Override
    public void renderItem(ItemStack stack, TransformType transformType) {

        // Render the base empty cage
        if(baseModel != null) {
            GlStateManager.pushMatrix();
            try {
                GlStateManager.translate(0.5F, 0.5F, 0.5F);
                MC.getRenderItem().renderItem(stack, baseModel);
            } finally {
                GlStateManager.popMatrix();
            }
        }

        if(MC.world == null) {
            return;
        }

        NBTTagCompound data = ItemMobSpawner.getSpawnData(stack);
        Entity entity = this.getEntity(data, MC.world);

        if(entity == null) {
            return;
        }

        RenderManager renderManager = MC.getRenderManager();

        // Save current GL states to reset after the entity has been rendered
        boolean renderShadow = renderManager.isRenderShadow();

        float lightmapX = OpenGlHelper.lastBrightnessX;
        float lightmapY = OpenGlHelper.lastBrightnessY;

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        boolean lightmapEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        int matrixDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);

        GlStateTracker.pushState();
        GlStateManager.pushMatrix();

        try {

            // Try render the spinning entity
            float scale = 0.53125F / Math.max(1.0F, Math.max(entity.height, entity.width));
            GlStateManager.translate(0.5F, 0.4F, 0.5F);
            GlStateManager.rotate((float) (ClientUtils.getRenderTime() * 10 % 360), 0, 1, 0);
            GlStateManager.translate(0, -0.2F, 0);
            GlStateManager.rotate(-30, 1, 0, 0);
            GlStateManager.scale(scale, scale, scale);

            entity.setLocationAndAngles(0, 0, 0, 0, 0);

            renderManager.setRenderShadow(false);
            renderManager.renderEntity(entity, 0, 0, 0, 0, 0, true);

        } catch(Exception e) {

            entities.put(data.getString("id"), null); // Prevent rendering this entity from now on
            LogHelper.warn("Unable to render spawner entity for {}", data, e);

            if(Tessellator.getInstance().getBuffer().isDrawing) {
                Tessellator.getInstance().getBuffer().finishDrawing();
            }

        } finally {

            renderManager.setRenderShadow(renderShadow);

            // Remove unpopped states that modded entities might leak
            while(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) > matrixDepth) {
                GlStateManager.popMatrix();
            }

            GlStateTracker.popState();

            // Reset the GL states that have been saved earlier
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);

            if(lightmapEnabled) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }

            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.color(1, 1, 1, 1);

            MC.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        }

    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

}
