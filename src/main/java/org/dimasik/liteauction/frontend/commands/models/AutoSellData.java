package org.dimasik.liteauction.frontend.commands.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
@AllArgsConstructor
public class AutoSellData {
    public int price;
    public ItemStack itemStack;
    public boolean full;
}
