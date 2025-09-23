package org.dimasik.liteauction.frontend.menus.bids.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.Pair;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.storage.models.Bid;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.format.Formatter;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class ItemBids extends AbstractMenu {
    @Setter
    private boolean forceClose = false;
    private Main back;
    private BidItem bidItem;
    private HashMap<Integer, Bid> bids = new HashMap<>();
    private HashMap<Integer, Integer> availableBids = new HashMap<>();
    private List<Integer> availableBidsSlots = new ArrayList<>();

    public ItemBids(BidItem bidItem, Main back){
        this.bidItem = bidItem;
        this.back = back;
    }

    @Override
    public ItemBids compile() {
        try{
            bids.clear();
            availableBids.clear();
            availableBidsSlots.clear();
            inventory = ConfigUtils.buildInventory(this, "design/menus/bids/item_bids.yml", "inventory-type",
                    ConfigManager.getString("design/menus/bids/item_bids.yml", "gui-title", "&x&0&0&D&8&F&F Просмотр лота")
            );
            if(true){
                ItemStack itemStack = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color(" "));
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(0, itemStack);
                inventory.setItem(1, itemStack);
                inventory.setItem(2, itemStack);
                inventory.setItem(3, itemStack);
                inventory.setItem(5, itemStack);
                inventory.setItem(6, itemStack);
                inventory.setItem(7, itemStack);
                inventory.setItem(8, itemStack);
                inventory.setItem(9, itemStack);
                inventory.setItem(10, itemStack);
                inventory.setItem(11, itemStack);
                inventory.setItem(12, itemStack);
                inventory.setItem(13, itemStack);
                inventory.setItem(14, itemStack);
                inventory.setItem(15, itemStack);
                inventory.setItem(16, itemStack);
                inventory.setItem(17, itemStack);
                inventory.setItem(20, itemStack);
                inventory.setItem(24, itemStack);
                inventory.setItem(27, itemStack);
                inventory.setItem(29, itemStack);
                inventory.setItem(31, itemStack);
                inventory.setItem(33, itemStack);
                inventory.setItem(35, itemStack);
                inventory.setItem(36, itemStack);
                inventory.setItem(38, itemStack);
                inventory.setItem(40, itemStack);
                inventory.setItem(42, itemStack);
                inventory.setItem(44, itemStack);
                inventory.setItem(49, itemStack);
            }
            bidItem = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(bidItem.getId()).get().get();
            if(true){
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
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l&n▍&x&D&5&D&B&D&C Начальная цена:&x&0&0&D&8&F&F " + Formatter.formatPrice(bidItem.getStartPrice())));
                lore.add(Parser.color(" &x&0&0&D&8&F&F&l▍&x&D&5&D&B&D&C Цена шага:&x&0&0&D&8&F&F " + Formatter.formatPrice(bidItem.getStep())));
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F● &x&D&5&D&B&D&CДанный товар можно"));
                lore.add(Parser.color(" &0.&x&D&5&D&B&D&C  купить &x&0&0&D&8&F&Fтолько полностью&x&D&5&D&B&D&C."));
                lore.add(Parser.color(""));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(4, itemStack);
            }

            List<Integer> slotsList = List.of(18, 19, 28, 37, 46, 47, 48, 39, 30, 21, 22, 23, 32, 41, 50, 51, 52, 43, 34, 25, 26);
            int size = slotsList.size() - 1;

            int slot = 0;
            int bidsCount = LiteAuction.getInstance().getDatabaseManager().getBidsManager().getBidsByItemIdCount(bidItem.getId()).get();
            int startIndex = size * (bidsCount / size) - (size * (bidsCount / size) > 0 ? 1 : 0);
            List<Bid> bids = LiteAuction.getInstance().getDatabaseManager().getBidsManager().getBidsByItemId(bidItem.getId(), startIndex, slotsList.size() + 1).get();
            int pages = bids.size() / 45 + (bids.size() % 45 == 0 ? 0 : 1);
            int money = bidItem.getCurrentPrice();
            if(!bids.isEmpty()) money = bids.get(bids.size() - 1).getPrice() + bidItem.getStep();
            for(int i = 0; i < bids.size() && slot < size; i++) {
                Bid bid = bids.get(i);
                this.bids.put(slotsList.get(slot), bid);
                ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&5&F&B&0&0 Установлена ставка"));
                List<String> lore = new ArrayList<>();
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&5&F&B&0&0&n▍"));
                lore.add(Parser.color(" &x&0&5&F&B&0&0&n▍&f Владелец ставки:&x&0&5&F&B&0&0 " + bid.getPlayer()));
                lore.add(Parser.color(" &x&0&5&F&B&0&0&n▍&f Сумма ставки:&x&0&5&F&B&0&0 " + Formatter.formatPrice(bid.getPrice())));
                lore.add(Parser.color(" &x&0&5&F&B&0&0▍"));
                lore.add(Parser.color(" &x&0&5&F&B&0&0&n▍&f Текущая цена лота: &x&0&5&F&B&0&0" + Formatter.formatPrice(bidItem.getCurrentPrice())));
                lore.add(Parser.color(" &x&0&5&F&B&0&0▍"));
                lore.add(Parser.color(""));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(slotsList.get(slot), itemStack);
                slot++;
            }
            while(slot < size + 1){
                this.availableBidsSlots.add(slotsList.get(slot));
                this.availableBids.put(slotsList.get(slot), money);
                ItemStack itemStack = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color("&x&0&0&D&8&F&F Доступна ставка"));
                List<String> lore = new ArrayList<>();
                lore.add(Parser.color(""));
                lore.add(Parser.color(" &x&0&0&D&8&F&F▶&f Нажмите, чтобы сделать ставку в &x&0&0&D&8&F&F" + Formatter.formatPrice(money)));
                lore.add(Parser.color(""));
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(slotsList.get(slot), itemStack);
                money += bidItem.getStep();
                slot++;
            }

            Pair<ItemStack, Integer> back = ConfigUtils.buildItem("design/menus/bids/item_bids.yml", "back", "&x&F&F&2&2&2&2◀ &x&D&5&D&B&D&CНазад");
            inventory.setItem(back.getRight(), back.getLeft());
            Pair<ItemStack, Integer> help = ConfigUtils.buildItem("design/menus/bids/item_bids.yml", "help", "&x&0&0&D&8&F&F Информация про систему ставок");
            inventory.setItem(help.getRight(), help.getLeft());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
