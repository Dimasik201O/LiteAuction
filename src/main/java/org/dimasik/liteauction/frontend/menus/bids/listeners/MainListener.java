package org.dimasik.liteauction.frontend.menus.bids.listeners;

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
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.mysql.models.Bid;
import org.dimasik.liteauction.backend.mysql.models.BidItem;
import org.dimasik.liteauction.backend.mysql.models.GuiData;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.backend.utils.TagUtil;
import org.dimasik.liteauction.frontend.commands.CommandExecutor;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.bids.menus.ItemBids;
import org.dimasik.liteauction.frontend.menus.bids.menus.Main;
import org.dimasik.liteauction.frontend.menus.bids.menus.Sell;
import org.dimasik.liteauction.frontend.menus.bids.menus.RemoveItem;
import org.dimasik.liteauction.frontend.menus.market.menus.Unsold;

import java.util.List;
import java.util.Optional;

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
            try {
                if (slot < 45) {
                    if(event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER){
                        return;
                    }
                    BidItem bidItem = main.getItems().get(slot);
                    if(bidItem != null){
                        if(bidItem.getPlayer().equalsIgnoreCase(player.getName())){
                            Optional<BidItem> bidItemOptional = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(bidItem.getId()).get();
                            if (bidItemOptional.isEmpty()){
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                                return;
                            }

                            List<Bid> bids = LiteAuction.getInstance().getDatabaseManager().getBidsManager().getBidsByItemId(bidItem.getId()).get();
                            if(!bids.isEmpty()) {
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fДанный предмет больше нельзя снять с продажи."));
                                new ItemBids(bidItem, main).setPlayer(player).compile().open();
                                return;
                            }
                            new RemoveItem(bidItem, main).setPlayer(player).compile().open();
                        }
                        else {
                            Optional<BidItem> bidItemOptional = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(bidItem.getId()).get();
                            if (bidItemOptional.isEmpty()){
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                                return;
                            }

                            new ItemBids(bidItem, main).setPlayer(player).compile().open();
                        }
                    }
                } else if (slot == 45) {
                    new Sell(1, main).setPlayer(player).compile().open();
                } else if (slot == 46) {
                    new Unsold(1, main).setPlayer(player).compile().open();
                } else if (slot == 47) {
                    player.sendMessage(Parser.color("&#00D4FB▶ &fАукцион обновлен."));
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_LAUNCH, 1f, 1f);
                    }

                    int newPage = main.getPage();

                    List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    Main newMain = new Main(newPage);
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setCategoryType(main.getCategoryType());
                    newMain.setSortingType(main.getSortingType());
                    newMain.setPlayer(player).compile().open();
                } else if (slot == 48) {
                    int newPage = main.getPage() - 1;

                    List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

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
                } else if(slot == 49){
                    GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
                    MarketSortingType marketSortingType = guiData.getMarketSortingType();

                    int newPage = main.getPage();

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(main.getPlayer(), marketSortingType, main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    org.dimasik.liteauction.frontend.menus.market.menus.Main newMain = new org.dimasik.liteauction.frontend.menus.market.menus.Main(main.getPage());
                    newMain.setTarget(main.getPlayer());
                    newMain.setFilters(main.getFilters());
                    newMain.setCategoryType(main.getCategoryType());
                    newMain.setSortingType(marketSortingType);
                    newMain.setPlayer(player).compile().open();
                    player.sendMessage(Parser.color("&#00D4FB▶ &fРежим торговли был обновлен на: &#E7E7E7Торги&f."));
                } else if (slot == 50) {
                    int newPage = main.getPage() + 1;

                    List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

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
                else if (slot == 52) {
                    BidsSortingType newSortingType;
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

                    List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItems(main.getPlayer(), newSortingType, main.getFilters(), main.getCategoryType()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

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
                else if (slot == 53) {
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

                    List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItems(main.getPlayer(), main.getSortingType(), main.getFilters(), newCategoryType).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

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
                guiData.setBidsSortingType(main.getSortingType());
                guiData.setAuctionType(AuctionType.BIDS);
                LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().saveOrUpdateGuiData(guiData);
            }
            catch (Exception ignored) {}
        }
    }
}
