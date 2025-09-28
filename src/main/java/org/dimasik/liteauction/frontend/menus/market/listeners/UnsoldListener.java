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
import org.dimasik.liteauction.backend.storage.models.UnsoldItem;
import org.dimasik.liteauction.backend.utils.tags.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.abst.AbstractMenu;
import org.dimasik.liteauction.frontend.menus.market.menus.Unsold;

import java.util.List;
import java.util.Optional;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;

public class UnsoldListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof Unsold unsold) {
            event.setCancelled(true);
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
                            Optional<UnsoldItem> unsoldItemOptional = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getItemById(unsoldItem.getId()).get();
                            if(unsoldItemOptional.isEmpty()){
                                player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                                return;
                            }

                            ItemStack itemStack = unsoldItem.decodeItemStack();
                            ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + unsoldItem.getAmount() + " &fбыл снят с продажи."), itemStack);
                            addItemInventory(player.getInventory(), itemStack.asQuantity(unsoldItem.getAmount()), player.getLocation());
                            LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().deleteItem(unsoldItem.getId());

                            int newPage = unsold.getPage();

                            int items = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItemsCount(unsold.getViewer().getName()).get();
                            int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

                            newPage = Math.min(pages, newPage);
                            newPage = Math.max(1, newPage);

                            unsold.setForceClose(true);
                            Unsold newUnsold = new Unsold(newPage, unsold.getBack());
                            newUnsold.setPlayer(player).compile().open();
                        }
                    }
                }
                else if(slot == 45){
                    player.closeInventory();
                } else if (slot == 48) {
                    int newPage = unsold.getPage() - 1;

                    int items = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItemsCount(unsold.getViewer().getName()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

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

                    int items = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItemsCount(unsold.getViewer().getName()).get();
                    int pages = items / 45 + (items % 45 == 0 ? 0 : 1);

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
        if(inventory.getHolder() instanceof Unsold unsold){
            if(unsold.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                AbstractMenu main = unsold.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
