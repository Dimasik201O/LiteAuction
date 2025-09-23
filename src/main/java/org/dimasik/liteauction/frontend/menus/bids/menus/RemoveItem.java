package org.dimasik.liteauction.frontend.menus.bids.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RemoveItem extends AbstractMenu {
    @Setter
    private boolean forceClose = false;
    private BidItem bidItem;
    private Main back;

    public RemoveItem(BidItem bidItem, Main back){
        this.bidItem = bidItem;
        this.back = back;
    }

    public RemoveItem compile(){
        try{
            inventory = ConfigUtils.buildInventory(this, "design/menus/bids/remove_item.yml", "inventory-type",
                    ConfigManager.getString("design/menus/bids/remove_item.yml", "gui-title", "&x&0&0&D&8&F&F Снять предмет с продажи")
            );
            if(true){
                ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&5&F&B&0&0▶ &x&D&5&D&B&D&CСнять предмет с продажи"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(0, itemStack);
                inventory.setItem(1, itemStack);
                inventory.setItem(2, itemStack);
                inventory.setItem(9, itemStack);
                inventory.setItem(10, itemStack);
                inventory.setItem(11, itemStack);
                inventory.setItem(18, itemStack);
                inventory.setItem(19, itemStack);
                inventory.setItem(20, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color(" "));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(3, itemStack);
                inventory.setItem(4, itemStack);
                inventory.setItem(5, itemStack);
                inventory.setItem(12, itemStack);
                inventory.setItem(14, itemStack);
                inventory.setItem(21, itemStack);
                inventory.setItem(22, itemStack);
                inventory.setItem(23, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CОтменить"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(6, itemStack);
                inventory.setItem(7, itemStack);
                inventory.setItem(8, itemStack);
                inventory.setItem(15, itemStack);
                inventory.setItem(16, itemStack);
                inventory.setItem(17, itemStack);
                inventory.setItem(24, itemStack);
                inventory.setItem(25, itemStack);
                inventory.setItem(26, itemStack);
            }
            if(true){
                ItemStack itemStack = bidItem.decodeItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.addAll(ConfigManager.getStringList("design/menus/bids/remove_item.yml", "item.lore").stream().map(s -> Parser.color(s)).toList());
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(13, itemStack);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
