package org.dimasik.liteauction.frontend.menus.market.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.mysql.models.UnsoldItem;
import org.dimasik.liteauction.backend.utils.Formatter;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class Unsold extends AbstractMenu {
    @Setter
    private boolean forceClose = false;
    @Setter
    private int page;
    private AbstractMenu back;
    private HashMap<Integer, UnsoldItem> items = new HashMap<>();

    public Unsold(int page, AbstractMenu back){
        this.page = page;
        this.back = back;
    }

    public Unsold compile(){
        try{
            items.clear();
            int slot = 0;
            List<UnsoldItem> items = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItems(viewer.getName()).get();
            int startIndex = 45 * (page - 1);
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            inventory = Bukkit.createInventory(this, 54, Parser.color("Просроченные товары"));
            for(int i = startIndex; i < items.size() && slot < 45; i++) {
                UnsoldItem unsoldItem = items.get(i);
                this.items.put(slot, unsoldItem);
                ItemStack itemStack = unsoldItem.decodeItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Категория:&x&0&0&D&8&F&F " + String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(unsoldItem.getTags()))));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Удалится через:&x&0&0&D&8&F&F " + Formatter.getTimeUntilDeletion(unsoldItem)));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(unsoldItem.getPrice() * unsoldItem.getAmount())));
                if(unsoldItem.isByOne()){
                    lore.add(Parser.color(""));
                    lore.add(Parser.color(" &x&0&0&D&8&F&F● &x&D&5&D&B&D&CДанный товар можно"));
                    lore.add(Parser.color(" &0.&x&D&5&D&B&D&C  купить &x&0&0&D&8&F&Fтолько полностью&x&D&5&D&B&D&C."));
                }
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы забрать предмет"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                itemStack.setAmount(unsoldItem.getAmount());
                inventory.setItem(slot, itemStack);
                slot++;
            }


            if(true){
                ItemStack itemStack = new ItemStack(Material.ARROW);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(45, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F◀ Предыдущая страница"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(48, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.LIME_DYE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&6Следующая страница ▶"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(50, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.BOOK);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Помощь по аукциону"));
                List<String> lore = new ArrayList<>();
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &f&m                                          &f "));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&9&C&F&9&F&F       Как снять с продажи?"));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&D&5&D&B&D&C Чтобы забрать предмет,"));
                lore.add(Parser.color(" &x&D&5&D&B&D&C нажмите по иконке товара."));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &f&m                                          &f "));
                lore.add(Parser.color(""));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(53, itemStack);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Unsold setPlayer(Player player){
        this.viewer = player;
        return this;
    }
}
