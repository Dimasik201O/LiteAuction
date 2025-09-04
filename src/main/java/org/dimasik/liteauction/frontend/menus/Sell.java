package org.dimasik.liteauction.frontend.menus;

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
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.redis.UpdateData;
import org.dimasik.liteauction.backend.utils.Formatter;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Sell implements InventoryHolder {
    @Getter
    @Setter
    private boolean forceClose;
    @Setter
    @Getter
    private int page;
    @Getter
    private Main back;
    @Getter
    private HashMap<Integer, SellItem> items = new HashMap<>();
    @Getter
    private Player viewer;
    private Inventory inventory;

    public Sell(int page, Main back){
        this.page = page;
        this.back = back;
    }

    public Sell compile(){
        try{
            items.clear();
            int slot = 0;
            List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItems(viewer.getName()).get();
            int startIndex = 45 * (page - 1);
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            inventory = Bukkit.createInventory(this, 54, Parser.color("Товары на продаже"));
            for(int i = startIndex; i < items.size() && slot < 45; i++) {
                SellItem sellItem = items.get(i);
                this.items.put(slot, sellItem);
                LiteAuction.getItems().put(new UpdateData(inventory, slot), sellItem.getId());
                ItemStack itemStack = sellItem.decodeItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Категория:&x&0&0&D&8&F&F " + String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(sellItem.getTags()))));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Истекает через:&x&0&0&D&8&F&F " + Formatter.getTimeUntilExpiration(sellItem)));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(sellItem.getPrice() * sellItem.getAmount())));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Цена за 1 ед.:&x&0&0&D&8&F&F " + Formatter.formatPrice(sellItem.getPrice())));
                if(sellItem.isByOne()){
                    lore.add(Parser.color(""));
                    lore.add(Parser.color(" &x&0&0&D&8&F&F● &x&D&5&D&B&D&CДанный товар можно"));
                    lore.add(Parser.color(" &0.&x&D&5&D&B&D&C  купить &x&0&0&D&8&F&Fтолько полностью&x&D&5&D&B&D&C."));
                }
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы снять с продажи"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                itemStack.setAmount(sellItem.getAmount());
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
                lore.add(Parser.color(" &x&D&5&D&B&D&C Чтобы снять товар с продажи,"));
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

    public Sell setPlayer(Player player){
        this.viewer = player;
        return this;
    }

    public void open(){
        viewer.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
