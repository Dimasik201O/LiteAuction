package org.dimasik.liteauction.frontend.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.Main;
import org.dimasik.liteauction.frontend.menus.RemoveItem;
import org.dimasik.liteauction.frontend.menus.Sell;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;
import static org.dimasik.liteauction.LiteAuction.removeClosedUpdates;

public class SellListener implements Listener {
    @EventHandler
    public void on(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof Sell) {
            event.setCancelled(true);
            Sell sell = (Sell) inventory.getHolder();
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if(slot < 45){
                    SellItem sellItem = sell.getItems().get(slot);
                    if(sellItem != null){
                        if(sellItem.getPlayer().equalsIgnoreCase(player.getName())){
                            Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(sellItem.getId()).get();
                            if (sellItemOptional.isEmpty()){
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                                return;
                            }
                            else if(sellItemOptional.get().getAmount() < sellItem.getAmount()){
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                                return;
                            }

                            ItemStack itemStack = sellItem.decodeItemStack();
                            ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + itemStack.getAmount() + " &fбыл снят с продажи."), itemStack);
                            LiteAuction.getInstance().getRedisManager().publishMessage("update", sellItem.getId());
                            addItemInventory(player.getInventory(), itemStack.asQuantity(sellItem.getAmount()), player.getLocation());
                            LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(sellItem.getId());

                            int newPage = sell.getPage();

                            List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItems(sell.getViewer().getName()).get();
                            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                            newPage = Math.min(pages, newPage);
                            newPage = Math.max(1, newPage);

                            Sell newSell = new Sell(newPage, sell.getBack());
                            newSell.setPlayer(player).compile().open();
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
        if(inventory.getHolder() instanceof Sell){
            removeClosedUpdates();
        }
    }
}
