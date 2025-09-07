package org.dimasik.liteauction.frontend.menus.market.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.Formatter;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CountBuyItem extends AbstractMenu {
    @Setter
    private boolean forceClose = false;
    private int count;
    private SellItem sellItem;
    private Main back;

    public CountBuyItem(SellItem sellItem, Main back, int count){
        this.sellItem = sellItem;
        this.back = back;
        this.count = count;
    }

    public CountBuyItem compile(){
        try{
            inventory = Bukkit.createInventory(this, InventoryType.HOPPER, "Покупка предмета");
            if(true){
                ItemStack itemStack = new ItemStack(Material.RED_CONCRETE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ Уменьшить на 10 единиц"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(0, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.RED_CONCRETE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2▶ Уменьшить на 1 единиц"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(1, itemStack);
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
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(sellItem.getPrice() * count)));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Цена за 1 ед.:&x&0&0&D&8&F&F " + Formatter.formatPrice(sellItem.getPrice())));
                lore.add(Parser.color(""));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                itemStack.setAmount(count);
                inventory.setItem(2, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.LIME_CONCRETE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&5&F&B&0&0▶ Увеличить на 1 единиц"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(3, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.LIME_CONCRETE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&5&F&B&0&0▶ Увеличить на 10 единиц"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(4, itemStack);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public CountBuyItem setPlayer(Player player){
        this.viewer = player;
        return this;
    }
}
