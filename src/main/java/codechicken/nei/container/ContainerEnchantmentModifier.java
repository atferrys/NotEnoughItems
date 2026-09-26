package codechicken.nei.container;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ContainerEnchantmentModifier extends ContainerEnchantment {

    public ContainerEnchantmentModifier(InventoryPlayer inventoryplayer, World world) {
        super(inventoryplayer, world, new BlockPos(0, 0, 0));
    }

    public void addEnchantment(String enchantmentLocation, int level) {

        Enchantment enchantment = Enchantment.getEnchantmentByLocation(enchantmentLocation);

        if(enchantment != null && !getSlot(0).getStack().isEmpty() && level > 0) {
            inventorySlots.get(0).getStack().addEnchantment(enchantment, level);
        }

    }

    public void removeEnchantment(String enchantmentLocation) {

        Enchantment enchantment = Enchantment.getEnchantmentByLocation(enchantmentLocation);

        if(enchantment == null) {
            return;
        }

        ItemStack stack = inventorySlots.get(0).getStack();

        if(stack.isEmpty()) {
            return;
        }

        // 1.12 still stored numeric IDs in the item NBT
        int enchantmentId = Enchantment.getEnchantmentID(enchantment);
        NBTTagList enchantmentList = stack.getEnchantmentTagList();

        for(int i = 0; i < enchantmentList.tagCount(); i++) {

            if(enchantmentList.getCompoundTagAt(i).getShort("id") != enchantmentId) {
                continue;
            }

            enchantmentList.removeTag(i);

            NBTTagCompound stackCompound = stack.getTagCompound();

            if(stackCompound == null) {
                break;
            }

            if(enchantmentList.tagCount() == 0) {
                stackCompound.removeTag("ench");
            }

            if(stackCompound.isEmpty()) {
                stack.setTagCompound(null);
            }

            break;

        }

    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer entityPlayer) {
        return true;
    }

}
