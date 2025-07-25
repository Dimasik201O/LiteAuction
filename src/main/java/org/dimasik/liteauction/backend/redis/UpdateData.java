package org.dimasik.liteauction.backend.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.Inventory;

@Data
@AllArgsConstructor
public class UpdateData {
    private Inventory inventory;
    private int slot;
}
