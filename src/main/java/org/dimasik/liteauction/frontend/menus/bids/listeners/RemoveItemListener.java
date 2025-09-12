package org.dimasik.liteauction.frontend.menus.bids.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.bids.menus.Main;
import org.dimasik.liteauction.frontend.menus.bids.menus.RemoveItem;

import java.util.Optional;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;

public class RemoveItemListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof RemoveItem removeItem) {
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                switch (slot) {
                    case 0:
                    case 1:
                    case 2:
                    case 9:
                    case 10:
                    case 11:
                    case 18:
                    case 19:
                    case 20:
                        Optional<BidItem> bidItemOptional = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(removeItem.getBidItem().getId()).get();
                        if (bidItemOptional.isEmpty()){
                            player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                            return;
                        }

                        ItemStack itemStack = removeItem.getBidItem().decodeItemStack();
                        ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + itemStack.getAmount() + " &fбыл снят с продажи."), itemStack);
                        LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "market " + removeItem.getBidItem().getId());
                        addItemInventory(player.getInventory(), itemStack, player.getLocation());
                        LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().deleteItem(removeItem.getBidItem().getId());

                        player.closeInventory();
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 15:
                    case 16:
                    case 17:
                    case 24:
                    case 25:
                    case 26:
                        player.closeInventory();
                        break;
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
        if(inventory.getHolder() instanceof RemoveItem removeItem) {
            if(removeItem.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = removeItem.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
