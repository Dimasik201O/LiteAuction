package org.dimasik.liteauction.frontend.menus.market.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.config.utils.PlaceholderUtils;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.exceptions.UnsupportedConfigurationException;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Getter
public class Main extends AbstractMenu {
    @Setter
    private MarketSortingType sortingType;
    @Setter
    private CategoryType categoryType;
    @Setter
    private HashSet<String> filters;
    @Setter
    private int page;
    private String player;
    private HashMap<Integer, SellItem> items = new HashMap<>();

    public Main(int page){
        this.sortingType = MarketSortingType.CHEAPEST_FIRST;
        this.categoryType = CategoryType.ALL;
        filters = new HashSet<>();
        this.page = page;
    }

    public Main compile(){
        try {
            items.clear();
            List<Integer> slots = ConfigUtils.getSlots("design/menus/market/main.yml", "active-items.slot");
            int slotIndex = 0;
            List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(player, sortingType, filters, categoryType).get();
            int slotsCount = slots.size();
            int startIndex = slotsCount * (page - 1);
            int pages = items.size() / slotsCount + (items.size() % slotsCount == 0 ? 0 : 1);
            inventory = ConfigUtils.buildInventory(this, "design/menus/market/main.yml", "inventory-type",
                    PlaceholderUtils.replace(
                            ConfigManager.getString("design/menus/market/main.yml", "gui-title", "&0Аукцион (%current_page%/%pages_amount%)"),
                            true,
                            new Pair<>("%current_page%", String.valueOf(page)),
                            new Pair<>("%pages_amount%", String.valueOf(pages))
                    )
            );
            for(int i = startIndex; i < items.size() && slotIndex < slotsCount; i++) {
                int slot = slots.get(slotIndex);
                SellItem sellItem = items.get(i);
                this.items.put(slot, sellItem);
                ItemStack itemStack = sellItem.decodeItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.main").stream().map(s -> PlaceholderUtils.replace(
                        s,
                        true,
                        new Pair<>("%categories%", String.join(ConfigManager.getString("design/menus/main.yml", "category-splitter", "&f, &x&0&0&D&8&F&F"), TagUtil.getItemCategories(sellItem.getTags()))),
                        new Pair<>("%seller%", sellItem.getPlayer()),
                        new Pair<>("%expirytime%", Formatter.getTimeUntilExpiration(sellItem)),
                        new Pair<>("%price%", String.valueOf(sellItem.getPrice())),
                        new Pair<>("%full_price%", String.valueOf(sellItem.getPrice() * sellItem.getAmount())),
                        new Pair<>("%format:price%", Formatter.formatPrice(sellItem.getPrice())),
                        new Pair<>("%format:full_price%", Formatter.formatPrice(sellItem.getPrice() * sellItem.getAmount()))
                )).toList());
                if(sellItem.isByOne()){
                    lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.by-one").stream().map(s -> PlaceholderUtils.replace(
                            s,
                            true
                    )).toList());
                }
                if(sellItem.getPlayer().equalsIgnoreCase(viewer.getName())){
                    lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.seller").stream().map(s -> PlaceholderUtils.replace(
                            s,
                            true
                    )).toList());
                }
                else{
                    lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.buy").stream().map(s -> PlaceholderUtils.replace(
                            s,
                            true
                    )).toList());
                    if(!sellItem.isByOne()) {
                        lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.buy-by-one").stream().map(s -> PlaceholderUtils.replace(
                                s,
                                true
                        )).toList());
                    }
                }
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                itemStack.setAmount(sellItem.getAmount());
                inventory.setItem(slot, itemStack);
                slotIndex++;
            }

            if(true){
                int item_count = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItems(viewer.getName()).get().size();
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "on-sell-items",
                        "&x&0&0&D&8&F&F ❏ Товары на продаже ❏",
                        new Pair<>("%item_count%", String.valueOf(item_count))
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                int item_count = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItems(viewer.getName()).get().size();
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "unsold-items",
                        "&x&0&0&D&8&F&F ❏ Просроченные товары ❏",
                        new Pair<>("%item_count%", String.valueOf(item_count))
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "update",
                        "&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CОбновить аукцион"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "prev-page",
                        "&x&0&0&D&8&F&F◀ Предыдущая страница"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "switch",
                        "&x&0&0&D&8&F&F Помощь по аукциону"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "next-page",
                        "&6Следующая страница ▶"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "help",
                        "&x&0&0&D&8&F&F Помощь по системе аукциона:"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.HOPPER);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Сортировка"));
                List<String> lore = new ArrayList<>();
                for(MarketSortingType sortingType : MarketSortingType.values()){
                    if(this.sortingType == sortingType){
                        lore.add(Parser.color("&o&6&6✔&6 &6" + sortingType.getDisplayName()));
                    }
                    else{
                        lore.add(Parser.color("&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&C" + sortingType.getDisplayName()));
                    }
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
