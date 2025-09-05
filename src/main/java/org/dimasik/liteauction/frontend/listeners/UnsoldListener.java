package org.dimasik.liteauction.frontend.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.mysql.models.UnsoldItem;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.Main;
import org.dimasik.liteauction.frontend.menus.Sell;
import org.dimasik.liteauction.frontend.menus.Unsold;

import java.util.List;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;
import static org.dimasik.liteauction.LiteAuction.removeClosedUpdates;

public class UnsoldListener implements Listener {

    @EventHandler
    public void on(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof Unsold) {
            event.setCancelled(true);
            Unsold unsold = (Unsold) inventory.getHolder();
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if(slot < 45){
                    UnsoldItem unsoldItem = unsold.getItems().get(slot);
                    if(unsoldItem != null){
                        if(unsoldItem.getPlayer().equalsIgnoreCase(player.getName())){
                            ItemStack itemStack = unsoldItem.decodeItemStack();
                            ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + unsoldItem.getAmount() + " &fбыл снят с продажи."), itemStack);
                            addItemInventory(player.getInventory(), itemStack.asQuantity(unsoldItem.getAmount()), player.getLocation());
                            LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().deleteItem(unsoldItem.getId());

                            int newPage = unsold.getPage();

                            List<UnsoldItem> items = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItems(unsold.getViewer().getName()).get();
                            int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                            newPage = Math.min(pages, newPage);
                            newPage = Math.max(1, newPage);

                            unsold.setForceClose(true);
                            Unsold newUnsold = new Unsold(newPage, unsold.getBack());
                            newUnsold.setPlayer(player).compile().open();
                        }
                    }
                }
                else if(slot == 45){
                    Main main = unsold.getBack();
                    main.compile().open();

                } else if (slot == 48) {
                    int newPage = unsold.getPage() - 1;

                    List<UnsoldItem> items = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItems(unsold.getViewer().getName()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != unsold.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        unsold.setForceClose(true);
                        Unsold newUnsold = new Unsold(newPage, unsold.getBack());
                        newUnsold.setPlayer(player).compile().open();
                    }
                } else if (slot == 50) {
                    int newPage = unsold.getPage() + 1;

                    List<UnsoldItem> items = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItems(unsold.getViewer().getName()).get();
                    int pages = items.size() / 45 + (items.size() % 45 == 0 ? 0 : 1);

                    newPage = Math.min(pages, newPage);
                    newPage = Math.max(1, newPage);

                    if(newPage != unsold.getPage()) {
                        if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(player.getName()).get()) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 1f, 1f);
                        }
                        unsold.setForceClose(true);
                        Unsold newUnsold = new Unsold(newPage, unsold.getBack());
                        newUnsold.setPlayer(player).compile().open();
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
        if(inventory.getHolder() instanceof Unsold){
            Unsold unsold = (Unsold) inventory.getHolder();
            if(unsold.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = unsold.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
