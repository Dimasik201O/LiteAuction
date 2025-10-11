package org.dimasik.liteauction.backend.utils.nms;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VersionUtil {

    public static Class<?> getCraftItemStackClass() throws ClassNotFoundException {
        try {
            String packageName = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
            String version = packageName.split("\\.")[3];
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (ClassNotFoundException e) {
            return Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
        }
    }
}