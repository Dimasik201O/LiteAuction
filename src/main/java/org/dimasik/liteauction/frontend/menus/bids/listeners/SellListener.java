package org.dimasik.liteauction.frontend.menus.bids.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.mysql.models.Bid;
import org.dimasik.liteauction.backend.mysql.models.BidItem;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.bids.menus.Main;
import org.dimasik.liteauction.frontend.menus.bids.menus.Sell;

import java.util.List;
import java.util.Optional;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;

public class SellListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof Sell sell) {
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if(slot < 45){
                    BidItem bidItem = sell.getItems().get(slot);
                    if(bidItem != null){
                        if(bidItem.getPlayer().equalsIgnoreCase(player.getName())){
                            Optional<BidItem> bidItemOptional = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(bidItem.getId()).get();
                            if (bidItemOptional.isEmpty()){
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                                return;
                            }

                            ItemStack itemStack = bidItem.decodeItemStack();
                            List<Bid> bids = LiteAuction.getInstance().getDatabaseManager().getBidsManager().getBidsByItemId(bidItem.getId()).get();
                            if(!bids.isEmpty()) {
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fДанный предмет больше нельзя снять с продажи."));
                                return;
                            }

                            ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + itemStack.getAmount() + " &fбыл снят с продажи."), itemStack);
                            addItemInventory(player.getInventory(), itemStack, player.getLocation());
                            LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().deleteItem(bidItem.getId());

                            int newPage = sell.getPage();

                            List<BidItem> items = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getPlayerItems(sell.getViewer().getName()).get();
                            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                            newPage = Math.min(pages, newPage);
                            newPage = Math.max(1, newPage);

                            sell.setForceClose(true);
                            Sell newSell = new Sell(newPage, sell.getBack());
                            newSell.setPlayer(player).compile().open();

                            LiteAuction.getInstance().getRedisManager().publishMessage("update", "bids " + bidItem.getId() + " delete");
                        }
                    }
                }
                else if(slot == 45){
                    Main main = sell.getBack();
                    main.compile().open();
                } else if (slot == 48) {
                    int newPage = sell.getPage() - 1;

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItems(sell.getViewer().getName()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != sell.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        sell.setForceClose(true);
                        Sell newSell = new Sell(newPage, sell.getBack());
                        newSell.setPlayer(player).compile().open();
                    }
                } else if (slot == 50) {
                    int newPage = sell.getPage() + 1;

                    List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItems(sell.getViewer().getName()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != sell.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        sell.setForceClose(true);
                        Sell newSell = new Sell(newPage, sell.getBack());
                        newSell.setPlayer(player).compile().open();
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
        if(inventory.getHolder() instanceof Sell sell){
            if(sell.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = sell.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
