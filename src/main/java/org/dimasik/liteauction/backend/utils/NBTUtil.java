package org.dimasik.liteauction.backend.utils;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTUtil {
    public static String getNBTAsString(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return "";
        }

        try {
            net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

            NBTTagCompound tag = nmsItem.getTag();

            if (tag == null) {
                return "";
            }

            return tag.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}