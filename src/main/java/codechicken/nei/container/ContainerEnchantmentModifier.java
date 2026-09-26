package codechicken.nei.container;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerEnchantmentModifier extends ContainerEnchantment {

    public ContainerEnchantmentModifier(InventoryPlayer inventoryplayer, World world) {
        super(inventoryplayer, world, new BlockPos(0, 0, 0));
    }

    @Deprecated
    public boolean addEnchantment(int e, int level) {
        return addEnchantment(Enchantment.REGISTRY.getNameForObject(Enchantment.getEnchantmentByID(e)).toString(), level);
    }

    public boolean addEnchantment(String enchantmentLocation, int level) {
        Enchantment enchantment = Enchantment.getEnchantmentByLocation(enchantmentLocation);
        if (enchantment != null) {
            inventorySlots.get(0).getStack().addEnchantment(enchantment, level);
            return true;
        }
        return false;
    }

    @Deprecated
    //TODO String variant.
    public void removeEnchantment(int e) {
        ItemStack stack = inventorySlots.get(0).getStack();
        NBTTagList nbttaglist = stack.getEnchantmentTagList();
        if (nbttaglist != null) {
            for (int i = 0; i < nbttaglist.tagCount(); i++) {
                int ID = nbttaglist.getCompoundTagAt(i).getShort("id");
                if (ID == e) {
                    nbttaglist.removeTag(i);
                    if (nbttaglist.tagCount() == 0) {
                        stack.getTagCompound().removeTag("ench");
                    }
                    if (stack.getTagCompound().isEmpty()) {
                        stack.setTagCompound(null);
                    }
                    return;
                }
            }
        }
    }

    public boolean canInteractWith(EntityPlayer entityplayer) {
        return true;
    }

}
