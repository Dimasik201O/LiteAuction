package org.dimasik.liteauction.frontend.menus.market.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.tags.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.format.Parser;
import org.dimasik.liteauction.frontend.menus.abst.AbstractListener;
import org.dimasik.liteauction.frontend.menus.market.menus.Main;
import org.dimasik.liteauction.frontend.menus.market.menus.RemoveItem;

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
                if (ConfigUtils.getSlots("design/menus/market/remove_item.yml", "approve.slot").contains(slot)) {
                    Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(removeItem.getSellItem().getId()).get();
                    if (sellItemOptional.isEmpty()){
                        player.sendMessage(Parser.color(ConfigManager.getString("design/menus/market/remove_item.yml", "messages.cannot_take_item", "&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили.")));
                        return;
                    }
                    else if(sellItemOptional.get().getAmount() < removeItem.getSellItem().getAmount()){
                        player.sendMessage(Parser.color(ConfigManager.getString("design/menus/market/remove_item.yml", "messages.cannot_take_item", "&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили.")));
                        return;
                    }

                    ItemStack itemStack = removeItem.getSellItem().decodeItemStack();
                    ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + removeItem.getSellItem().getAmount() + " &fбыл снят с продажи."), itemStack);
                    LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "market " + removeItem.getSellItem().getId());
                    addItemInventory(player.getInventory(), itemStack.asQuantity(removeItem.getSellItem().getAmount()), player.getLocation());
                    LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(removeItem.getSellItem().getId());

                    player.closeInventory();
                } else if (ConfigUtils.getSlots("design/menus/market/remove_item.yml", "cancel.slot").contains(slot)) {
                    player.closeInventory();
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
