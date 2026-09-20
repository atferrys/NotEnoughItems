package codechicken.nei;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemMobSpawner {

    public static List<ItemStack> getSpawnerVariants() {

        List<EntityEntry> mobs = new ArrayList<>();

        for(EntityEntry entry : ForgeRegistries.ENTITIES) {
            if(entry.getEntityClass() != null && EntityLiving.class.isAssignableFrom(entry.getEntityClass())) {
                mobs.add(entry);
            }
        }

        mobs.sort(Comparator.comparing(entry -> entry.getRegistryName().toString()));

        List<ItemStack> stacks = new ArrayList<>();

        for(EntityEntry entry : mobs) {

            ItemStack stack = new ItemStack(Blocks.MOB_SPAWNER);
            NBTTagCompound blockEntityTag = stack.getOrCreateSubCompound("BlockEntityTag");

            NBTTagCompound spawnData = new NBTTagCompound();
            spawnData.setString("id", entry.getRegistryName().toString());
            blockEntityTag.setTag("SpawnData", spawnData);

            // ItemBlock merges this with the placed tile's defaults, which include pigs in the SpawnPotentials, so the
            // desired mob would initially spawn but then be replaced by the one from SpawnPotentials.
            // https://minecraft.wiki/w/Monster_Spawner#Block_data
            NBTTagList spawnPotentials = new NBTTagList();
            NBTTagCompound potential = new NBTTagCompound();
            potential.setInteger("Weight", 1);
            potential.setTag("Entity", spawnData.copy());
            spawnPotentials.appendTag(potential);
            blockEntityTag.setTag("SpawnPotentials", spawnPotentials);

            stacks.add(stack);

        }

        return stacks;

    }

    public static NBTTagCompound getSpawnData(ItemStack stack) {

        NBTTagCompound blockEntityTag = stack.getSubCompound("BlockEntityTag");

        if(blockEntityTag != null && blockEntityTag.hasKey("SpawnData", 10)) {
            return blockEntityTag.getCompoundTag("SpawnData").copy();
        }

        NBTTagCompound data = new NBTTagCompound();
        data.setString("id", "minecraft:pig");

        return data;

    }

    public static void addTooltip(ItemStack stack, List<String> tooltip) {

        String entityID = getSpawnData(stack).getString("id");

        if(entityID.isEmpty()) {
            return;
        }

        EntityEntry entity;

        try {
            entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityID));
        } catch (IllegalArgumentException e) {
            return;
        }

        if(entity == null) {
            return;
        }

        String nameKey = "entity." + entity.getName() + ".name";
        String name = I18n.hasKey(nameKey) ? I18n.format(nameKey) : entityID;

        boolean isHostile = entity.getEntityClass() != null && IMob.class.isAssignableFrom(entity.getEntityClass());
        TextFormatting color = isHostile ? TextFormatting.DARK_RED : TextFormatting.DARK_AQUA;

        tooltip.add(color + name);

    }

}
