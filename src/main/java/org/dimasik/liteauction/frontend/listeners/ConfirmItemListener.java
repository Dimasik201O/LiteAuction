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
import org.dimasik.liteauction.backend.mysql.models.SellItem;
import org.dimasik.liteauction.backend.utils.Formatter;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;
import org.dimasik.liteauction.frontend.menus.ConfirmItem;
import org.dimasik.liteauction.frontend.menus.CountBuyItem;
import org.dimasik.liteauction.frontend.menus.Main;
import org.dimasik.liteauction.frontend.menus.RemoveItem;

import java.util.Optional;

import static org.dimasik.liteauction.LiteAuction.addItemInventory;

public class ConfirmItemListener implements Listener {
    @EventHandler
    public void on(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof ConfirmItem) {
            event.setCancelled(true);
            ConfirmItem confirmItem = (ConfirmItem) inventory.getHolder();
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
                        Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(confirmItem.getSellItem().getId()).get();
                        if (sellItemOptional.isEmpty()){
                            player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                            return;
                        }
                        else if(sellItemOptional.get().getAmount() < confirmItem.getSellItem().getAmount()){
                            player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                            return;
                        }
                        SellItem sellItem = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(confirmItem.getSellItem().getId()).get().get();
                        int price = sellItem.getPrice() * sellItem.getAmount();
                        double money = LiteAuction.getEconomyEditor().getBalance(player.getName());
                        if(money < price){
                            player.sendMessage(Parser.color("&#FB2222▶ &fУ вас &#FB2222недостаточно средств &fдля совершения покупки."));
                            player.playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1f, 1f);
                            Main main = confirmItem.getBack();
                            main.compile().open();
                            return;
                        }

                        ItemStack itemStack = confirmItem.getSellItem().decodeItemStack();
                        ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#FEA900▶ &fВы купили &#FEA900%item%&f &#FEA900x" + itemStack.getAmount() + " &fу &#FEA900" + sellItem.getPlayer() + " &fза &#FEA900" + Formatter.formatPrice(price)), itemStack);
                        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1f, 1f);

                        LiteAuction.getInstance().getRedisManager().publishMessage("update", confirmItem.getSellItem().getId());
                        LiteAuction.getInstance().getRedisManager().publishMessage("msg", sellItem.getPlayer() + " " + Parser.color(ItemHoverUtil.getHoverItemMessage("&#00D4FB▶ &#00D5FB" + player.getName() + " &fкупил у вас &#9AF5FB%item%&f &#9AF5FBx" + sellItem.getAmount() + " &fза &#FEA900" + price + Formatter.CURRENCY_SYMBOL, sellItem.decodeItemStack().asQuantity(sellItem.getAmount()))));
                        LiteAuction.getInstance().getRedisManager().publishMessage("sound", sellItem.getPlayer() + " " + Sound.ENTITY_WANDERING_TRADER_YES.toString().toLowerCase() + " 1.0 1.0");

                        LiteAuction.getEconomyEditor().addBalance(sellItem.getPlayer(), price);
                        LiteAuction.getEconomyEditor().subtractBalance(player.getName(), price);

                        addItemInventory(player.getInventory(), itemStack.asQuantity(confirmItem.getSellItem().getAmount()), player.getLocation());
                        LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(confirmItem.getSellItem().getId());

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
        if(inventory.getHolder() instanceof ConfirmItem) {
            ConfirmItem confirmItem = (ConfirmItem) inventory.getHolder();
            if(confirmItem.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = confirmItem.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
