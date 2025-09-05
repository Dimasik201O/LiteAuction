package org.dimasik.liteauction.frontend.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.Main;
import org.dimasik.liteauction.frontend.menus.RemoveItem;

import java.util.Optional;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;

public class RemoveItemListener implements Listener {
    @EventHandler
    public void on(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof RemoveItem) {
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            RemoveItem removeItem = (RemoveItem) inventory.getHolder();
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
                        Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(removeItem.getSellItem().getId()).get();
                        if (sellItemOptional.isEmpty()){
                            player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                            return;
                        }
                        else if(sellItemOptional.get().getAmount() < removeItem.getSellItem().getAmount()){
                            player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                            return;
                        }

                        ItemStack itemStack = removeItem.getSellItem().decodeItemStack();
                        ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + removeItem.getSellItem().getAmount() + " &fбыл снят с продажи."), itemStack);
                        LiteAuction.getInstance().getRedisManager().publishMessage("update", removeItem.getSellItem().getId());
                        addItemInventory(player.getInventory(), itemStack.asQuantity(removeItem.getSellItem().getAmount()), player.getLocation());
                        LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(removeItem.getSellItem().getId());

                        Main main = removeItem.getBack();
                        main.compile().open();
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
                        Main back = removeItem.getBack();
                        back.compile().open();
                        break;
                }
            } catch (Exception e) {
                player.closeInventory();
                player.sendMessage(Parser.color("&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия."));
            }
        }
    }
}
