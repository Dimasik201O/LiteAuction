package org.dimasik.liteauction.frontend.menus.market.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfirmItem extends AbstractMenu {
    @Setter
    private boolean forceClose = false;
    private SellItem sellItem;
    private Main back;

    public ConfirmItem(SellItem sellItem, Main back){
        this.sellItem = sellItem;
        this.back = back;
    }

    public ConfirmItem compile(){
        try{
            inventory = Bukkit.createInventory(this, 27, "Покупка предмета");
            if(true){
                ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&5&F&B&0&0 ▶ &x&D&5&D&B&D&CКупить"));
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
                itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2 ▶ &x&D&5&D&B&D&CОтменить покупку"));
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
                ItemStack itemStack = sellItem.decodeItemStack();
                itemStack.setAmount(sellItem.getAmount());
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Категория:&x&0&0&D&8&F&F " + String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(sellItem.getTags()))));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Продавец:&x&0&0&D&8&F&F " + sellItem.getPlayer()));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(sellItem.getPrice() * sellItem.getAmount())));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Цена за 1 ед.:&x&0&0&D&8&F&F " + Formatter.formatPrice(sellItem.getPrice())));
                lore.add(Parser.color(""));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(13, itemStack);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ConfirmItem setPlayer(Player player){
        this.viewer = player;
        return this;
    }
}
