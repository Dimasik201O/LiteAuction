package org.dimasik.liteauction.frontend.menus.market.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.api.events.market.buy.PreClickSellItemEvent;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.exceptions.UnsupportedConfigurationException;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.storage.models.GuiData;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.backend.utils.tags.TagUtil;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.market.menus.*;

import java.util.ArrayList;
import java.util.List;

public class MainListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof Main main){
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            List<Integer> slots = ConfigUtils.getSlots("design/menus/market/main.yml", "active-items.slot");
            try {
                if (slots.contains(slot)) {
                    if(event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER){
                        return;
                    }
                    SellItem sellItem = main.getItems().get(slot);
                    if(sellItem != null){
                        PreClickSellItemEvent preEvent = new PreClickSellItemEvent(player, sellItem);
                        LiteAuction.getEventManager().triggerEvent(preEvent);
                        if(preEvent.isCancelled()){
                            return;
                        }

                        if(sellItem.getPlayer().equalsIgnoreCase(player.getName())){
                            new RemoveItem(sellItem, main).setPlayer(player).compile().open();
                        }
                        else{
                            if(event.getClick() == ClickType.SWAP_OFFHAND && player.hasPermission("liteauction.admin")){
                                player.sendMessage("");
                                player.sendMessage("Вы нажали 'F' - означает удаление предмета.");
                                player.sendMessage("Для подтверждения удаления предмета напишите команду:");
                                player.sendMessage("/ah admin deleteItem " + sellItem.getId());
                                player.sendMessage("");
                                player.closeInventory();
                                player.updateInventory();
                                Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), player::updateInventory, 1);
                            }
                            else if(event.getClick() == ClickType.MIDDLE || event.getClick() == ClickType.DROP){
                                if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                                }
                                Main newMain = new Main(1);
                                newMain.setFilters(TagUtil.getPartialTags(sellItem.decodeItemStack()));
                                newMain.setCategoryType(main.getCategoryType());
                                newMain.setSortingType(main.getSortingType());
                                newMain.setPlayer(player).compile().open();
                            }
                            else if(event.isLeftClick() || sellItem.isByOne() || sellItem.getAmount() == 1) {
                                double money = LiteAuction.getEconomyEditor().getBalance(player.getName());
                                int price = sellItem.getPrice() * sellItem.getAmount();
                                if(money < price){
                                    player.sendMessage(Parser.color(ConfigManager.getString(
                                            "design/menus/market/main.yml",
                                            "active-items.no-money.message",
                                            "&#FB2222▶ &fУ вас &#FB2222недостаточно средств &fдля совершения покупки."
                                    )));
                                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                                        player.playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1f, 1f);
                                    }
                                    if(ConfigManager.getBoolean(
                                            "design/menus/market/main.yml",
                                            "active-items.no-money.item.enable",
                                            true
                                    )){
                                        ItemStack origItem = inventory.getItem(slot).clone();
                                        ItemStack itemStack = new ItemStack(Material.BARRIER);
                                        ItemMeta itemMeta = itemStack.getItemMeta();
                                        itemMeta.setDisplayName(Parser.color(ConfigManager.getString(
                                                "design/menus/market/main.yml",
                                                "active-items.no-money.item.displayname",
                                                "&x&F&F&2&2&2&2▶ &x&D&5&D&B&D&CУ вас &x&F&F&2&2&2&2нет денег &x&D&5&D&B&D&Cна это!"
                                        )));
                                        itemStack.setItemMeta(itemMeta);
                                        inventory.setItem(slot, itemStack);
                                        Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                                            if(!inventory.getViewers().isEmpty()) {
                                                inventory.setItem(slot, origItem);
                                            }
                                        }, 20);
                                    }
                                    return;
                                }

                                new ConfirmItem(sellItem, main).setPlayer(player).compile().open();
                            }
                            else if(event.isRightClick()){
                                new CountBuyItem(sellItem, main, 1).setPlayer(player).compile().open();
                            }
                        }
                    }
                } else if (slot == ConfigManager.getInt("design/menus/market/main.yml", "on-sell-items.slot", 45)) {
                    new Sell(1, main).setPlayer(player).compile().open();
                } else if (slot == ConfigManager.getInt("design/menus/market/main.yml", "unsold-items.slot", 46)) {
                    new Unsold(1, main).setPlayer(player).compile().open();
                } else if (slot == ConfigManager.getInt("design/menus/market/main.yml", "update.slot", 47)) {
                    player.sendMessage(Parser.color(ConfigManager.getString("design/menus/market/main.yml", "update.message", "&#00D4FB▶ &fАукцион обновлен.")));
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1f, 1f);
                    }

                    int newPage = main.getPage();

                    int items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItemsCount(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    Main newMain = new Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setCategoryType(main.getCategoryType());
                    newMain.setSortingType(main.getSortingType());
                    newMain.setPlayer(player).compile().open();
                } else if (slot == ConfigManager.getInt("design/menus/market/main.yml", "prev-page.slot", 48)) {
                    int newPage = main.getPage() - 1;

                    int items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItemsCount(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != main.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        Main newMain = new Main(newPage);
                        newMain.setTarget(main.getPlayer());
                        newMain.setFilters(main.getFilters());
                        newMain.setCategoryType(main.getCategoryType());
                        newMain.setSortingType(main.getSortingType());
                        newMain.setPlayer(player).compile().open();
                    }
                } else if(slot == ConfigManager.getInt("design/menus/market/main.yml", "switch.slot", 49)){
                    GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
                    BidsSortingType bidsSortingType = guiData.getBidsSortingType();

                    int newPage = main.getPage();

                    int items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItemsCount(main.getPlayer(), bidsSortingType, main.getFilters(), main.getCategoryType()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    org.dimasik.liteauction.frontend.menus.bids.menus.Main newMain = new org.dimasik.liteauction.frontend.menus.bids.menus.Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setCategoryType(main.getCategoryType());
                    newMain.setSortingType(bidsSortingType);
                    newMain.setPlayer(player).compile().open();
                    player.sendMessage(Parser.color("&#00D4FB▶ &fРежим торговли был обновлен на: &#E7E7E7Ставки&f."));
                } else if (slot == ConfigManager.getInt("design/menus/market/main.yml", "next-page.slot", 50)) {
                    int newPage = main.getPage() + 1;

                    int items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItemsCount(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != main.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        Main newMain = new Main(newPage);
                        newMain.setTarget(main.getPlayer());
                        newMain.setFilters(main.getFilters());
                        newMain.setCategoryType(main.getCategoryType());
                        newMain.setSortingType(main.getSortingType());
                        newMain.setPlayer(player).compile().open();
                    }
                }
                else if (slot == ConfigManager.getInt("design/menus/market/main.yml", "sorting.slot", 52)) {
                    MarketSortingType newSortingType;
                    if(event.isLeftClick()) {
                        newSortingType = main.getSortingType().relative(true);
                    }
                    else if(event.isRightClick()) {
                        newSortingType = main.getSortingType().relative(false);
                    }
                    else{
                        return;
                    }
                    int newPage = main.getPage();

                    int items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItemsCount(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    Main newMain = new Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setCategoryType(main.getCategoryType());
                    newMain.setSortingType(newSortingType);
                    newMain.setPlayer(player).compile().open();
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    }
                }
                else if (slot == ConfigManager.getInt("design/menus/market/main.yml", "category.slot", 53)) {
                    CategoryType newCategoryType;
                    if(event.isLeftClick()) {
                        newCategoryType = main.getCategoryType().relative(true);
                    }
                    else if(event.isRightClick()) {
                        newCategoryType = main.getCategoryType().relative(false);
                    }
                    else{
                        return;
                    }
                    int newPage = main.getPage();

                    int items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItemsCount(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    Main newMain = new Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setSortingType(main.getSortingType());
                    newMain.setCategoryType(newCategoryType);
                    newMain.setPlayer(player).compile().open();
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    }
                }
            } catch (Exception e) {
                player.closeInventory();
                player.sendMessage(Parser.color("&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия."));
            }
        }
    }

    @EventHandler
    public void on(InventoryCloseEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof Main main){
            Player player = (Player) event.getPlayer();
            try {
                GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
                guiData.setCategoryType(main.getCategoryType());
                guiData.setMarketSortingType(main.getSortingType());
                guiData.setAuctionType(AuctionType.MARKET);
                LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().saveOrUpdateGuiData(guiData);
            }
            catch (Exception ignored) {}
        }
    }
}
