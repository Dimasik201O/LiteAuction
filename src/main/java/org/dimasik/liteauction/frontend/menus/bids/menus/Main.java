package org.dimasik.liteauction.frontend.menus.bids.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.mysql.models.BidItem;
import org.dimasik.liteauction.backend.utils.Formatter;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Getter
public class Main extends AbstractMenu {
    @Setter
    private BidsSortingType sortingType;
    @Setter
    private CategoryType categoryType;
    @Setter
    private HashSet<String> filters;
    @Setter
    private int page;
    private String player;
    private HashMap<Integer, BidItem> items = new HashMap<>();

    public Main(int page){
        this.sortingType = BidsSortingType.CHEAPEST_FIRST;
        this.categoryType = CategoryType.ALL;
        filters = new HashSet<>();
        this.page = page;
    }

    public Main compile(){
        try {
            items.clear();
            int slot = 0;
            List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItems(player, sortingType, filters, categoryType).get();
            int startIndex = 45 * (page - 1);
            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);
            inventory = Bukkit.createInventory(this, 54, Parser.color("&0Аукцион (" + page + "/" + pages + ")"));
            for(int i = startIndex; i < items.size() && slot < 45; i++) {
                BidItem bidItem = items.get(i);
                this.items.put(slot, bidItem);
                ItemStack itemStack = bidItem.decodeItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Категория:&x&0&0&D&8&F&F " + String.join("&f, &x&0&0&D&8&F&F", TagUtil.getItemCategories(bidItem.getTags()))));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Продавец:&x&0&0&D&8&F&F " + bidItem.getPlayer()));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Истекает через:&x&0&0&D&8&F&F " + Formatter.getTimeUntilExpiration(bidItem)));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Текущая цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(bidItem.getCurrentPrice())));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Цена шага:&x&0&0&D&8&F&F " + Formatter.formatPrice(bidItem.getStep())));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F● &x&D&5&D&B&D&CДанный товар можно"));
                lore.add(Parser.color(" &0.&x&D&5&D&B&D&C  купить &x&0&0&D&8&F&Fтолько полностью&x&D&5&D&B&D&C."));
                lore.add(Parser.color(""));
                if(bidItem.getPlayer().equalsIgnoreCase(viewer.getName())){
                    lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы снять с продажи"));
                }
                else{
                    lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите ЛКМ, чтобы сделать ставку"));
                }
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(slot, itemStack);
                slot++;
            }

            if(true){
                ItemStack itemStack = new ItemStack(Material.ENDER_CHEST);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F ❏ Товары на продаже ❏"));
                List<String> lore = new ArrayList<>();
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C В этом разделе можно узнать,"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C какие товары &x&0&0&D&8&F&Fсейчас на продаже&f."));
                lore.add(Parser.color(""));
                lore.add(Parser.color("   &6Товаров на продаже:&x&0&0&D&8&F&F " + LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getPlayerItems(viewer.getName()).get().size()));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы открыть"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(45, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F ❏ Просроченные товары ❏"));
                List<String> lore = new ArrayList<>();
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C В этом разделе можно узнать,"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C какие ваши товары больше"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&0&0&D&8&F&F не продаются &x&D&5&D&B&D&Cна аукционе."));
                lore.add(Parser.color(""));
                lore.add(Parser.color("   &6Неактивных предметов:&x&0&0&D&8&F&F " + LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItems(viewer.getName()).get().size()));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы открыть"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(46, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.EMERALD);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CОбновить аукцион"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(47, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.GRAY_DYE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F◀ Предыдущая страница"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(48, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.NETHER_STAR);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Помощь по аукциону"));
                List<String> lore = new ArrayList<>();
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &f&m                                      &f "));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&9&C&F&9&F&F              Аукцион"));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&D&5&D&B&D&C Вы выставляете свои вещи,"));
                lore.add(Parser.color(" &x&D&5&D&B&D&C а другие игроки режима"));
                lore.add(Parser.color(" &x&D&5&D&B&D&C покупают их у вас."));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &f&m                                      &f "));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F▶ &x&D&5&D&B&D&CНажмите, чтобы выбрать режим: &x&0&0&D&8&F&FТорги"));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(49, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.LIME_DYE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&6Следующая страница ▶"));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(50, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.PAPER);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Помощь по системе аукциона:"));
                List<String> lore = new ArrayList<>();
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Чтобы выставлять предметы на продажу,"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C нужно просто написать команду:"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F /ah sell <цена>"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Если вы хотите выставить предметы"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C без поштучной возможности покупки:"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F /ah sell <цена> full"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Чтобы найти нужный предмет"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C по названию, пропишите команду:"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F /ah search <название>"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Чтобы найти все товары игрока,"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C пропишите команду:"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F /ah player <никнейм>"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Чтобы переключить звуковые"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C уведомления, пропишите команду:"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&0&0&D&8&F&F /ah sound"));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍"));
                lore.add(Parser.color(""));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(51, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.HOPPER);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Сортировка"));
                List<String> lore = new ArrayList<>();
                if(sortingType == BidsSortingType.CHEAPEST_FIRST){
                    lore.add(Parser.color("&o&6&6✔&6 &6Сначала дешевые"));
                }
                else{
                    lore.add(Parser.color("&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&CСначала дешевые"));
                }
                if(sortingType == BidsSortingType.EXPENSIVE_FIRST){
                    lore.add(Parser.color("&o&6&6✔&6 &6Сначала дорогие"));
                }
                else{
                    lore.add(Parser.color("&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&CСначала дорогие"));
                }
                if(sortingType == BidsSortingType.NEWEST_FIRST){
                    lore.add(Parser.color("&o&6&6✔&6 &6Сначала новые"));
                }
                else{
                    lore.add(Parser.color("&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&CСначала новые"));
                }
                if(sortingType == BidsSortingType.OLDEST_FIRST){
                    lore.add(Parser.color("&o&6&6✔&6 &6Сначала старые"));
                }
                else{
                    lore.add(Parser.color("&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&CСначала старые"));
                }
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(52, itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.CHEST_MINECART);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Категории предметов"));
                List<String> lore = new ArrayList<>();
                for(CategoryType categoryType : CategoryType.values()){
                    if(this.categoryType == categoryType){
                        lore.add(Parser.color("&o&6&6✔&6 &6" + categoryType.getDisplayName()));
                    }
                    else{
                        lore.add(Parser.color("&o&x&9&C&F&9&F&F● &f" + categoryType.getDisplayName()));
                    }
                }
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(53, itemStack);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Main setTarget(String player){
        this.player = player;
        return this;
    }
}
