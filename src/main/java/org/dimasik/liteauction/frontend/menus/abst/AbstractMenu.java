package org.dimasik.liteauction.frontend.menus.abst;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.dimasik.liteauction.LiteAuction;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMenu implements InventoryHolder {
    @Getter
    protected Player viewer;
    protected Inventory inventory;

    public AbstractMenu setPlayer(Player player){
        this.viewer = player;
        return this;
    }

    public void open(){
        if(viewer != null) {
            if (inventory != null) {
                Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                    viewer.openInventory(inventory);
                });
            }
        }
    }

    public abstract AbstractMenu compile();

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void close(){
        if(viewer != null){
            Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                viewer.closeInventory();
            });
        }
    }
}
