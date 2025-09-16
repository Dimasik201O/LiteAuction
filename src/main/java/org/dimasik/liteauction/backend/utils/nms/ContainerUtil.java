package org.dimasik.liteauction.backend.utils.nms;

import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@UtilityClass
public class ContainerUtil {
    public static Inventory getActiveContainer(Player player) {
        if (player == null) return null;

        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        if (entityPlayer.activeContainer == null) {
            return null;
        }

        return entityPlayer.activeContainer.getBukkitView().getTopInventory();
    }

    public static boolean hasActiveContainer(Player player) {
        if (player == null) return false;

        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        return entityPlayer.activeContainer != null &&
                entityPlayer.activeContainer != entityPlayer.defaultContainer;
    }
}
