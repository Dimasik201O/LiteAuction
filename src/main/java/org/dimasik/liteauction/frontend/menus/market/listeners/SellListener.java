package org.dimasik.liteauction.frontend.menus.market.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.market.menus.Main;
import org.dimasik.liteauction.frontend.menus.market.menus.Sell;

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
                            ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + sellItem.getAmount() + " &fбыл снят с продажи."), itemStack);
                            addItemInventory(player.getInventory(), itemStack.asQuantity(sellItem.getAmount()), player.getLocation());
                            LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(sellItem.getId());

                            int newPage = sell.getPage();

                            List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItems(sell.getViewer().getName()).get();
                            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                            newPage = Math.min(pages, newPage);
                            newPage = Math.max(1, newPage);

                            sell.setForceClose(true);
                            Sell newSell = new Sell(newPage, sell.getBack());
                            newSell.setPlayer(player).compile().open();

                            LiteAuction.getInstance().getRedisManager().publishMessage("update", "market " + sellItem.getId());
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
