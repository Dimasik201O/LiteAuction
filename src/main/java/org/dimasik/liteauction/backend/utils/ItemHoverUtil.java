package org.dimasik.liteauction.backend.utils;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.dimasik.liteauction.backend.utils.ItemNameUtil.getLocalizedItemName;

public class ItemHoverUtil {
    public static void sendHoverItemMessage(Player player, String message, ItemStack hoverItem) {
        player.sendMessage(message.replace("%item%", getItemDisplayName(hoverItem)));
    }

    public static String getHoverItemMessage(String message, ItemStack hoverItem) {
        return message.replace("%item%", getItemDisplayName(hoverItem));
    }

    private static String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return "[" + getLocalizedItemName(item.getType()) + "]";
    }
}