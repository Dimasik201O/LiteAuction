package org.dimasik.liteauction.backend.utils.nms;

import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

@UtilityClass
public class ContainerUtil {
    // переписано без NMS
    public static Inventory getActiveContainer(Player player) {
        if (player == null) return null;

        return player.getOpenInventory().getTopInventory();
    }

    // фунция не нужна т.к. инвентарь не будет instanceOf AbstractMenu
}
